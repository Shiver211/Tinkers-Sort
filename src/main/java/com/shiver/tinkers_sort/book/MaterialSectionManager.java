package com.shiver.tinkers_sort.book;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shiver.tinkers_sort.config.ModConfig;
import com.shiver.tinkers_sort.sorting.MaterialComparator;
import com.shiver.tinkers_sort.sorting.MaterialFilter;
import com.shiver.tinkers_sort.sorting.SortMode;
import com.shiver.tinkers_sort.sorting.SortOrder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.gui.book.element.ElementImage;
import slimeknights.mantle.client.gui.book.element.ElementItem;
import slimeknights.mantle.client.gui.book.element.SizedBookElement;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.book.content.ContentListing;
import slimeknights.tconstruct.library.book.content.ContentMaterial;
import slimeknights.tconstruct.library.book.content.ContentPageIconList;
import slimeknights.tconstruct.library.book.content.ContentSingleStatMultMaterial;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.MaterialTypes;

import java.util.*;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class MaterialSectionManager {

    private static final List<String> BOW_MATERIAL_TYPES = ImmutableList.of(
            MaterialTypes.BOW, MaterialTypes.BOWSTRING, MaterialTypes.SHAFT, MaterialTypes.FLETCHING
    );

    private static List<Material> rawToolMaterials = new ArrayList<>();
    private static Map<String, Integer> defaultToolIndices = new HashMap<>();

    private static Map<String, List<Material>> rawBowMaterials = new HashMap<>();
    private static Map<String, Map<String, Integer>> defaultBowIndices = new HashMap<>();

    private static SortMode currentMode = SortMode.DEFAULT;
    private static SortOrder currentOrder = SortOrder.ASCENDING;
    private static String currentQuery = "";
    private static boolean initialized = false;

    public static void ensureSectionsInitialized(BookData book) {
        if (book == null || book.sections == null) return;
        for (SectionData section : book.sections) {
            if (section != null) {
                if (section.parent == null) {
                    section.parent = book;
                }
                if (section.source == null) {
                    section.source = BookRepository.DUMMY;
                }
            }
        }
    }

    public static SectionData findSection(BookData book, String name) {
        if (book == null || book.sections == null || name == null) return null;
        ensureSectionsInitialized(book);
        for (SectionData section : book.sections) {
            if (section != null && section.name != null && section.name.equalsIgnoreCase(name)) {
                return section;
            }
        }
        return null;
    }

    public static void init(BookData book) {
        if (book == null) return;
        ensureSectionsInitialized(book);

        SectionData toolData = findSection(book, "materials");
        Set<String> addedToolIds = new LinkedHashSet<>();
        rawToolMaterials = new ArrayList<>();

        // 1. Extract any materials already loaded into section pages
        if (toolData != null && toolData.pages != null) {
            for (PageData page : toolData.pages) {
                if (page.content instanceof ContentMaterial) {
                    ContentMaterial cm = (ContentMaterial) page.content;
                    if (cm.materialName != null) {
                        Material m = TinkerRegistry.getMaterial(cm.materialName);
                        if (m != null && !m.isHidden() && addedToolIds.add(m.getIdentifier())) {
                            rawToolMaterials.add(m);
                        }
                    }
                }
            }
        }

        // 2. Also query TinkerRegistry to make sure no valid head materials were missed
        for (Material m : TinkerRegistry.getAllMaterials()) {
            if (!m.isHidden() && m.hasStats(MaterialTypes.HEAD) && addedToolIds.add(m.getIdentifier())) {
                rawToolMaterials.add(m);
            }
        }

        defaultToolIndices.clear();
        for (int i = 0; i < rawToolMaterials.size(); i++) {
            defaultToolIndices.put(rawToolMaterials.get(i).getIdentifier(), i);
        }

        // Collect and cache bow materials
        SectionData bowData = findSection(book, "bowmaterials");
        rawBowMaterials.clear();
        defaultBowIndices.clear();
        for (String type : BOW_MATERIAL_TYPES) {
            List<Material> list = new ArrayList<>();
            Set<String> addedBowIds = new LinkedHashSet<>();

            if (bowData != null && bowData.pages != null) {
                for (PageData page : bowData.pages) {
                    if (page.content instanceof ContentSingleStatMultMaterial) {
                        ContentSingleStatMultMaterial cs = (ContentSingleStatMultMaterial) page.content;
                        if (type.equals(cs.materialType) && cs.materialNames != null) {
                            for (String name : cs.materialNames) {
                                Material m = TinkerRegistry.getMaterial(name);
                                if (m != null && !m.isHidden() && addedBowIds.add(m.getIdentifier())) {
                                    list.add(m);
                                }
                            }
                        }
                    }
                }
            }

            for (Material m : TinkerRegistry.getAllMaterials()) {
                if (!m.isHidden() && m.hasStats(type) && addedBowIds.add(m.getIdentifier())) {
                    list.add(m);
                }
            }

            rawBowMaterials.put(type, list);

            Map<String, Integer> indexMap = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                indexMap.put(list.get(i).getIdentifier(), i);
            }
            defaultBowIndices.put(type, indexMap);
        }

        // Load configured defaults
        if (ModConfig.rememberLastSort) {
            try {
                currentMode = SortMode.valueOf(ModConfig.defaultSortMode);
            } catch (Exception e) {
                currentMode = SortMode.DEFAULT;
            }
            currentOrder = ModConfig.defaultAscending ? SortOrder.ASCENDING : SortOrder.DESCENDING;
        }

        initialized = true;

        if (currentMode != SortMode.DEFAULT || currentOrder != SortOrder.ASCENDING) {
            applySort(book, "materials", currentMode, currentOrder, currentQuery);
        }
    }

    public static SortMode getCurrentMode() {
        return currentMode;
    }

    public static SortOrder getCurrentOrder() {
        return currentOrder;
    }

    public static String getCurrentQuery() {
        return currentQuery;
    }

    public static void applySort(BookData book, String sectionName, SortMode mode, SortOrder order, String query) {
        if (!initialized) {
            init(book);
        }

        currentMode = mode;
        currentOrder = order;
        currentQuery = query == null ? "" : query.trim();

        if (ModConfig.rememberLastSort) {
            ModConfig.defaultSortMode = mode.name();
            ModConfig.defaultAscending = (order == SortOrder.ASCENDING);
            ModConfig.save();
        }

        if ("materials".equalsIgnoreCase(sectionName)) {
            rebuildToolMaterials(book);
        } else if ("bowmaterials".equalsIgnoreCase(sectionName)) {
            rebuildBowMaterials(book);
        } else {
            // Default to rebuilding materials
            rebuildToolMaterials(book);
        }
    }

    private static void rebuildToolMaterials(BookData book) {
        if (book == null) return;
        SectionData data = findSection(book, "materials");
        if (data == null) return;

        if (rawToolMaterials.isEmpty()) {
            init(book);
        }

        data.pages.clear();

        List<Material> filtered = rawToolMaterials.stream()
                .filter(m -> MaterialFilter.matches(m, currentQuery))
                .sorted(new MaterialComparator(currentMode, currentOrder, defaultToolIndices))
                .collect(Collectors.toList());

        String sectionTitle = book.translate("materials");

        if (filtered.isEmpty()) {
            ContentPageIconList emptyOverview = new ContentPageIconList();
            emptyOverview.title = sectionTitle;
            PageData emptyPage = new PageData(true);
            emptyPage.source = data.source != null ? data.source : BookRepository.DUMMY;
            emptyPage.parent = data;
            emptyPage.content = emptyOverview;
            emptyPage.load();
            data.pages.add(emptyPage);
            return;
        }

        List<ContentPageIconList> listPages = ContentPageIconList.getPagesNeededForItemCount(filtered.size(), data, sectionTitle);
        ListIterator<ContentPageIconList> iter = listPages.listIterator();
        ContentPageIconList overview = iter.next();

        for (Material material : filtered) {
            PageData page = new PageData(true);
            page.source = data.source != null ? data.source : BookRepository.DUMMY;
            page.parent = data;
            page.name = material.getIdentifier();
            page.type = ContentMaterial.ID;
            page.content = new ContentMaterial(material);
            page.load();
            data.pages.add(page);

            SizedBookElement icon;
            if (material.getRepresentativeItem() != null && !material.getRepresentativeItem().isEmpty()) {
                icon = new ElementItem(0, 0, 1f, material.getRepresentativeItem());
            } else {
                icon = new ElementImage(ImageData.MISSING);
            }

            while (!overview.addLink(icon, material.getLocalizedNameColored(), page)) {
                if (!iter.hasNext()) break;
                overview = iter.next();
            }
        }
    }

    private static void rebuildBowMaterials(BookData book) {
        if (book == null) return;
        SectionData data = findSection(book, "bowmaterials");
        if (data == null) return;

        if (rawBowMaterials.isEmpty()) {
            init(book);
        }

        data.pages.clear();

        ContentListing listing = new ContentListing();
        listing.title = book.translate("bowmaterials");

        PageData listingPage = new PageData(true);
        listingPage.source = data.source != null ? data.source : BookRepository.DUMMY;
        listingPage.parent = data;
        listingPage.name = "bowmaterials";
        listingPage.type = "";
        listingPage.content = listing;
        listingPage.load();
        data.pages.add(listingPage);

        for (String type : BOW_MATERIAL_TYPES) {
            int pageIndex = data.pages.size();
            List<Material> list = rawBowMaterials.get(type);
            if (list == null) continue;

            List<Material> filtered = list.stream()
                    .filter(m -> MaterialFilter.matches(m, currentQuery))
                    .sorted(new MaterialComparator(currentMode, currentOrder, defaultBowIndices.get(type)))
                    .collect(Collectors.toList());

            if (filtered.isEmpty()) continue;

            String statName = Material.UNKNOWN.getStats(type).getLocalizedName();
            List<ContentPageIconList> contentPages = ContentPageIconList.getPagesNeededForItemCount(filtered.size(), data, statName);
            ListIterator<ContentPageIconList> iter = contentPages.listIterator();
            ContentPageIconList currentOverview = iter.next();
            contentPages.forEach(p -> p.maxScale = 1f);

            for (List<Material> chunk : Lists.partition(filtered, 3)) {
                ContentSingleStatMultMaterial content = new ContentSingleStatMultMaterial(chunk, type);
                String id = type + "_" + chunk.stream().map(Material::getIdentifier).collect(Collectors.joining("_"));
                PageData page = new PageData(true);
                page.source = data.source != null ? data.source : BookRepository.DUMMY;
                page.parent = data;
                page.name = id;
                page.type = ContentSingleStatMultMaterial.ID;
                page.content = content;
                page.load();
                data.pages.add(page);

                for (Material material : chunk) {
                    SizedBookElement icon;
                    if (material.getRepresentativeItem() != null && !material.getRepresentativeItem().isEmpty()) {
                        icon = new ElementItem(0, 0, 1f, material.getRepresentativeItem());
                    } else {
                        icon = new ElementImage(ImageData.MISSING);
                    }

                    if (!currentOverview.addLink(icon, material.getLocalizedNameColored(), page)) {
                        if (!iter.hasNext()) break;
                        currentOverview = iter.next();
                    }
                }
            }

            if (pageIndex < data.pages.size()) {
                listing.addEntry(statName, data.pages.get(pageIndex));
            }
        }
    }
}

