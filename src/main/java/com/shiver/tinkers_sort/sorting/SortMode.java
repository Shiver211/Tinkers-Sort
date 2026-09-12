package com.shiver.tinkers_sort.sorting;

import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

public enum SortMode {
    DEFAULT("tinkers_sort.sort.default", "tinkers_sort.sort.default.desc", TargetCategory.ALL),
    NAME("tinkers_sort.sort.name", "tinkers_sort.sort.name.desc", TargetCategory.ALL),
    HARVEST_LEVEL("tinkers_sort.sort.harvest_level", "tinkers_sort.sort.harvest_level.desc", TargetCategory.TOOL),
    DURABILITY("tinkers_sort.sort.durability", "tinkers_sort.sort.durability.desc", TargetCategory.TOOL_AND_BOW),
    MINING_SPEED("tinkers_sort.sort.mining_speed", "tinkers_sort.sort.mining_speed.desc", TargetCategory.TOOL),
    ATTACK_DAMAGE("tinkers_sort.sort.attack_damage", "tinkers_sort.sort.attack_damage.desc", TargetCategory.TOOL),
    HANDLE_MODIFIER("tinkers_sort.sort.handle_modifier", "tinkers_sort.sort.handle_modifier.desc", TargetCategory.TOOL),
    DRAW_SPEED("tinkers_sort.sort.draw_speed", "tinkers_sort.sort.draw_speed.desc", TargetCategory.BOW),
    RANGE("tinkers_sort.sort.range", "tinkers_sort.sort.range.desc", TargetCategory.BOW),
    BONUS_DAMAGE("tinkers_sort.sort.bonus_damage", "tinkers_sort.sort.bonus_damage.desc", TargetCategory.BOW),
    BOWSTRING_MODIFIER("tinkers_sort.sort.bowstring_modifier", "tinkers_sort.sort.bowstring_modifier.desc", TargetCategory.BOWSTRING),
    BONUS_AMMO("tinkers_sort.sort.bonus_ammo", "tinkers_sort.sort.bonus_ammo.desc", TargetCategory.SHAFT),
    SHAFT_MODIFIER("tinkers_sort.sort.shaft_modifier", "tinkers_sort.sort.shaft_modifier.desc", TargetCategory.SHAFT),
    ACCURACY("tinkers_sort.sort.accuracy", "tinkers_sort.sort.accuracy.desc", TargetCategory.FLETCHING),
    FLETCHING_MODIFIER("tinkers_sort.sort.fletching_modifier", "tinkers_sort.sort.fletching_modifier.desc", TargetCategory.FLETCHING);

    public enum TargetCategory {
        ALL,
        TOOL,
        TOOL_AND_BOW,
        BOW,
        BOWSTRING,
        SHAFT,
        FLETCHING
    }

    private final String unlocalizedName;
    private final String unlocalizedDesc;
    private final TargetCategory category;

    SortMode(String unlocalizedName, String unlocalizedDesc, TargetCategory category) {
        this.unlocalizedName = unlocalizedName;
        this.unlocalizedDesc = unlocalizedDesc;
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
            return category == TargetCategory.ALL || category == TargetCategory.BOWSTRING;
        } else if ("shaft".equals(s)) {
            return category == TargetCategory.ALL || category == TargetCategory.SHAFT;
        } else if ("fletching".equals(s)) {
            return category == TargetCategory.ALL || category == TargetCategory.FLETCHING;
        } else if ("bowmaterials".equals(s)) {
            return category != TargetCategory.TOOL;
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

