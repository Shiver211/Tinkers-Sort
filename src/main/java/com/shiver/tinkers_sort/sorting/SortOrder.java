package com.shiver.tinkers_sort.sorting;

import net.minecraft.client.resources.I18n;

public enum SortOrder {
    ASCENDING("▲", "tinkers_sort.order.ascending"),
    DESCENDING("▼", "tinkers_sort.order.descending");

    private final String symbol;
    private final String unlocalizedName;

    SortOrder(String symbol, String unlocalizedName) {
        this.symbol = symbol;
        this.unlocalizedName = unlocalizedName;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDisplayName() {
        return I18n.format(unlocalizedName);
    }

    public SortOrder toggle() {
        return this == ASCENDING ? DESCENDING : ASCENDING;
    }
}

