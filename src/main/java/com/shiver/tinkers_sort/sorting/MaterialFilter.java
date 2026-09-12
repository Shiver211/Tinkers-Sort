package com.shiver.tinkers_sort.sorting;

import com.shiver.tinkers_sort.integration.JechHelper;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.traits.ITrait;

import java.util.Collection;

public class MaterialFilter {

    public static boolean matches(Material material, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String q = query.trim().toLowerCase();

        // Trait search: #trait
        if (q.startsWith("#")) {
            String traitQuery = q.substring(1).trim();
            if (traitQuery.isEmpty()) return true;
            Collection<ITrait> traits = material.getAllTraits();
            for (ITrait trait : traits) {
                if (JechHelper.contains(trait.getLocalizedName(), traitQuery)
                        || trait.getIdentifier().toLowerCase().contains(traitQuery)) {
                    return true;
                }
            }
            return false;
        }

        // Standard search: localized name or identifier
        if (JechHelper.contains(material.getLocalizedName(), q)) {
            return true;
        }

        if (JechHelper.contains(material.getIdentifier(), q)) {
            return true;
        }

        return false;
    }
}
