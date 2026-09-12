package com.shiver.tinkers_sort.config;

import com.shiver.tinkers_sort.Tags;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Tags.MOD_ID, name = Tags.MOD_ID)
public class ModConfig {

    @Config.Comment("Default sorting mode for tool materials (DEFAULT, NAME, HARVEST_LEVEL, DURABILITY, MINING_SPEED, ATTACK_DAMAGE)")
    @Config.Name("DefaultSortMode")
    public static String defaultSortMode = "DEFAULT";

    @Config.Comment("Default sort order for tool materials: true for ascending (A-Z / low to high), false for descending (Z-A / high to low)")
    @Config.Name("DefaultAscending")
    public static boolean defaultAscending = true;

    @Config.Comment("Default sorting mode for bow materials / limbs (DEFAULT, NAME, DURABILITY, DRAW_SPEED, RANGE, BONUS_DAMAGE)")
    @Config.Name("DefaultBowSortMode")
    public static String defaultBowSortMode = "DEFAULT";

    @Config.Comment("Default sort order for bow materials / limbs: true for ascending, false for descending")
    @Config.Name("DefaultBowAscending")
    public static boolean defaultBowAscending = true;

    @Config.Comment("Default sorting mode for bowstring materials (DEFAULT, NAME, BOWSTRING_MODIFIER)")
    @Config.Name("DefaultBowstringSortMode")
    public static String defaultBowstringSortMode = "DEFAULT";

    @Config.Comment("Default sort order for bowstring materials: true for ascending, false for descending")
    @Config.Name("DefaultBowstringAscending")
    public static boolean defaultBowstringAscending = true;

    @Config.Comment("Default sorting mode for arrow shaft materials (DEFAULT, NAME, BONUS_AMMO, SHAFT_MODIFIER)")
    @Config.Name("DefaultShaftSortMode")
    public static String defaultShaftSortMode = "DEFAULT";

    @Config.Comment("Default sort order for arrow shaft materials: true for ascending, false for descending")
    @Config.Name("DefaultShaftAscending")
    public static boolean defaultShaftAscending = true;

    @Config.Comment("Default sorting mode for fletching materials (DEFAULT, NAME, ACCURACY, FLETCHING_MODIFIER)")
    @Config.Name("DefaultFletchingSortMode")
    public static String defaultFletchingSortMode = "DEFAULT";

    @Config.Comment("Default sort order for fletching materials: true for ascending, false for descending")
    @Config.Name("DefaultFletchingAscending")
    public static boolean defaultFletchingAscending = true;

    @Config.Comment("Whether to remember the last used sort mode and order across sessions")
    @Config.Name("RememberLastSort")
    public static boolean rememberLastSort = true;

    @Config.Comment("Whether to enable the real-time search bar in the book")
    @Config.Name("EnableSearch")
    public static boolean enableSearch = true;

    @Config.Comment("Vertical offset of the sorting toolbar relative to the book top (in GUI pixels)")
    @Config.Name("ToolbarYOffset")
    public static int toolbarYOffset = -22;

    public static void save() {
        ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
    }

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID)
    public static class ConfigSyncHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Tags.MOD_ID)) {
                ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}

