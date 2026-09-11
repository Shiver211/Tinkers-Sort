package com.shiver.tinkers_sort.book;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.BookTransformer;
import slimeknights.mantle.client.book.data.BookData;

@SideOnly(Side.CLIENT)
public class TinkerBookTransformer extends BookTransformer {

    public static final TinkerBookTransformer INSTANCE = new TinkerBookTransformer();

    @Override
    public void transform(BookData book) {
        MaterialSectionManager.init(book);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TinkerBookTransformer;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

