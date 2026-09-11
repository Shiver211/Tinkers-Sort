package com.shiver.tinkers_sort.sorting;

import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.traits.ITrait;

import java.util.Collection;

public class MaterialFilter {

    public static boolean matches(Material material, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String q = query.trim().toLowerCase();

        // Mod search: @modid or @modname
        if (q.startsWith("@")) {
            String modQuery = q.substring(1).trim();
            if (modQuery.isEmpty()) return true;
            String modId = MaterialComparator.getModId(material);
            String modName = MaterialComparator.getModName(material);
            return JechHelper.contains(modId, modQuery) || JechHelper.contains(modName, modQuery);
        }

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
