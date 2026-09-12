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
import slimeknights.mantle.client.gui.book.GuiBook;
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

    public static final List<String> BOW_MATERIAL_TYPES = ImmutableList.of(
            MaterialTypes.BOW, MaterialTypes.BOWSTRING, MaterialTypes.SHAFT, MaterialTypes.FLETCHING
    );

    private static List<Material> rawToolMaterials = new ArrayList<>();
    private static Map<String, Integer> defaultToolIndices = new HashMap<>();

    private static Map<String, List<Material>> rawBowMaterials = new HashMap<>();
    private static Map<String, Map<String, Integer>> defaultBowIndices = new HashMap<>();
    private static final Map<String, Integer> bowCategoryFirstPages = new HashMap<>();

    public static class SectionState {
        public SortMode mode;
        public SortOrder order;
        public String query;

        public SectionState(SortMode mode, SortOrder order, String query) {
            this.mode = mode != null ? mode : SortMode.DEFAULT;
            this.order = order != null ? order : SortOrder.ASCENDING;
            this.query = query != null ? query : "";
        }
    }

    private static final Map<String, SectionState> sectionStates = new HashMap<>();

    public static String normalizeSectionKey(String sectionName) {
        if (sectionName == null || sectionName.trim().isEmpty()) return "materials";
        String s = sectionName.trim().toLowerCase();
        if (s.startsWith("bowmaterials:")) {
            return s.substring("bowmaterials:".length());
        }
        return s;
    }

    public static boolean isBowCategory(String sectionName) {
        if (sectionName == null) return false;
        String s = normalizeSectionKey(sectionName);
        return BOW_MATERIAL_TYPES.contains(s) || "bowmaterials".equals(s);
    }

    public static SectionState getSectionState(String sectionName) {
        String key = normalizeSectionKey(sectionName);
        return sectionStates.computeIfAbsent(key, k -> new SectionState(SortMode.DEFAULT, SortOrder.ASCENDING, ""));
    }

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

        // Load configured defaults for tools
        SectionState toolState = getSectionState("materials");
        if (ModConfig.rememberLastSort) {
            loadConfiguredState("materials", ModConfig.defaultSortMode, ModConfig.defaultAscending);
        }

        // Load configured defaults for bows and subcategories
        if (ModConfig.rememberLastSort) {
            loadConfiguredState("bowmaterials", ModConfig.defaultBowSortMode, ModConfig.defaultBowAscending);
            loadConfiguredState(MaterialTypes.BOW, ModConfig.defaultBowSortMode, ModConfig.defaultBowAscending);
            loadConfiguredState(MaterialTypes.BOWSTRING, ModConfig.defaultBowstringSortMode, ModConfig.defaultBowstringAscending);
            loadConfiguredState(MaterialTypes.SHAFT, ModConfig.defaultShaftSortMode, ModConfig.defaultShaftAscending);
            loadConfiguredState(MaterialTypes.FLETCHING, ModConfig.defaultFletchingSortMode, ModConfig.defaultFletchingAscending);
        }

        initialized = true;

        if (toolState.mode != SortMode.DEFAULT || toolState.order != SortOrder.ASCENDING) {
            rebuildToolMaterials(book);
        }
        rebuildBowMaterials(book);
    }

    private static void loadConfiguredState(String section, String modeName, boolean ascending) {
        SectionState state = getSectionState(section);
        try {
            SortMode m = SortMode.valueOf(modeName);
            if (m.isApplicable(section)) {
                state.mode = m;
            }
        } catch (Exception ignored) {
            state.mode = SortMode.DEFAULT;
        }
        state.order = ascending ? SortOrder.ASCENDING : SortOrder.DESCENDING;
    }

    public static SortMode getCurrentMode(String sectionName) {
        return getSectionState(sectionName).mode;
    }

    public static SortOrder getCurrentOrder(String sectionName) {
        return getSectionState(sectionName).order;
    }

    public static String getCurrentQuery(String sectionName) {
        return getSectionState(sectionName).query;
    }

    public static void applySort(BookData book, String sectionName, SortMode mode, SortOrder order, String query) {
        if (!initialized) {
            if (book != null) {
                init(book);
            }
        }

        String key = normalizeSectionKey(sectionName);
        SectionState state = getSectionState(key);
        state.mode = mode;
        state.order = order;
        String trimmedQuery = query == null ? "" : query.trim();
        state.query = trimmedQuery;

        if (isBowCategory(key)) {
            // Keep bow query in sync across all bow categories
            getSectionState("bowmaterials").query = trimmedQuery;
            for (String t : BOW_MATERIAL_TYPES) {
                getSectionState(t).query = trimmedQuery;
            }

            if ("bowmaterials".equals(key)) {
                SectionState bowSub = getSectionState(MaterialTypes.BOW);
                bowSub.mode = mode;
                bowSub.order = order;
            }

            if (ModConfig.rememberLastSort) {
                if (MaterialTypes.BOW.equals(key) || "bowmaterials".equals(key)) {
                    ModConfig.defaultBowSortMode = mode.name();
                    ModConfig.defaultBowAscending = (order == SortOrder.ASCENDING);
                } else if (MaterialTypes.BOWSTRING.equals(key)) {
                    ModConfig.defaultBowstringSortMode = mode.name();
                    ModConfig.defaultBowstringAscending = (order == SortOrder.ASCENDING);
                } else if (MaterialTypes.SHAFT.equals(key)) {
                    ModConfig.defaultShaftSortMode = mode.name();
                    ModConfig.defaultShaftAscending = (order == SortOrder.ASCENDING);
                } else if (MaterialTypes.FLETCHING.equals(key)) {
                    ModConfig.defaultFletchingSortMode = mode.name();
                    ModConfig.defaultFletchingAscending = (order == SortOrder.ASCENDING);
                }
                ModConfig.save();
            }

            rebuildBowMaterials(book);
        } else {
            if (ModConfig.rememberLastSort) {
                ModConfig.defaultSortMode = mode.name();
                ModConfig.defaultAscending = (order == SortOrder.ASCENDING);
                ModConfig.save();
            }
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

        SectionState state = getSectionState("materials");
        List<Material> filtered = rawToolMaterials.stream()
                .filter(m -> MaterialFilter.matches(m, state.query))
                .sorted(new MaterialComparator(state.mode, state.order, defaultToolIndices))
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
        bowCategoryFirstPages.clear();

        SectionState overallState = getSectionState("bowmaterials");
        String searchQuery = overallState.query;

        ContentListing listing = new ContentListing();
        listing.title = book.translate("bowmaterials");

        PageData listingPage = new PageData(true);
        listingPage.source = data.source != null ? data.source : BookRepository.DUMMY;
        listingPage.parent = data;
        listingPage.name = "bowmaterials_toc";
        listingPage.type = "";
        listingPage.content = listing;
        listingPage.load();
        data.pages.add(listingPage);

        for (String type : BOW_MATERIAL_TYPES) {
            int pageIndex = data.pages.size();
            List<Material> list = rawBowMaterials.get(type);
            if (list == null) continue;

            SectionState typeState = getSectionState(type);
            SortMode modeToUse = typeState.mode;
            SortOrder orderToUse = typeState.order;
            if (modeToUse == SortMode.DEFAULT && overallState.mode != SortMode.DEFAULT && overallState.mode.isApplicable(type)) {
                modeToUse = overallState.mode;
                orderToUse = overallState.order;
            }

            String q = !typeState.query.isEmpty() ? typeState.query : searchQuery;

            List<Material> filtered = list.stream()
                    .filter(m -> MaterialFilter.matches(m, q))
                    .sorted(new MaterialComparator(modeToUse, orderToUse, defaultBowIndices.get(type)))
                    .collect(Collectors.toList());

            if (filtered.isEmpty()) continue;

            bowCategoryFirstPages.put(type, pageIndex);

            String statName = Material.UNKNOWN.getStats(type).getLocalizedName();
            List<ContentPageIconList> contentPages = ContentPageIconList.getPagesNeededForItemCount(filtered.size(), data, statName);
            int iconIdx = 0;
            for (int i = pageIndex; i < data.pages.size(); i++) {
                PageData p = data.pages.get(i);
                if (p.content instanceof ContentPageIconList) {
                    p.name = type + "_overview_" + (iconIdx++);
                }
            }

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

    public static Integer getBowCategoryPageIndex(String type) {
        if (type == null || "all".equalsIgnoreCase(type) || "bowmaterials".equalsIgnoreCase(type)) {
            Integer bowIdx = bowCategoryFirstPages.get(MaterialTypes.BOW);
            return bowIdx != null ? bowIdx : 1;
        }
        Integer idx = bowCategoryFirstPages.get(normalizeSectionKey(type));
        if (idx == null) {
            Integer bowIdx = bowCategoryFirstPages.get(MaterialTypes.BOW);
            return bowIdx != null ? bowIdx : 1;
        }
        return idx;
    }

    public static void navigateToBowCategory(GuiBook guiBook, String type) {
        if (guiBook == null || guiBook.book == null) return;
        ensureSectionsInitialized(guiBook.book);
        SectionData section = findSection(guiBook.book, "bowmaterials");
        if (section == null) return;
        Integer relIndex = getBowCategoryPageIndex(type);
        if (relIndex == null) return;
        int firstPageNum = guiBook.book.getFirstPageNumber(section, guiBook.advancementCache);
        if (firstPageNum >= 0) {
            guiBook.openPage(firstPageNum + relIndex);
            guiBook.updateScreen();
        }
    }

    public static String detectCurrentBowCategory(GuiBook guiBook, String preferredCategory) {
        if (guiBook == null || guiBook.book == null) return null;
        ensureSectionsInitialized(guiBook.book);
        int page = guiBook.getPage_();
        if (page < 0) return null;

        if (page == 0) {
            PageData p = guiBook.book.findPage(0, guiBook.advancementCache);
            String type = getBowTypeForPage(p);
            if (type != null) return type;
            if (p != null && p.content instanceof ContentListing) {
                if (preferredCategory != null && isBowCategory(preferredCategory) && !"all".equalsIgnoreCase(preferredCategory)) {
                    return preferredCategory;
                }
                return MaterialTypes.BOW;
            }
            return null;
        }

        int leftPageNum = (page - 1) * 2 + 1;
        PageData leftPage = guiBook.book.findPage(leftPageNum, guiBook.advancementCache);

        int rightPageNum = (page - 1) * 2 + 2;
        PageData rightPage = guiBook.book.findPage(rightPageNum, guiBook.advancementCache);

        return arbitrateBowCategory(leftPage, rightPage, preferredCategory);
    }

    public static String arbitrateBowCategory(PageData leftPage, PageData rightPage, String preferredCategory) {
        String leftType = getBowTypeForPage(leftPage);
        String rightType = getBowTypeForPage(rightPage);

        if (leftType == null && rightType == null) {
            if ((leftPage != null && leftPage.content instanceof ContentListing) ||
                (rightPage != null && rightPage.content instanceof ContentListing)) {
                if (preferredCategory != null && isBowCategory(preferredCategory) && !"all".equalsIgnoreCase(preferredCategory)) {
                    return preferredCategory;
                }
                return MaterialTypes.BOW;
            }
            return null;
        }

        if (leftType != null && rightType == null) return leftType;
        if (leftType == null && rightType != null) return rightType;
        if (leftType.equalsIgnoreCase(rightType)) return leftType;

        // Both pages have bow categories and they differ (Split Spread)
        // Tier 1: Context continuity (user's preferred / active category)
        if (preferredCategory != null && !preferredCategory.isEmpty() && !"all".equalsIgnoreCase(preferredCategory)) {
            if (preferredCategory.equalsIgnoreCase(leftType)) {
                return leftType;
            }
            if (preferredCategory.equalsIgnoreCase(rightType)) {
                return rightType;
            }
        }

        // Tier 2: Content structural weight: Overview/Icon list page takes precedence over trailing detail page
        boolean leftIsIconList = leftPage != null && leftPage.content instanceof ContentPageIconList;
        boolean rightIsIconList = rightPage != null && rightPage.content instanceof ContentPageIconList;
        if (rightIsIconList && !leftIsIconList) {
            return rightType;
        }
        if (leftIsIconList && !rightIsIconList) {
            return leftType;
        }

        // Fallback: Natural left-to-right reading order
        return leftType;
    }

    public static String getBowTypeForPage(PageData page) {
        if (page == null) return null;
        if (page.content instanceof ContentSingleStatMultMaterial) {
            return ((ContentSingleStatMultMaterial) page.content).materialType;
        }
        if (page.content instanceof ContentPageIconList) {
            String pageTitle = ((ContentPageIconList) page.content).title;
            if (pageTitle != null) {
                for (String type : new String[]{MaterialTypes.BOWSTRING, MaterialTypes.SHAFT, MaterialTypes.FLETCHING, MaterialTypes.BOW}) {
                    try {
                        String statName = Material.UNKNOWN.getStats(type).getLocalizedName();
                        if (pageTitle.equalsIgnoreCase(statName)) {
                            return type;
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        if (page.name != null) {
            // Check longer types first, or match exact/underscore-separated prefix
            if (page.name.equals(MaterialTypes.BOWSTRING) || page.name.startsWith(MaterialTypes.BOWSTRING + "_")) {
                return MaterialTypes.BOWSTRING;
            }
            if (page.name.equals(MaterialTypes.SHAFT) || page.name.startsWith(MaterialTypes.SHAFT + "_")) {
                return MaterialTypes.SHAFT;
            }
            if (page.name.equals(MaterialTypes.FLETCHING) || page.name.startsWith(MaterialTypes.FLETCHING + "_")) {
                return MaterialTypes.FLETCHING;
            }
            if (page.name.equals(MaterialTypes.BOW) || page.name.startsWith(MaterialTypes.BOW + "_")) {
                return MaterialTypes.BOW;
            }
        }
        return null;
    }
    public static void resetAllBowCategories() {
        resetAllBowCategories(null);
    }

    public static void resetAllBowCategories(BookData book) {
        getSectionState("bowmaterials").mode = SortMode.DEFAULT;
        getSectionState("bowmaterials").order = SortOrder.ASCENDING;
        getSectionState("bowmaterials").query = "";

        for (String type : BOW_MATERIAL_TYPES) {
            SectionState s = getSectionState(type);
            s.mode = SortMode.DEFAULT;
            s.order = SortOrder.ASCENDING;
            s.query = "";
        }

        if (ModConfig.rememberLastSort) {
            ModConfig.defaultBowSortMode = "DEFAULT";
            ModConfig.defaultBowAscending = true;
            ModConfig.defaultBowstringSortMode = "DEFAULT";
            ModConfig.defaultBowstringAscending = true;
            ModConfig.defaultShaftSortMode = "DEFAULT";
            ModConfig.defaultShaftAscending = true;
            ModConfig.defaultFletchingSortMode = "DEFAULT";
            ModConfig.defaultFletchingAscending = true;
            ModConfig.save();
        }

        rebuildBowMaterials(book);
    }
}

