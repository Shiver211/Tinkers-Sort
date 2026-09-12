package com.shiver.tinkers_sort.integration;

import com.shiver.tinkers_sort.book.ArmorySectionManager;
import com.shiver.tinkers_sort.sorting.SortMode;
import com.shiver.tinkers_sort.sorting.SortOrder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.data.BookData;

@SideOnly(Side.CLIENT)
public class ArmoryToolbarHelper {

    public static SortMode getCurrentMode() {
        return ArmorySectionManager.getCurrentMode();
    }

    public static SortOrder getCurrentOrder() {
        return ArmorySectionManager.getCurrentOrder();
    }

    public static String getCurrentQuery() {
        return ArmorySectionManager.getCurrentQuery();
    }

    public static void applySort(BookData book, SortMode mode, SortOrder order, String query) {
        ArmorySectionManager.applySort(book, mode, order, query);
    }

    public static void reset(BookData book) {
        ArmorySectionManager.reset(book);
    }

    public static void init(BookData book) {
        ArmorySectionManager.init(book);
    }
}
