package com.shiver.tinkers_sort.proxy;

import com.shiver.tinkers_sort.book.TinkerBookTransformer;
import com.shiver.tinkers_sort.client.gui.BookGuiHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import slimeknights.tconstruct.library.book.TinkerBook;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(new BookGuiHandler());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        TinkerBook.INSTANCE.addTransformer(TinkerBookTransformer.INSTANCE);
        com.shiver.tinkers_sort.integration.ArmoryIntegration.init();
    }
}

