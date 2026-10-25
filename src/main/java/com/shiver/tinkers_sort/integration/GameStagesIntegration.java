package com.shiver.tinkers_sort.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.materials.Material;

import java.lang.reflect.Method;
import java.util.*;

@SideOnly(Side.CLIENT)
public class GameStagesIntegration {

    private static Boolean gameStagesLoaded = null;
    private static Boolean itemStagesLoaded = null;

    private static boolean itemStagesAttempted = false;
    private static Method itemStagesGetStageMethod = null;

    // Test overrides
    private static Boolean forceLoadedForTest = null;
    private static Set<String> mockPlayerStages = null;
    private static Map<String, Collection<String>> mockMaterialStages = null;

    public static boolean isGameStagesLoaded() {
        if (forceLoadedForTest != null) return forceLoadedForTest;
        if (gameStagesLoaded == null) {
            try {
                gameStagesLoaded = Loader.isModLoaded("gamestages");
            } catch (Throwable t) {
                gameStagesLoaded = false;
            }
        }
        return gameStagesLoaded;
    }

    public static boolean isItemStagesLoaded() {
        if (forceLoadedForTest != null) return forceLoadedForTest;
        if (itemStagesLoaded == null) {
            try {
                itemStagesLoaded = Loader.isModLoaded("itemstages");
            } catch (Throwable t) {
                itemStagesLoaded = false;
            }
        }
        return itemStagesLoaded;
    }

    public static boolean isAvailable() {
        return isGameStagesLoaded();
    }

    public static String getItemStage(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        if (!itemStagesAttempted) {
            itemStagesAttempted = true;
            if (isItemStagesLoaded()) {
                try {
                    Class<?> clazz = Class.forName("net.darkhax.itemstages.ItemStages");
                    itemStagesGetStageMethod = clazz.getMethod("getStage", ItemStack.class);
                } catch (Throwable t) {
                    itemStagesGetStageMethod = null;
                }
            }
        }
        if (itemStagesGetStageMethod != null) {
            try {
                return (String) itemStagesGetStageMethod.invoke(null, stack);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    public static boolean hasStage(EntityPlayer player, String stage) {
        if (stage == null || stage.isEmpty()) return true;
        if (mockPlayerStages != null) {
            return mockPlayerStages.contains(stage);
        }
        if (player == null || !isGameStagesLoaded()) return false;
        try {
            return GameStagesAdapter.hasStage(player, stage);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasAnyOf(EntityPlayer player, Collection<String> stages) {
        if (stages == null || stages.isEmpty()) return true;
        if (mockPlayerStages != null) {
            for (String s : stages) {
                if (mockPlayerStages.contains(s)) return true;
            }
            return false;
        }
        if (player == null || !isGameStagesLoaded()) return false;
        try {
            return GameStagesAdapter.hasAnyOf(player, stages);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isMaterialUnlocked(Material material) {
        if (material == null) return true;
        if (!isAvailable() && mockMaterialStages == null) {
            return true;
        }

        EntityPlayer player = null;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null) {
                player = mc.player;
            }
        } catch (Throwable ignored) {}

        if (player == null && mockPlayerStages == null) {
            return true;
        }

        return isMaterialUnlocked(material, player);
    }

    public static boolean isMaterialUnlocked(Material material, EntityPlayer player) {
        if (material == null) return true;
        String matId = material.getIdentifier();

        // 0. Check mock stages if present in testing
        if (mockMaterialStages != null && mockMaterialStages.containsKey(matId)) {
            Collection<String> req = mockMaterialStages.get(matId);
            if (req != null && !req.isEmpty()) {
                return hasAnyOf(player, req);
            }
        }

        // 1. Check ItemStages on representative item and shard
        if (isItemStagesLoaded()) {
            ItemStack rep = material.getRepresentativeItem();
            if (rep != null && !rep.isEmpty()) {
                String stage = getItemStage(rep);
                if (stage != null && !stage.isEmpty()) {
                    if (!hasStage(player, stage)) {
                        return false;
                    }
                }
            }

            ItemStack shard = material.getShard();
            if (shard != null && !shard.isEmpty()) {
                String stage = getItemStage(shard);
                if (stage != null && !stage.isEmpty()) {
                    if (!hasStage(player, stage)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static void setForceLoadedForTest(Boolean loaded) {
        forceLoadedForTest = loaded;
    }

    public static void setMockPlayerStages(Set<String> stages) {
        mockPlayerStages = stages;
    }

    public static void setMockMaterialStages(Map<String, Collection<String>> stages) {
        mockMaterialStages = stages;
    }

    public static void resetTestState() {
        forceLoadedForTest = null;
        mockPlayerStages = null;
        mockMaterialStages = null;
        itemStagesAttempted = false;
        itemStagesGetStageMethod = null;
        gameStagesLoaded = null;
        itemStagesLoaded = null;
    }

    /**
     * Isolated inner class to avoid ClassNotFoundException when GameStages is not installed.
     */
    private static class GameStagesAdapter {
        static boolean hasStage(EntityPlayer player, String stage) {
            return net.darkhax.gamestages.GameStageHelper.hasStage(player, stage);
        }

        static boolean hasAnyOf(EntityPlayer player, Collection<String> stages) {
            return net.darkhax.gamestages.GameStageHelper.hasAnyOf(player, stages);
        }
    }
}
