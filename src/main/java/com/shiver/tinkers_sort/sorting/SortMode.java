package com.shiver.tinkers_sort.sorting;

import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

public enum SortMode {
    DEFAULT("tinkers_sort.sort.default", "tinkers_sort.sort.default.desc", false, false, TargetCategory.ALL),
    NAME("tinkers_sort.sort.name", "tinkers_sort.sort.name.desc", false, false, TargetCategory.ALL),
    HARVEST_LEVEL("tinkers_sort.sort.harvest_level", "tinkers_sort.sort.harvest_level.desc", true, false, TargetCategory.TOOL),
    DURABILITY("tinkers_sort.sort.durability", "tinkers_sort.sort.durability.desc", true, true, TargetCategory.TOOL_AND_BOW),
    MINING_SPEED("tinkers_sort.sort.mining_speed", "tinkers_sort.sort.mining_speed.desc", true, false, TargetCategory.TOOL),
    ATTACK_DAMAGE("tinkers_sort.sort.attack_damage", "tinkers_sort.sort.attack_damage.desc", true, false, TargetCategory.TOOL),
    DRAW_SPEED("tinkers_sort.sort.draw_speed", "tinkers_sort.sort.draw_speed.desc", false, true, TargetCategory.BOW),
    RANGE("tinkers_sort.sort.range", "tinkers_sort.sort.range.desc", false, true, TargetCategory.BOW),
    BONUS_DAMAGE("tinkers_sort.sort.bonus_damage", "tinkers_sort.sort.bonus_damage.desc", false, true, TargetCategory.BOW),
    BOWSTRING_MODIFIER("tinkers_sort.sort.bowstring_modifier", "tinkers_sort.sort.bowstring_modifier.desc", false, true, TargetCategory.BOWSTRING),
    BONUS_AMMO("tinkers_sort.sort.bonus_ammo", "tinkers_sort.sort.bonus_ammo.desc", false, true, TargetCategory.SHAFT),
    SHAFT_MODIFIER("tinkers_sort.sort.shaft_modifier", "tinkers_sort.sort.shaft_modifier.desc", false, true, TargetCategory.SHAFT),
    ACCURACY("tinkers_sort.sort.accuracy", "tinkers_sort.sort.accuracy.desc", false, true, TargetCategory.FLETCHING),
    FLETCHING_MODIFIER("tinkers_sort.sort.fletching_modifier", "tinkers_sort.sort.fletching_modifier.desc", false, true, TargetCategory.FLETCHING),
    MODIFIER("tinkers_sort.sort.modifier", "tinkers_sort.sort.modifier.desc", false, true, TargetCategory.GENERAL_MODIFIER);

    public enum TargetCategory {
        ALL,
        TOOL,
        TOOL_AND_BOW,
        BOW,
        BOWSTRING,
        SHAFT,
        FLETCHING,
        GENERAL_MODIFIER
    }

    private final String unlocalizedName;
    private final String unlocalizedDesc;
    private final boolean toolSpecific;
    private final boolean bowSpecific;
    private final TargetCategory category;

    SortMode(String unlocalizedName, String unlocalizedDesc, boolean toolSpecific, boolean bowSpecific, TargetCategory category) {
        this.unlocalizedName = unlocalizedName;
        this.unlocalizedDesc = unlocalizedDesc;
        this.toolSpecific = toolSpecific;
        this.bowSpecific = bowSpecific;
        this.category = category;
    }

    public String getDisplayName() {
        return I18n.format(unlocalizedName);
    }

    public String getDescription() {
        return I18n.format(unlocalizedDesc);
    }

    public TargetCategory getCategory() {
        return category;
    }

    public boolean isApplicable(String sectionName) {
        if (sectionName == null) return true;
        String s = sectionName.trim().toLowerCase();
        if (s.startsWith("bowmaterials:")) {
            s = s.substring("bowmaterials:".length());
        }

        if ("materials".equals(s)) {
            return category == TargetCategory.ALL || category == TargetCategory.TOOL || category == TargetCategory.TOOL_AND_BOW;
        } else if ("bow".equals(s)) {
            return category == TargetCategory.ALL || category == TargetCategory.BOW || category == TargetCategory.TOOL_AND_BOW;
        } else if ("bowstring".equals(s)) {
            return category == TargetCategory.ALL || category == TargetCategory.BOWSTRING || category == TargetCategory.GENERAL_MODIFIER;
        } else if ("shaft".equals(s)) {
            return category == TargetCategory.ALL || category == TargetCategory.SHAFT || category == TargetCategory.GENERAL_MODIFIER;
        } else if ("fletching".equals(s)) {
            return category == TargetCategory.ALL || category == TargetCategory.FLETCHING || category == TargetCategory.GENERAL_MODIFIER;
        } else if ("bowmaterials".equals(s) || "all".equals(s)) {
            return !toolSpecific || this == DURABILITY;
        }
        return true;
    }

    public static List<SortMode> getApplicableModes(String sectionName) {
        List<SortMode> list = new ArrayList<>();
        for (SortMode mode : values()) {
            if (mode.isApplicable(sectionName)) {
                list.add(mode);
            }
        }
        return list;
    }

    public SortMode next(String sectionName) {
        List<SortMode> applicable = getApplicableModes(sectionName);
        if (applicable.isEmpty()) return DEFAULT;
        int idx = applicable.indexOf(this);
        if (idx == -1 || idx + 1 >= applicable.size()) {
            return applicable.get(0);
        }
        return applicable.get(idx + 1);
    }

    public SortMode previous(String sectionName) {
        List<SortMode> applicable = getApplicableModes(sectionName);
        if (applicable.isEmpty()) return DEFAULT;
        int idx = applicable.indexOf(this);
        if (idx <= 0) {
            return applicable.get(applicable.size() - 1);
        }
        return applicable.get(idx - 1);
    }
}

