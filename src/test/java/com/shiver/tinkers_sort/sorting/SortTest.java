package com.shiver.tinkers_sort.sorting;

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
}

