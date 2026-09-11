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
}

