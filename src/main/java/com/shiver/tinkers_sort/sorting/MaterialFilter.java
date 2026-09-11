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
            String modId = MaterialComparator.getModId(material).toLowerCase();
            String modName = MaterialComparator.getModName(material).toLowerCase();
            return modId.contains(modQuery) || modName.contains(modQuery);
        }

        // Trait search: #trait
        if (q.startsWith("#")) {
            String traitQuery = q.substring(1).trim();
            if (traitQuery.isEmpty()) return true;
            Collection<ITrait> traits = material.getAllTraits();
            for (ITrait trait : traits) {
                if (trait.getLocalizedName().toLowerCase().contains(traitQuery)
                        || trait.getIdentifier().toLowerCase().contains(traitQuery)) {
                    return true;
                }
            }
            return false;
        }

        // Standard search: localized name or identifier
        String localizedName = material.getLocalizedName().toLowerCase();
        if (localizedName.contains(q)) {
            return true;
        }

        String identifier = material.getIdentifier().toLowerCase();
        if (identifier.contains(q)) {
            return true;
        }

        // Check mod name as well
        String modName = MaterialComparator.getModName(material).toLowerCase();
        if (modName.contains(q)) {
            return true;
        }

        // Check traits
        Collection<ITrait> traits = material.getAllTraits();
        for (ITrait trait : traits) {
            if (trait.getLocalizedName().toLowerCase().contains(q)) {
                return true;
            }
        }

        return false;
    }
}
