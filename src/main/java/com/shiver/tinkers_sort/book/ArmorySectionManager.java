package com.shiver.tinkers_sort.book;

import c4.conarm.lib.book.content.ContentArmorMaterial;
import c4.conarm.lib.materials.ArmorMaterialType;
import com.google.common.collect.ImmutableList;
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
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.client.gui.book.element.ElementImage;
import slimeknights.mantle.client.gui.book.element.ElementItem;
import slimeknights.mantle.client.gui.book.element.SizedBookElement;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.book.content.ContentMaterial;
import slimeknights.tconstruct.library.book.content.ContentPageIconList;
import slimeknights.tconstruct.library.materials.Material;

import java.util.*;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class ArmorySectionManager {

    private static List<Material> rawArmorMaterials = new ArrayList<>();
    private static Map<String, Integer> defaultArmorIndices = new HashMap<>();

    private static SortMode currentMode = SortMode.DEFAULT;
    private static SortOrder currentOrder = SortOrder.ASCENDING;
    private static String currentQuery = "";

    private static boolean initialized = false;

    public static boolean isValidArmorMaterial(Material material) {
        if (material == null || material.isHidden()) return false;
        return material.hasStats(ArmorMaterialType.CORE) ||
               material.hasStats(ArmorMaterialType.PLATES) ||
               material.hasStats(ArmorMaterialType.TRIM);
    }

    public static void init(BookData book) {
        if (book == null) return;
        MaterialSectionManager.ensureSectionsInitialized(book);

        SectionData data = MaterialSectionManager.findSection(book, "materials");
        Set<String> added = new LinkedHashSet<>();
        rawArmorMaterials = new ArrayList<>();

        // 1. Extract materials already loaded into section pages
        if (data != null && data.pages != null) {
            for (PageData page : data.pages) {
                if (page.content instanceof ContentArmorMaterial) {
                    ContentArmorMaterial cam = (ContentArmorMaterial) page.content;
                    if (cam.materialName != null) {
                        Material m = TinkerRegistry.getMaterial(cam.materialName);
                        if (m != null && !m.isHidden() && added.add(m.getIdentifier())) {
                            rawArmorMaterials.add(m);
                        }
                    }
                } else if (page.content instanceof ContentMaterial) {
                    ContentMaterial cm = (ContentMaterial) page.content;
                    if (cm.materialName != null) {
                        Material m = TinkerRegistry.getMaterial(cm.materialName);
                        if (m != null && !m.isHidden() && added.add(m.getIdentifier())) {
                            rawArmorMaterials.add(m);
                        }
                    }
                }
            }
        }

        // 2. Query registry to ensure no valid armor materials are missed
        for (Material m : TinkerRegistry.getAllMaterials()) {
            if (isValidArmorMaterial(m) && added.add(m.getIdentifier())) {
                rawArmorMaterials.add(m);
            }
        }

        defaultArmorIndices.clear();
        for (int i = 0; i < rawArmorMaterials.size(); i++) {
            defaultArmorIndices.put(rawArmorMaterials.get(i).getIdentifier(), i);
        }

        // Load configured defaults
        if (ModConfig.rememberLastSort) {
            try {
                SortMode m = SortMode.valueOf(ModConfig.defaultArmorSortMode);
                if (m.isApplicable("armormaterials")) {
                    currentMode = m;
                }
            } catch (Exception ignored) {
                currentMode = SortMode.DEFAULT;
            }
            currentOrder = ModConfig.defaultArmorAscending ? SortOrder.ASCENDING : SortOrder.DESCENDING;
        }

        initialized = true;

        if (currentMode != SortMode.DEFAULT || currentOrder != SortOrder.ASCENDING) {
            rebuildArmorMaterials(book);
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

    public static String getCurrentCategory() {
        return "all";
    }

    public static void applySort(BookData book, SortMode mode, SortOrder order, String query) {
        if (!initialized && book != null) {
            init(book);
        }

        currentMode = mode != null ? mode : SortMode.DEFAULT;
        currentOrder = order != null ? order : SortOrder.ASCENDING;
        currentQuery = query == null ? "" : query.trim();

        if (ModConfig.rememberLastSort) {
            ModConfig.defaultArmorSortMode = currentMode.name();
            ModConfig.defaultArmorAscending = (currentOrder == SortOrder.ASCENDING);
            ModConfig.save();
        }

        rebuildArmorMaterials(book);
    }

    public static void setCategory(BookData book, String category) {
        // No-op: categories are not used for armor
    }

    public static void reset(BookData book) {
        currentMode = SortMode.DEFAULT;
        currentOrder = SortOrder.ASCENDING;
        currentQuery = "";

        if (ModConfig.rememberLastSort) {
            ModConfig.defaultArmorSortMode = "DEFAULT";
            ModConfig.defaultArmorAscending = true;
            ModConfig.save();
        }

        rebuildArmorMaterials(book);
    }

    public static void rebuildArmorMaterials(BookData book) {
        if (book == null) return;
        SectionData data = MaterialSectionManager.findSection(book, "materials");
        if (data == null) return;

        if (rawArmorMaterials.isEmpty()) {
            init(book);
        }

        data.pages.clear();

        List<Material> filtered = rawArmorMaterials.stream()
                .filter(m -> MaterialFilter.matches(m, currentQuery))
                .sorted(new MaterialComparator(currentMode, currentOrder, defaultArmorIndices))
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
            page.type = ContentArmorMaterial.ID;
            page.content = new ContentArmorMaterial(material);
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
}
