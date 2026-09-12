package com.shiver.tinkers_sort.integration;

import com.shiver.tinkers_sort.book.ArmoryBookTransformer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.data.BookData;

@SideOnly(Side.CLIENT)
public class ArmoryIntegration {

    private static Boolean conarmLoaded = null;

    public static boolean isLoaded() {
        if (conarmLoaded == null) {
            try {
                conarmLoaded = Loader.isModLoaded("conarm");
            } catch (Throwable t) {
                conarmLoaded = false;
            }
        }
        return conarmLoaded;
    }

    public static void setForceLoadedForTest(Boolean loaded) {
        conarmLoaded = loaded;
    }

    public static void init() {
        if (isLoaded()) {
            try {
                c4.conarm.lib.book.ArmoryBook.INSTANCE.addTransformer(ArmoryBookTransformer.INSTANCE);
            } catch (Throwable t) {
                // Ignore if class not found or other errors
            }
        }
    }

    public static boolean isArmoryBook(BookData book) {
        if (!isLoaded() || book == null) {
            return false;
        }
        try {
            return book == c4.conarm.lib.book.ArmoryBook.INSTANCE;
        } catch (Throwable t) {
            return false;
        }
    }
}
