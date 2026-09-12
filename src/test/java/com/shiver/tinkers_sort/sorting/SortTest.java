package com.shiver.tinkers_sort.sorting;

import com.shiver.tinkers_sort.book.MaterialSectionManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SortTest {

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
        assertFalse(toolModes.contains(SortMode.DRAW_SPEED));

        List<SortMode> bowModes = SortMode.getApplicableModes("bowmaterials");
        assertTrue(bowModes.contains(SortMode.DEFAULT));
        assertTrue(bowModes.contains(SortMode.NAME));
        assertTrue(bowModes.contains(SortMode.DRAW_SPEED));
        assertTrue(bowModes.contains(SortMode.RANGE));
        assertFalse(bowModes.contains(SortMode.HARVEST_LEVEL));
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
        assertTrue(stringModes.contains(SortMode.DEFAULT));
        assertTrue(stringModes.contains(SortMode.NAME));
        assertTrue(stringModes.contains(SortMode.BOWSTRING_MODIFIER));
        assertFalse(stringModes.contains(SortMode.DRAW_SPEED));
        assertFalse(stringModes.contains(SortMode.BONUS_AMMO));

        List<SortMode> shaftModes = SortMode.getApplicableModes("shaft");
        assertTrue(shaftModes.contains(SortMode.DEFAULT));
        assertTrue(shaftModes.contains(SortMode.NAME));
        assertTrue(shaftModes.contains(SortMode.SHAFT_MODIFIER));
        assertTrue(shaftModes.contains(SortMode.BONUS_AMMO));
        assertFalse(shaftModes.contains(SortMode.BOWSTRING_MODIFIER));
        assertFalse(shaftModes.contains(SortMode.ACCURACY));

        List<SortMode> fletchingModes = SortMode.getApplicableModes("fletching");
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
        assertEquals(Integer.valueOf(0), MaterialSectionManager.getBowCategoryPageIndex("all"));
        assertEquals(Integer.valueOf(1), MaterialSectionManager.getBowCategoryPageIndex("bowmaterials"));
        assertEquals(Integer.valueOf(1), MaterialSectionManager.getBowMaterialIconPageIndex("all"));
        assertEquals(Integer.valueOf(1), MaterialSectionManager.getBowMaterialIconPageIndex("bowmaterials"));
        assertEquals(Integer.valueOf(1), MaterialSectionManager.getBowMaterialIconPageIndex("bow"));
        assertEquals(Integer.valueOf(1), MaterialSectionManager.getBowMaterialIconPageIndex(null));
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
}


