package com.shiver.tinkers_sort.sorting;

import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

public enum SortMode {
    DEFAULT("tinkers_sort.sort.default", "tinkers_sort.sort.default.desc", false, false),
    NAME("tinkers_sort.sort.name", "tinkers_sort.sort.name.desc", false, false),
    HARVEST_LEVEL("tinkers_sort.sort.harvest_level", "tinkers_sort.sort.harvest_level.desc", true, false),
    DURABILITY("tinkers_sort.sort.durability", "tinkers_sort.sort.durability.desc", true, true),
    MINING_SPEED("tinkers_sort.sort.mining_speed", "tinkers_sort.sort.mining_speed.desc", true, false),
    ATTACK_DAMAGE("tinkers_sort.sort.attack_damage", "tinkers_sort.sort.attack_damage.desc", true, false),
    DRAW_SPEED("tinkers_sort.sort.draw_speed", "tinkers_sort.sort.draw_speed.desc", false, true),
    RANGE("tinkers_sort.sort.range", "tinkers_sort.sort.range.desc", false, true),
    BONUS_DAMAGE("tinkers_sort.sort.bonus_damage", "tinkers_sort.sort.bonus_damage.desc", false, true);

    private final String unlocalizedName;
    private final String unlocalizedDesc;
    private final boolean toolSpecific;
    private final boolean bowSpecific;

    SortMode(String unlocalizedName, String unlocalizedDesc, boolean toolSpecific, boolean bowSpecific) {
        this.unlocalizedName = unlocalizedName;
        this.unlocalizedDesc = unlocalizedDesc;
        this.toolSpecific = toolSpecific;
        this.bowSpecific = bowSpecific;
    }

    public String getDisplayName() {
        return I18n.format(unlocalizedName);
    }

    public String getDescription() {
        return I18n.format(unlocalizedDesc);
    }

    public boolean isApplicable(String sectionName) {
        if ("materials".equalsIgnoreCase(sectionName)) {
            return !bowSpecific || this == DURABILITY;
        } else if ("bowmaterials".equalsIgnoreCase(sectionName)) {
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

