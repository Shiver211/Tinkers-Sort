package com.shiver.tinkers_sort.sorting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import net.minecraftforge.fml.common.ModContainer;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.BowMaterialStats;
import slimeknights.tconstruct.library.materials.ExtraMaterialStats;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.MaterialTypes;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

public class MaterialComparator implements Comparator<Material> {

    private final SortMode mode;
    private final SortOrder order;
    private final Map<String, Integer> defaultIndices;
    private final Collator collator;

    public MaterialComparator(SortMode mode, SortOrder order, Map<String, Integer> defaultIndices) {
        this.mode = mode;
        this.order = order;
        this.defaultIndices = defaultIndices;

        Locale currentLocale = Locale.CHINA;
        try {
            Language currentLang = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage();
            if (currentLang != null) {
                currentLocale = currentLang.getJavaLocale();
            }
        } catch (Exception ignored) {}
        this.collator = Collator.getInstance(currentLocale);
        this.collator.setStrength(Collator.SECONDARY);
    }

    @Override
    public int compare(Material m1, Material m2) {
        if (m1 == m2) return 0;
        if (m1 == null) return 1;
        if (m2 == null) return -1;

        int result = 0;
        switch (mode) {
            case DEFAULT:
                result = compareDefault(m1, m2);
                break;
            case NAME:
                result = compareName(m1, m2);
                break;
            case MOD:
                result = compareMod(m1, m2);
                break;
            case HARVEST_LEVEL:
                result = compareHarvestLevel(m1, m2);
                break;
            case DURABILITY:
                result = compareDurability(m1, m2);
                break;
            case MINING_SPEED:
                result = compareMiningSpeed(m1, m2);
                break;
            case ATTACK_DAMAGE:
                result = compareAttackDamage(m1, m2);
                break;
            case DRAW_SPEED:
                result = compareDrawSpeed(m1, m2);
                break;
            case RANGE:
                result = compareRange(m1, m2);
                break;
            case BONUS_DAMAGE:
                result = compareBonusDamage(m1, m2);
                break;
        }

        // Secondary stable tie-breaker: fallback to default index
        if (result == 0) {
            int idx1 = defaultIndices != null ? defaultIndices.getOrDefault(m1.getIdentifier(), Integer.MAX_VALUE) : 0;
            int idx2 = defaultIndices != null ? defaultIndices.getOrDefault(m2.getIdentifier(), Integer.MAX_VALUE) : 0;
            result = Integer.compare(idx1, idx2);
        }

        return result;
    }

    private int compareDefault(Material m1, Material m2) {
        int idx1 = defaultIndices != null ? defaultIndices.getOrDefault(m1.getIdentifier(), Integer.MAX_VALUE) : 0;
        int idx2 = defaultIndices != null ? defaultIndices.getOrDefault(m2.getIdentifier(), Integer.MAX_VALUE) : 0;
        int cmp = Integer.compare(idx1, idx2);
        return order == SortOrder.DESCENDING ? -cmp : cmp;
    }

    private int compareName(Material m1, Material m2) {
        int cmp = JechHelper.compare(m1.getLocalizedName(), m2.getLocalizedName(), collator);
        return order == SortOrder.DESCENDING ? -cmp : cmp;
    }

    private int compareMod(Material m1, Material m2) {
        String mod1 = getModName(m1);
        String mod2 = getModName(m2);
        int cmp = JechHelper.compare(mod1, mod2, collator);
        if (cmp != 0) {
            return order == SortOrder.DESCENDING ? -cmp : cmp;
        }
        return compareName(m1, m2);
    }

    public static String getModName(Material material) {
        try {
            ModContainer mod = TinkerRegistry.getTrace(material);
            if (mod != null) {
                return mod.getName();
            }
        } catch (Exception ignored) {}
        return "Minecraft";
    }

    public static String getModId(Material material) {
        try {
            ModContainer mod = TinkerRegistry.getTrace(material);
            if (mod != null) {
                return mod.getModId();
            }
        } catch (Exception ignored) {}
        return "minecraft";
    }

    private int compareHarvestLevel(Material m1, Material m2) {
        HeadMaterialStats h1 = m1.getStats(MaterialTypes.HEAD);
        HeadMaterialStats h2 = m2.getStats(MaterialTypes.HEAD);
        boolean has1 = h1 != null;
        boolean has2 = h2 != null;
        if (has1 != has2) {
            return has1 ? -1 : 1;
        }
        if (!has1) {
            return 0;
        }
        int cmp = Integer.compare(h1.harvestLevel, h2.harvestLevel);
        return order == SortOrder.DESCENDING ? -cmp : cmp;
    }

    private int compareDurability(Material m1, Material m2) {
        Integer d1 = getDurability(m1);
        Integer d2 = getDurability(m2);
        boolean has1 = d1 != null;
        boolean has2 = d2 != null;
        if (has1 != has2) {
            return has1 ? -1 : 1;
        }
        if (!has1) {
            return 0;
        }
        int cmp = Integer.compare(d1, d2);
        return order == SortOrder.DESCENDING ? -cmp : cmp;
    }

    private Integer getDurability(Material m) {
        HeadMaterialStats head = m.getStats(MaterialTypes.HEAD);
        if (head != null) return head.durability;
        HandleMaterialStats handle = m.getStats(MaterialTypes.HANDLE);
        if (handle != null) return handle.durability;
        ExtraMaterialStats extra = m.getStats(MaterialTypes.EXTRA);
        if (extra != null) return extra.extraDurability;
        return null;
    }

    private int compareMiningSpeed(Material m1, Material m2) {
        HeadMaterialStats h1 = m1.getStats(MaterialTypes.HEAD);
        HeadMaterialStats h2 = m2.getStats(MaterialTypes.HEAD);
        boolean has1 = h1 != null;
        boolean has2 = h2 != null;
        if (has1 != has2) {
            return has1 ? -1 : 1;
        }
        if (!has1) {
            return 0;
        }
        int cmp = Float.compare(h1.miningspeed, h2.miningspeed);
        return order == SortOrder.DESCENDING ? -cmp : cmp;
    }

    private int compareAttackDamage(Material m1, Material m2) {
        HeadMaterialStats h1 = m1.getStats(MaterialTypes.HEAD);
        HeadMaterialStats h2 = m2.getStats(MaterialTypes.HEAD);
        boolean has1 = h1 != null;
        boolean has2 = h2 != null;
        if (has1 != has2) {
            return has1 ? -1 : 1;
        }
        if (!has1) {
            return 0;
        }
        int cmp = Float.compare(h1.attack, h2.attack);
        return order == SortOrder.DESCENDING ? -cmp : cmp;
    }

    private int compareDrawSpeed(Material m1, Material m2) {
        BowMaterialStats b1 = m1.getStats(MaterialTypes.BOW);
        BowMaterialStats b2 = m2.getStats(MaterialTypes.BOW);
        boolean has1 = b1 != null;
        boolean has2 = b2 != null;
        if (has1 != has2) {
            return has1 ? -1 : 1;
        }
        if (!has1) {
            return 0;
        }
        int cmp = Float.compare(b1.drawspeed, b2.drawspeed);
        return order == SortOrder.DESCENDING ? -cmp : cmp;
    }

    private int compareRange(Material m1, Material m2) {
        BowMaterialStats b1 = m1.getStats(MaterialTypes.BOW);
        BowMaterialStats b2 = m2.getStats(MaterialTypes.BOW);
        boolean has1 = b1 != null;
        boolean has2 = b2 != null;
        if (has1 != has2) {
            return has1 ? -1 : 1;
        }
        if (!has1) {
            return 0;
        }
        int cmp = Float.compare(b1.range, b2.range);
        return order == SortOrder.DESCENDING ? -cmp : cmp;
    }

    private int compareBonusDamage(Material m1, Material m2) {
        BowMaterialStats b1 = m1.getStats(MaterialTypes.BOW);
        BowMaterialStats b2 = m2.getStats(MaterialTypes.BOW);
        boolean has1 = b1 != null;
        boolean has2 = b2 != null;
        if (has1 != has2) {
            return has1 ? -1 : 1;
        }
        if (!has1) {
            return 0;
        }
        int cmp = Float.compare(b1.bonusDamage, b2.bonusDamage);
        return order == SortOrder.DESCENDING ? -cmp : cmp;
    }
}
