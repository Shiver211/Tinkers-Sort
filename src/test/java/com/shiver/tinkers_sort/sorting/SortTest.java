package com.shiver.tinkers_sort.sorting;

import com.shiver.tinkers_sort.book.MaterialSectionManager;
import com.shiver.tinkers_sort.integration.JechHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SortTest {

    @BeforeAll
    public static void setUpForgeMock() {
        try {
            net.minecraftforge.fml.common.IFMLSidedHandler handler = (net.minecraftforge.fml.common.IFMLSidedHandler) java.lang.reflect.Proxy.newProxyInstance(
                    SortTest.class.getClassLoader(),
                    new Class<?>[]{net.minecraftforge.fml.common.IFMLSidedHandler.class},
                    (proxy, method, args) -> {
                        if ("getSide".equals(method.getName())) {
                            return net.minecraftforge.fml.relauncher.Side.CLIENT;
                        }
                        return null;
                    }
            );
            java.lang.reflect.Field f = net.minecraftforge.fml.common.FMLCommonHandler.class.getDeclaredField("sidedDelegate");
            f.setAccessible(true);
            f.set(net.minecraftforge.fml.common.FMLCommonHandler.instance(), handler);

            net.minecraftforge.fml.common.Loader loader = net.minecraftforge.fml.common.Loader.instance();
            java.lang.reflect.Field lf = net.minecraftforge.fml.common.Loader.class.getDeclaredField("modController");
            lf.setAccessible(true);
            net.minecraftforge.fml.common.LoadController lc = (net.minecraftforge.fml.common.LoadController) lf.get(loader);
            if (lc == null) {
                lc = new net.minecraftforge.fml.common.LoadController(loader);
                lf.set(loader, lc);
            }
            net.minecraftforge.fml.common.ModContainer container = (net.minecraftforge.fml.common.ModContainer) java.lang.reflect.Proxy.newProxyInstance(
                    SortTest.class.getClassLoader(),
                    new Class<?>[]{net.minecraftforge.fml.common.ModContainer.class},
                    (proxy, method, args) -> {
                        if ("getModId".equals(method.getName())) {
                            return "conarm";
                        }
                        return null;
                    }
            );
            loader.setActiveModContainer(container);

            if (!net.minecraft.init.Bootstrap.isRegistered()) {
                net.minecraft.init.Bootstrap.register();
            }
        } catch (Throwable ignored) {}
    }

    @Test
    public void testSortOrderToggle() {
        assertEquals(SortOrder.DESCENDING, SortOrder.ASCENDING.toggle());
        assertEquals(SortOrder.ASCENDING, SortOrder.DESCENDING.toggle());
    }

    @Test
    public void testApplicableModes() {
        List<SortMode> toolModes = SortMode.getApplicableModes("materials");
        assertTrue(toolModes.contains(SortMode.DEFAULT));
        assertTrue(toolModes.contains(SortMode.NAME));
        assertTrue(toolModes.contains(SortMode.HARVEST_LEVEL));
        assertTrue(toolModes.contains(SortMode.DURABILITY));
        assertTrue(toolModes.contains(SortMode.HANDLE_MODIFIER));
        assertFalse(toolModes.contains(SortMode.DRAW_SPEED));

        List<SortMode> bowModes = SortMode.getApplicableModes("bowmaterials");
        assertTrue(bowModes.contains(SortMode.DEFAULT));
        assertTrue(bowModes.contains(SortMode.NAME));
        assertTrue(bowModes.contains(SortMode.DRAW_SPEED));
        assertTrue(bowModes.contains(SortMode.RANGE));
        assertFalse(bowModes.contains(SortMode.HARVEST_LEVEL));
        assertFalse(bowModes.contains(SortMode.HANDLE_MODIFIER));
    }

    @Test
    public void testSortModeCycle() {
        SortMode current = SortMode.DEFAULT;
        SortMode next = current.next("materials");
        assertNotEquals(current, next);

        SortMode prev = next.previous("materials");
        assertEquals(current, prev);
    }

    @Test
    public void testEmptyQueryFilter() {
        assertTrue(MaterialFilter.matches(null, ""));
        assertTrue(MaterialFilter.matches(null, "   "));
        assertTrue(MaterialFilter.matches(null, null));
    }

    @Test
    public void testJechHelperWithJechEnabled() {
        JechHelper.setForceLoadedForTest(true);

        assertTrue(JechHelper.isJechLoaded());
        assertTrue(JechHelper.contains("钴", "gu"));
        assertTrue(JechHelper.contains("钴", "g"));
        assertTrue(JechHelper.contains("黑曜石", "hys"));
        assertTrue(JechHelper.contains("黑曜石", "heiyaoshi"));
        assertTrue(JechHelper.contains("生铁", "st"));
        assertTrue(JechHelper.contains("铁", "tie"));
        assertFalse(JechHelper.contains("蓝色粘液", "gu"));
        assertFalse(JechHelper.contains("绿色粘液", "gu"));

        assertEquals("gu", JechHelper.toPinyin("钴"));
        assertEquals("gu", JechHelper.toPinyin("§9钴§r"));
        assertEquals("suishi", JechHelper.toPinyin("燧石"));
        assertEquals("gang", JechHelper.toPinyin("钢"));
        assertEquals("gutou", JechHelper.toPinyin("骨头"));
        assertEquals("zhi", JechHelper.toPinyin("纸"));
        assertEquals("zhutie", JechHelper.toPinyin("猪铁"));
    }

    @Test
    public void testJechSortingOrder() {
        JechHelper.setForceLoadedForTest(true);

        java.util.List<String> names = java.util.Arrays.asList(
                "纸", "猪铁", "木头", "铁", "石", "燧石", "钢", "骨头", "钴", "仙人掌"
        );
        java.text.Collator collator = java.text.Collator.getInstance(java.util.Locale.CHINA);
        names.sort((s1, s2) -> JechHelper.compare(s1, s2, collator));

        // Alphabetical by JECH Pinyin:
        // gang (钢), gu (钴), gutou (骨头), mutou (木头), shi (石), suishi (燧石), tie (铁), xianrenzhang (仙人掌), zhi (纸), zhutie (猪铁)
        assertEquals("钢", names.get(0));
        assertEquals("钴", names.get(1));
        assertEquals("骨头", names.get(2));
        assertEquals("木头", names.get(3));
        assertEquals("石", names.get(4));
        assertEquals("燧石", names.get(5));
        assertEquals("铁", names.get(6));
        assertEquals("仙人掌", names.get(7));
        assertEquals("纸", names.get(8));
        assertEquals("猪铁", names.get(9));
    }

    @Test
    public void testJechHelperFallback() {
        JechHelper.setForceLoadedForTest(false);

        assertFalse(JechHelper.isJechLoaded());
        assertTrue(JechHelper.contains("Cobalt", "cob"));
        assertFalse(JechHelper.contains("Cobalt", "iron"));
        assertEquals("Cobalt", JechHelper.toPinyin("Cobalt"));
    }

    @Test
    public void testIndependentSectionStates() {
        MaterialSectionManager.applySort(null, "materials", SortMode.NAME, SortOrder.DESCENDING, "铁");
        MaterialSectionManager.applySort(null, "bowmaterials", SortMode.DRAW_SPEED, SortOrder.ASCENDING, "木");

        assertEquals(SortMode.NAME, MaterialSectionManager.getCurrentMode("materials"));
        assertEquals(SortOrder.DESCENDING, MaterialSectionManager.getCurrentOrder("materials"));
        assertEquals("铁", MaterialSectionManager.getCurrentQuery("materials"));

        assertEquals(SortMode.DRAW_SPEED, MaterialSectionManager.getCurrentMode("bowmaterials"));
        assertEquals(SortOrder.ASCENDING, MaterialSectionManager.getCurrentOrder("bowmaterials"));
        assertEquals("木", MaterialSectionManager.getCurrentQuery("bowmaterials"));

        // Changing materials must not affect bowmaterials
        MaterialSectionManager.applySort(null, "materials", SortMode.DURABILITY, SortOrder.ASCENDING, "");
        assertEquals(SortMode.DURABILITY, MaterialSectionManager.getCurrentMode("materials"));
        assertEquals("", MaterialSectionManager.getCurrentQuery("materials"));

        assertEquals(SortMode.DRAW_SPEED, MaterialSectionManager.getCurrentMode("bowmaterials"));
        assertEquals("木", MaterialSectionManager.getCurrentQuery("bowmaterials"));
    }

    @Test
    public void testBowCategoriesApplicableModes() {
        List<SortMode> bowModes = SortMode.getApplicableModes("bow");
        assertTrue(bowModes.contains(SortMode.DEFAULT));
        assertTrue(bowModes.contains(SortMode.NAME));
        assertTrue(bowModes.contains(SortMode.DRAW_SPEED));
        assertTrue(bowModes.contains(SortMode.RANGE));
        assertTrue(bowModes.contains(SortMode.BONUS_DAMAGE));
        assertTrue(bowModes.contains(SortMode.DURABILITY));
        assertFalse(bowModes.contains(SortMode.BOWSTRING_MODIFIER));
        assertFalse(bowModes.contains(SortMode.SHAFT_MODIFIER));
        assertFalse(bowModes.contains(SortMode.FLETCHING_MODIFIER));

        List<SortMode> stringModes = SortMode.getApplicableModes("bowstring");
        assertEquals(3, stringModes.size());
        assertTrue(stringModes.contains(SortMode.DEFAULT));
        assertTrue(stringModes.contains(SortMode.NAME));
        assertTrue(stringModes.contains(SortMode.BOWSTRING_MODIFIER));
        assertFalse(stringModes.contains(SortMode.DRAW_SPEED));
        assertFalse(stringModes.contains(SortMode.BONUS_AMMO));

        List<SortMode> shaftModes = SortMode.getApplicableModes("shaft");
        assertEquals(4, shaftModes.size());
        assertTrue(shaftModes.contains(SortMode.DEFAULT));
        assertTrue(shaftModes.contains(SortMode.NAME));
        assertTrue(shaftModes.contains(SortMode.SHAFT_MODIFIER));
        assertTrue(shaftModes.contains(SortMode.BONUS_AMMO));
        assertFalse(shaftModes.contains(SortMode.BOWSTRING_MODIFIER));
        assertFalse(shaftModes.contains(SortMode.ACCURACY));

        List<SortMode> fletchingModes = SortMode.getApplicableModes("fletching");
        assertEquals(4, fletchingModes.size());
        assertTrue(fletchingModes.contains(SortMode.DEFAULT));
        assertTrue(fletchingModes.contains(SortMode.NAME));
        assertTrue(fletchingModes.contains(SortMode.ACCURACY));
        assertTrue(fletchingModes.contains(SortMode.FLETCHING_MODIFIER));
        assertFalse(fletchingModes.contains(SortMode.BONUS_AMMO));
        assertFalse(fletchingModes.contains(SortMode.BOWSTRING_MODIFIER));
    }

    @Test
    public void testBowCategoryCycles() {
        for (String cat : new String[]{"bow", "bowstring", "shaft", "fletching"}) {
            List<SortMode> modes = SortMode.getApplicableModes(cat);
            for (SortMode mode : modes) {
                SortMode next = mode.next(cat);
                assertTrue(modes.contains(next), "Next mode should belong to " + cat);
                SortMode prev = next.previous(cat);
                assertEquals(mode, prev, "Previous of next should be current mode in " + cat);
            }
        }
    }

    @Test
    public void testBowCategoryNormalization() {
        assertEquals("bowmaterials", MaterialSectionManager.normalizeSectionKey("bowmaterials"));
        assertEquals("bow", MaterialSectionManager.normalizeSectionKey("bowmaterials:bow"));
        assertEquals("bow", MaterialSectionManager.normalizeSectionKey("bow"));
        assertEquals("bowstring", MaterialSectionManager.normalizeSectionKey("bowstring"));
        assertEquals("shaft", MaterialSectionManager.normalizeSectionKey("shaft"));
        assertEquals("fletching", MaterialSectionManager.normalizeSectionKey("fletching"));
        assertEquals("materials", MaterialSectionManager.normalizeSectionKey("materials"));
        assertTrue(MaterialSectionManager.isBowCategory("bow"));
        assertTrue(MaterialSectionManager.isBowCategory("bowmaterials"));
        assertTrue(MaterialSectionManager.isBowCategory("bowstring"));
        assertTrue(MaterialSectionManager.isBowCategory("shaft"));
        assertTrue(MaterialSectionManager.isBowCategory("fletching"));
        assertFalse(MaterialSectionManager.isBowCategory("materials"));
    }

    @Test
    public void testIndependentBowSubcategoryStates() {
        MaterialSectionManager.applySort(null, "bow", SortMode.DRAW_SPEED, SortOrder.ASCENDING, "木");
        MaterialSectionManager.applySort(null, "bowstring", SortMode.BOWSTRING_MODIFIER, SortOrder.DESCENDING, "线");
        MaterialSectionManager.applySort(null, "shaft", SortMode.BONUS_AMMO, SortOrder.ASCENDING, "竹");
        MaterialSectionManager.applySort(null, "fletching", SortMode.ACCURACY, SortOrder.DESCENDING, "羽");

        // Independent sort modes
        assertEquals(SortMode.DRAW_SPEED, MaterialSectionManager.getCurrentMode("bow"));
        assertEquals(SortOrder.ASCENDING, MaterialSectionManager.getCurrentOrder("bow"));

        assertEquals(SortMode.BOWSTRING_MODIFIER, MaterialSectionManager.getCurrentMode("bowstring"));
        assertEquals(SortOrder.DESCENDING, MaterialSectionManager.getCurrentOrder("bowstring"));

        assertEquals(SortMode.BONUS_AMMO, MaterialSectionManager.getCurrentMode("shaft"));
        assertEquals(SortOrder.ASCENDING, MaterialSectionManager.getCurrentOrder("shaft"));

        assertEquals(SortMode.ACCURACY, MaterialSectionManager.getCurrentMode("fletching"));
        assertEquals(SortOrder.DESCENDING, MaterialSectionManager.getCurrentOrder("fletching"));

        // Search query is synchronized across all bow categories
        assertEquals("羽", MaterialSectionManager.getCurrentQuery("bow"));
        assertEquals("羽", MaterialSectionManager.getCurrentQuery("bowstring"));
        assertEquals("羽", MaterialSectionManager.getCurrentQuery("shaft"));
        assertEquals("羽", MaterialSectionManager.getCurrentQuery("fletching"));

        // Reset all bow categories
        MaterialSectionManager.resetAllBowCategories();
        assertEquals(SortOrder.ASCENDING, MaterialSectionManager.getCurrentOrder("bow"));
        assertEquals(SortOrder.ASCENDING, MaterialSectionManager.getCurrentOrder("bowstring"));
        assertEquals(SortOrder.ASCENDING, MaterialSectionManager.getCurrentOrder("shaft"));
        assertEquals(SortOrder.ASCENDING, MaterialSectionManager.getCurrentOrder("fletching"));
        assertEquals("", MaterialSectionManager.getCurrentQuery("bow"));
        assertEquals("", MaterialSectionManager.getCurrentQuery("bowstring"));
        assertEquals("", MaterialSectionManager.getCurrentQuery("shaft"));
        assertEquals("", MaterialSectionManager.getCurrentQuery("fletching"));
    }

    @Test
    public void testBowCategoryPageIndices() {
        assertEquals(Integer.valueOf(1), MaterialSectionManager.getBowCategoryPageIndex("all"));
        assertEquals(Integer.valueOf(1), MaterialSectionManager.getBowCategoryPageIndex("bowmaterials"));
        assertEquals(Integer.valueOf(1), MaterialSectionManager.getBowCategoryPageIndex("bow"));
        assertEquals(Integer.valueOf(1), MaterialSectionManager.getBowCategoryPageIndex(null));
    }

    @Test
    public void testBowTypeForPageDetection() {
        slimeknights.mantle.client.book.data.PageData bowOverview = new slimeknights.mantle.client.book.data.PageData();
        bowOverview.name = "bow_overview_0";
        assertEquals("bow", MaterialSectionManager.getBowTypeForPage(bowOverview));

        slimeknights.mantle.client.book.data.PageData bowDetail = new slimeknights.mantle.client.book.data.PageData();
        bowDetail.name = "bow_wood_stone";
        assertEquals("bow", MaterialSectionManager.getBowTypeForPage(bowDetail));

        slimeknights.mantle.client.book.data.PageData stringOverview = new slimeknights.mantle.client.book.data.PageData();
        stringOverview.name = "bowstring_overview_0";
        assertEquals("bowstring", MaterialSectionManager.getBowTypeForPage(stringOverview));

        slimeknights.mantle.client.book.data.PageData stringDetail = new slimeknights.mantle.client.book.data.PageData();
        stringDetail.name = "bowstring_string_vine";
        assertEquals("bowstring", MaterialSectionManager.getBowTypeForPage(stringDetail));

        slimeknights.mantle.client.book.data.PageData shaftOverview = new slimeknights.mantle.client.book.data.PageData();
        shaftOverview.name = "shaft_overview_0";
        assertEquals("shaft", MaterialSectionManager.getBowTypeForPage(shaftOverview));

        slimeknights.mantle.client.book.data.PageData fletchingOverview = new slimeknights.mantle.client.book.data.PageData();
        fletchingOverview.name = "fletching_overview_0";
        assertEquals("fletching", MaterialSectionManager.getBowTypeForPage(fletchingOverview));
    }

    @Test
    public void testSplitSpreadCategoryArbitration() {
        slimeknights.mantle.client.book.data.PageData stringDetail = new slimeknights.mantle.client.book.data.PageData();
        stringDetail.name = "bowstring_detail_page";

        slimeknights.mantle.client.book.data.PageData fletchingOverview = new slimeknights.mantle.client.book.data.PageData();
        fletchingOverview.name = "fletching_overview_0";
        fletchingOverview.content = new slimeknights.tconstruct.library.book.content.ContentPageIconList();

        // 1. When user preferredCategory matches one of the sides, it should stick to it
        assertEquals("bowstring", MaterialSectionManager.arbitrateBowCategory(stringDetail, fletchingOverview, "bowstring"));
        assertEquals("fletching", MaterialSectionManager.arbitrateBowCategory(stringDetail, fletchingOverview, "fletching"));

        // 2. When preferredCategory is "all" or null, ContentPageIconList wins over detail page
        assertEquals("fletching", MaterialSectionManager.arbitrateBowCategory(stringDetail, fletchingOverview, "all"));
        assertEquals("fletching", MaterialSectionManager.arbitrateBowCategory(stringDetail, fletchingOverview, null));

        // 3. When neither is ContentPageIconList, fallback to left page
        slimeknights.mantle.client.book.data.PageData fletchingDetail = new slimeknights.mantle.client.book.data.PageData();
        fletchingDetail.name = "fletching_feather";
        assertEquals("bowstring", MaterialSectionManager.arbitrateBowCategory(stringDetail, fletchingDetail, null));

        // 4. When both pages are the same category, returns that category
        slimeknights.mantle.client.book.data.PageData stringOverview = new slimeknights.mantle.client.book.data.PageData();
        stringOverview.name = "bowstring_overview_0";
        assertEquals("bowstring", MaterialSectionManager.arbitrateBowCategory(stringOverview, stringDetail, "fletching"));

        // 5. One page is null
        assertEquals("bowstring", MaterialSectionManager.arbitrateBowCategory(stringDetail, null, null));
        assertEquals("fletching", MaterialSectionManager.arbitrateBowCategory(null, fletchingOverview, null));

        // 6. TOC listing page
        slimeknights.mantle.client.book.data.PageData tocPage = new slimeknights.mantle.client.book.data.PageData();
        tocPage.content = new slimeknights.tconstruct.library.book.content.ContentListing();
        assertEquals("bow", MaterialSectionManager.arbitrateBowCategory(tocPage, null, null));
        assertEquals("shaft", MaterialSectionManager.arbitrateBowCategory(tocPage, null, "shaft"));
    }

    @Test
    public void testArmorSortModesApplicable() {
        List<SortMode> allArmorModes = SortMode.getApplicableModes("armormaterials");
        assertTrue(allArmorModes.contains(SortMode.DEFAULT));
        assertTrue(allArmorModes.contains(SortMode.NAME));
        assertTrue(allArmorModes.contains(SortMode.ARMOR_DEFENSE));
        assertTrue(allArmorModes.contains(SortMode.ARMOR_TOUGHNESS));
        assertTrue(allArmorModes.contains(SortMode.ARMOR_DURABILITY));
        assertTrue(allArmorModes.contains(SortMode.PLATES_DURABILITY));
        assertTrue(allArmorModes.contains(SortMode.PLATES_MODIFIER));
        assertTrue(allArmorModes.contains(SortMode.TRIM_EXTRA_DURABILITY));
        assertFalse(allArmorModes.contains(SortMode.HARVEST_LEVEL));
        assertFalse(allArmorModes.contains(SortMode.MINING_SPEED));
        assertFalse(allArmorModes.contains(SortMode.ATTACK_DAMAGE));
        assertFalse(allArmorModes.contains(SortMode.HANDLE_MODIFIER));
        assertFalse(allArmorModes.contains(SortMode.DRAW_SPEED));
        assertFalse(allArmorModes.contains(SortMode.RANGE));

        List<SortMode> toolModes = SortMode.getApplicableModes("materials");
        assertFalse(toolModes.contains(SortMode.ARMOR_DEFENSE));
        assertFalse(toolModes.contains(SortMode.ARMOR_TOUGHNESS));
        assertFalse(toolModes.contains(SortMode.ARMOR_DURABILITY));
        assertFalse(toolModes.contains(SortMode.PLATES_DURABILITY));
        assertFalse(toolModes.contains(SortMode.PLATES_MODIFIER));
        assertFalse(toolModes.contains(SortMode.TRIM_EXTRA_DURABILITY));
    }

    @Test
    public void testArmorModeCycles() {
        List<SortMode> modes = SortMode.getApplicableModes("armormaterials");
        assertEquals(8, modes.size());
        for (SortMode mode : modes) {
            SortMode next = mode.next("armormaterials");
            assertTrue(modes.contains(next), "Next mode should belong to armor modes");
            SortMode prev = next.previous("armormaterials");
            assertEquals(mode, prev, "Previous of next should be current mode in armormaterials");
        }
    }

    @Test
    public void testArmorySectionManagerState() {
        com.shiver.tinkers_sort.book.ArmorySectionManager.applySort(null, SortMode.ARMOR_DEFENSE, SortOrder.DESCENDING, "铁");
        assertEquals(SortMode.ARMOR_DEFENSE, com.shiver.tinkers_sort.book.ArmorySectionManager.getCurrentMode());
        assertEquals(SortOrder.DESCENDING, com.shiver.tinkers_sort.book.ArmorySectionManager.getCurrentOrder());
        assertEquals("铁", com.shiver.tinkers_sort.book.ArmorySectionManager.getCurrentQuery());

        // Reset
        com.shiver.tinkers_sort.book.ArmorySectionManager.reset(null);
        assertEquals(SortMode.DEFAULT, com.shiver.tinkers_sort.book.ArmorySectionManager.getCurrentMode());
        assertEquals(SortOrder.ASCENDING, com.shiver.tinkers_sort.book.ArmorySectionManager.getCurrentOrder());
        assertEquals("", com.shiver.tinkers_sort.book.ArmorySectionManager.getCurrentQuery());
    }

    @Test
    public void testArmoryIntegration() {
        com.shiver.tinkers_sort.integration.ArmoryIntegration.setForceLoadedForTest(false);
        assertFalse(com.shiver.tinkers_sort.integration.ArmoryIntegration.isLoaded());
        assertFalse(com.shiver.tinkers_sort.integration.ArmoryIntegration.isArmoryBook(null));

        com.shiver.tinkers_sort.integration.ArmoryIntegration.setForceLoadedForTest(true);
        assertTrue(com.shiver.tinkers_sort.integration.ArmoryIntegration.isLoaded());
        assertTrue(com.shiver.tinkers_sort.integration.ArmoryIntegration.isArmoryBook(c4.conarm.lib.book.ArmoryBook.INSTANCE));
    }

    @Test
    public void testArmorMaterialComparator() {
        slimeknights.tconstruct.library.materials.Material m1 = new slimeknights.tconstruct.library.materials.Material("mat1", 0xFFFFFF);
        m1.addStats(new c4.conarm.lib.materials.CoreMaterialStats(100f, 15f));
        m1.addStats(new c4.conarm.lib.materials.PlatesMaterialStats(1.2f, 80f, 3f));
        m1.addStats(new c4.conarm.lib.materials.TrimMaterialStats(50f));

        slimeknights.tconstruct.library.materials.Material m2 = new slimeknights.tconstruct.library.materials.Material("mat2", 0xAAAAAA);
        m2.addStats(new c4.conarm.lib.materials.CoreMaterialStats(200f, 10f));
        m2.addStats(new c4.conarm.lib.materials.PlatesMaterialStats(0.8f, 120f, 1f));
        m2.addStats(new c4.conarm.lib.materials.TrimMaterialStats(30f));

        // Test Defense
        MaterialComparator compDefenseAsc = new MaterialComparator(SortMode.ARMOR_DEFENSE, SortOrder.ASCENDING);
        assertTrue(compDefenseAsc.compare(m1, m2) > 0);
        MaterialComparator compDefenseDesc = new MaterialComparator(SortMode.ARMOR_DEFENSE, SortOrder.DESCENDING);
        assertTrue(compDefenseDesc.compare(m1, m2) < 0);

        // Test Toughness
        MaterialComparator compToughness = new MaterialComparator(SortMode.ARMOR_TOUGHNESS, SortOrder.DESCENDING);
        assertTrue(compToughness.compare(m1, m2) < 0);

        // Test Core Durability
        MaterialComparator compDurability = new MaterialComparator(SortMode.ARMOR_DURABILITY, SortOrder.DESCENDING);
        assertTrue(compDurability.compare(m1, m2) > 0);

        // Test Plates Durability
        MaterialComparator compPlatesDur = new MaterialComparator(SortMode.PLATES_DURABILITY, SortOrder.DESCENDING);
        assertTrue(compPlatesDur.compare(m1, m2) > 0);

        // Test Plates Modifier
        MaterialComparator compPlatesMod = new MaterialComparator(SortMode.PLATES_MODIFIER, SortOrder.DESCENDING);
        assertTrue(compPlatesMod.compare(m1, m2) < 0);

        // Test Trim Extra Durability
        MaterialComparator compTrim = new MaterialComparator(SortMode.TRIM_EXTRA_DURABILITY, SortOrder.DESCENDING);
        assertTrue(compTrim.compare(m1, m2) < 0);
    }

    @Test
    public void testArmorMaterialsUnifiedWithoutCategory() {
        // Material with only core stats
        slimeknights.tconstruct.library.materials.Material coreOnly = new slimeknights.tconstruct.library.materials.Material("core_mat", 0x111111);
        coreOnly.addStats(new c4.conarm.lib.materials.CoreMaterialStats(100f, 15f));

        // Material with only plates stats
        slimeknights.tconstruct.library.materials.Material platesOnly = new slimeknights.tconstruct.library.materials.Material("plates_mat", 0x222222);
        platesOnly.addStats(new c4.conarm.lib.materials.PlatesMaterialStats(1.0f, 100f, 2f));

        // Material with only trim stats
        slimeknights.tconstruct.library.materials.Material trimOnly = new slimeknights.tconstruct.library.materials.Material("trim_mat", 0x333333);
        trimOnly.addStats(new c4.conarm.lib.materials.TrimMaterialStats(40f));

        // Material with no armor stats
        slimeknights.tconstruct.library.materials.Material noArmor = new slimeknights.tconstruct.library.materials.Material("tool_only", 0x444444);

        assertTrue(com.shiver.tinkers_sort.book.ArmorySectionManager.isValidArmorMaterial(coreOnly));
        assertTrue(com.shiver.tinkers_sort.book.ArmorySectionManager.isValidArmorMaterial(platesOnly));
        assertTrue(com.shiver.tinkers_sort.book.ArmorySectionManager.isValidArmorMaterial(trimOnly));
        assertFalse(com.shiver.tinkers_sort.book.ArmorySectionManager.isValidArmorMaterial(noArmor));
    }

    @Test
    public void testInitLoadsDefaultsWhenRememberLastSortIsFalse() {
        boolean origRemember = com.shiver.tinkers_sort.config.ModConfig.rememberLastSort;
        String origToolMode = com.shiver.tinkers_sort.config.ModConfig.defaultSortMode;
        boolean origToolAsc = com.shiver.tinkers_sort.config.ModConfig.defaultAscending;
        String origBowMode = com.shiver.tinkers_sort.config.ModConfig.defaultBowSortMode;
        boolean origBowAsc = com.shiver.tinkers_sort.config.ModConfig.defaultBowAscending;
        String origArmorMode = com.shiver.tinkers_sort.config.ModConfig.defaultArmorSortMode;
        boolean origArmorAsc = com.shiver.tinkers_sort.config.ModConfig.defaultArmorAscending;

        try {
            com.shiver.tinkers_sort.config.ModConfig.rememberLastSort = false;
            com.shiver.tinkers_sort.config.ModConfig.defaultSortMode = "NAME";
            com.shiver.tinkers_sort.config.ModConfig.defaultAscending = false;
            com.shiver.tinkers_sort.config.ModConfig.defaultBowSortMode = "DRAW_SPEED";
            com.shiver.tinkers_sort.config.ModConfig.defaultBowAscending = false;
            com.shiver.tinkers_sort.config.ModConfig.defaultArmorSortMode = "ARMOR_DEFENSE";
            com.shiver.tinkers_sort.config.ModConfig.defaultArmorAscending = false;

            slimeknights.mantle.client.book.data.BookData book = new slimeknights.mantle.client.book.data.BookData();
            slimeknights.mantle.client.book.data.SectionData toolSec = new slimeknights.mantle.client.book.data.SectionData();
            toolSec.name = "materials";
            book.sections.add(toolSec);

            slimeknights.mantle.client.book.data.SectionData bowSec = new slimeknights.mantle.client.book.data.SectionData();
            bowSec.name = "bowmaterials";
            book.sections.add(bowSec);

            slimeknights.mantle.client.book.data.SectionData armorSec = new slimeknights.mantle.client.book.data.SectionData();
            armorSec.name = "armormaterials";
            book.sections.add(armorSec);

            MaterialSectionManager.init(book);
            com.shiver.tinkers_sort.book.ArmorySectionManager.init(book);

            assertEquals(SortMode.NAME, MaterialSectionManager.getCurrentMode("materials"));
            assertEquals(SortOrder.DESCENDING, MaterialSectionManager.getCurrentOrder("materials"));
            assertEquals(SortMode.DRAW_SPEED, MaterialSectionManager.getCurrentMode("bowmaterials"));
            assertEquals(SortOrder.DESCENDING, MaterialSectionManager.getCurrentOrder("bowmaterials"));
            assertEquals(SortMode.ARMOR_DEFENSE, com.shiver.tinkers_sort.book.ArmorySectionManager.getCurrentMode());
            assertEquals(SortOrder.DESCENDING, com.shiver.tinkers_sort.book.ArmorySectionManager.getCurrentOrder());

            // Also test fallback for invalid or inapplicable modes
            com.shiver.tinkers_sort.config.ModConfig.defaultSortMode = "NON_EXISTENT_MODE";
            com.shiver.tinkers_sort.config.ModConfig.defaultArmorSortMode = "DRAW_SPEED"; // DRAW_SPEED is not applicable to armor
            MaterialSectionManager.init(book);
            com.shiver.tinkers_sort.book.ArmorySectionManager.init(book);

            assertEquals(SortMode.DEFAULT, MaterialSectionManager.getCurrentMode("materials"));
            assertEquals(SortMode.DEFAULT, com.shiver.tinkers_sort.book.ArmorySectionManager.getCurrentMode());
        } finally {
            com.shiver.tinkers_sort.config.ModConfig.rememberLastSort = origRemember;
            com.shiver.tinkers_sort.config.ModConfig.defaultSortMode = origToolMode;
            com.shiver.tinkers_sort.config.ModConfig.defaultAscending = origToolAsc;
            com.shiver.tinkers_sort.config.ModConfig.defaultBowSortMode = origBowMode;
            com.shiver.tinkers_sort.config.ModConfig.defaultBowAscending = origBowAsc;
            com.shiver.tinkers_sort.config.ModConfig.defaultArmorSortMode = origArmorMode;
            com.shiver.tinkers_sort.config.ModConfig.defaultArmorAscending = origArmorAsc;
        }
    }

    @Test
    public void testPaginationLoopRetryOnPageOverflow() {
        class MockPage {
            int capacity = 20;
            java.util.List<String> items = new java.util.ArrayList<>();
            boolean add(String item) {
                if (items.size() >= capacity) return false;
                items.add(item);
                return true;
            }
        }

        java.util.List<MockPage> pages = java.util.Arrays.asList(new MockPage(), new MockPage(), new MockPage());
        java.util.Iterator<MockPage> iter = pages.iterator();
        MockPage current = iter.next();

        java.util.List<String> allItems = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            allItems.add("item_" + i);
        }

        for (String item : allItems) {
            while (!current.add(item)) {
                if (!iter.hasNext()) break;
                current = iter.next();
            }
        }

        int totalCount = pages.stream().mapToInt(p -> p.items.size()).sum();
        assertEquals(50, totalCount, "All 50 items should be added across pages without dropping items during page transition");
        assertEquals(20, pages.get(0).items.size());
        assertEquals(20, pages.get(1).items.size());
        assertEquals(10, pages.get(2).items.size());
    }

    @Test
    public void testMixedSpreadCurrentSectionResolution() throws Exception {
        slimeknights.mantle.client.book.data.BookData book = new slimeknights.mantle.client.book.data.BookData();
        slimeknights.mantle.client.book.data.SectionData toolSec = new slimeknights.mantle.client.book.data.SectionData();
        toolSec.name = "materials";
        book.sections.add(toolSec);

        slimeknights.mantle.client.book.data.SectionData bowSec = new slimeknights.mantle.client.book.data.SectionData();
        bowSec.name = "bowmaterials";
        book.sections.add(bowSec);

        // Create a spread where left page is materials and right page is bowmaterials
        // Page 0 (cover/first page in toolSec)
        slimeknights.mantle.client.book.data.PageData page0 = new slimeknights.mantle.client.book.data.PageData();
        page0.parent = toolSec;
        toolSec.pages.add(page0);

        // Page 1 (left page on spread 1)
        slimeknights.mantle.client.book.data.PageData leftPage = new slimeknights.mantle.client.book.data.PageData();
        leftPage.parent = toolSec;
        toolSec.pages.add(leftPage);

        // Page 2 (right page on spread 1)
        slimeknights.mantle.client.book.data.PageData rightPage = new slimeknights.mantle.client.book.data.PageData();
        rightPage.parent = bowSec;
        bowSec.pages.add(rightPage);

        java.lang.reflect.Field uf = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        uf.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) uf.get(null);
        slimeknights.mantle.client.gui.book.GuiBook guiBook =
                (slimeknights.mantle.client.gui.book.GuiBook) unsafe.allocateInstance(slimeknights.mantle.client.gui.book.GuiBook.class);

        java.lang.reflect.Field bf = slimeknights.mantle.client.gui.book.GuiBook.class.getDeclaredField("book");
        bf.setAccessible(true);
        bf.set(guiBook, book);

        java.lang.reflect.Field pf = slimeknights.mantle.client.gui.book.GuiBook.class.getDeclaredField("page");
        pf.setAccessible(true);
        pf.setInt(guiBook, 1);

        slimeknights.mantle.client.book.data.SectionData resolvedBow =
                com.shiver.tinkers_sort.client.gui.BookGuiHandler.getCurrentSection(guiBook, "bowmaterials");
        assertNotNull(resolvedBow);
        assertEquals("bowmaterials", resolvedBow.name);

        slimeknights.mantle.client.book.data.SectionData resolvedTools =
                com.shiver.tinkers_sort.client.gui.BookGuiHandler.getCurrentSection(guiBook, "materials");
        assertNotNull(resolvedTools);
        assertEquals("materials", resolvedTools.name);
    }

    @Test
    public void testBowMaterialsEmptySearchResultGeneratesCategoryOverviews() {
        slimeknights.mantle.client.book.data.BookData book = new slimeknights.mantle.client.book.data.BookData();
        slimeknights.mantle.client.book.data.SectionData toolSec = new slimeknights.mantle.client.book.data.SectionData();
        toolSec.name = "materials";
        book.sections.add(toolSec);

        slimeknights.mantle.client.book.data.SectionData bowSec = new slimeknights.mantle.client.book.data.SectionData();
        bowSec.name = "bowmaterials";
        book.sections.add(bowSec);

        MaterialSectionManager.init(book);

        // Filter with nonexistent query
        MaterialSectionManager.applySort(book, "bowmaterials", SortMode.DEFAULT, SortOrder.ASCENDING, "non_existent_filter_xyz");

        // Category overviews must still exist and be indexed >= 1
        Integer bowIdx = MaterialSectionManager.getBowCategoryPageIndex("bow");
        assertNotNull(bowIdx);
        assertEquals(Integer.valueOf(1), bowIdx);

        Integer stringIdx = MaterialSectionManager.getBowCategoryPageIndex("bowstring");
        assertNotNull(stringIdx);
        assertEquals(Integer.valueOf(2), stringIdx);

        Integer shaftIdx = MaterialSectionManager.getBowCategoryPageIndex("shaft");
        assertNotNull(shaftIdx);
        assertEquals(Integer.valueOf(3), shaftIdx);

        Integer fletchingIdx = MaterialSectionManager.getBowCategoryPageIndex("fletching");
        assertNotNull(fletchingIdx);
        assertEquals(Integer.valueOf(4), fletchingIdx);

        // Now clear query
        MaterialSectionManager.applySort(book, "bowmaterials", SortMode.DEFAULT, SortOrder.ASCENDING, "");
        assertEquals("", MaterialSectionManager.getCurrentQuery("bowmaterials"));
        assertEquals("", MaterialSectionManager.getCurrentQuery("bow"));
    }
}


