package com.shiver.tinkers_sort.book;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.BookTransformer;
import slimeknights.mantle.client.book.data.BookData;

@SideOnly(Side.CLIENT)
public class ArmoryBookTransformer extends BookTransformer {

    public static final ArmoryBookTransformer INSTANCE = new ArmoryBookTransformer();

    @Override
    public void transform(BookData book) {
        ArmorySectionManager.init(book);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ArmoryBookTransformer;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
