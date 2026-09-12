package com.shiver.tinkers_sort.sorting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import slimeknights.tconstruct.library.materials.ArrowShaftMaterialStats;
import slimeknights.tconstruct.library.materials.BowMaterialStats;
import slimeknights.tconstruct.library.materials.BowStringMaterialStats;
import slimeknights.tconstruct.library.materials.ExtraMaterialStats;
import slimeknights.tconstruct.library.materials.FletchingMaterialStats;
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
    private final String targetMaterialType;
    private final Collator collator;

    public MaterialComparator(SortMode mode, SortOrder order, Map<String, Integer> defaultIndices) {
        this(mode, order, defaultIndices, null);
    }

    public MaterialComparator(SortMode mode, SortOrder order, String targetMaterialType) {
        this(mode, order, null, targetMaterialType);
    }

    public MaterialComparator(SortMode mode, SortOrder order, Map<String, Integer> defaultIndices, String targetMaterialType) {
        this.mode = mode;
        this.order = order;
        this.defaultIndices = defaultIndices;
        this.targetMaterialType = targetMaterialType;

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
            case BOWSTRING_MODIFIER:
                result = compareBowstringModifier(m1, m2);
                break;
            case BONUS_AMMO:
                result = compareBonusAmmo(m1, m2);
                break;
            case SHAFT_MODIFIER:
                result = compareShaftModifier(m1, m2);
                break;
            case ACCURACY:
                result = compareAccuracy(m1, m2);
                break;
            case FLETCHING_MODIFIER:
                result = compareFletchingModifier(m1, m2);
                break;
            case MODIFIER:
                result = compareGeneralModifier(m1, m2);
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

    private int compareBowstringModifier(Material m1, Material m2) {
        BowStringMaterialStats s1 = m1.getStats(MaterialTypes.BOWSTRING);
        BowStringMaterialStats s2 = m2.getStats(MaterialTypes.BOWSTRING);
        boolean has1 = s1 != null;
        boolean has2 = s2 != null;
        if (has1 != has2) {
            return has1 ? -1 : 1;
        }
        if (!has1) {
            return 0;
        }
        int cmp = Float.compare(s1.modifier, s2.modifier);
        return order == SortOrder.DESCENDING ? -cmp : cmp;
    }

    private int compareBonusAmmo(Material m1, Material m2) {
        ArrowShaftMaterialStats s1 = m1.getStats(MaterialTypes.SHAFT);
        ArrowShaftMaterialStats s2 = m2.getStats(MaterialTypes.SHAFT);
        boolean has1 = s1 != null;
        boolean has2 = s2 != null;
        if (has1 != has2) {
            return has1 ? -1 : 1;
        }
        if (!has1) {
            return 0;
        }
        int cmp = Integer.compare(s1.bonusAmmo, s2.bonusAmmo);
        return order == SortOrder.DESCENDING ? -cmp : cmp;
    }

    private int compareShaftModifier(Material m1, Material m2) {
        ArrowShaftMaterialStats s1 = m1.getStats(MaterialTypes.SHAFT);
        ArrowShaftMaterialStats s2 = m2.getStats(MaterialTypes.SHAFT);
        boolean has1 = s1 != null;
        boolean has2 = s2 != null;
        if (has1 != has2) {
            return has1 ? -1 : 1;
        }
        if (!has1) {
            return 0;
        }
        int cmp = Float.compare(s1.modifier, s2.modifier);
        return order == SortOrder.DESCENDING ? -cmp : cmp;
    }

    private int compareAccuracy(Material m1, Material m2) {
        FletchingMaterialStats f1 = m1.getStats(MaterialTypes.FLETCHING);
        FletchingMaterialStats f2 = m2.getStats(MaterialTypes.FLETCHING);
        boolean has1 = f1 != null;
        boolean has2 = f2 != null;
        if (has1 != has2) {
            return has1 ? -1 : 1;
        }
        if (!has1) {
            return 0;
        }
        int cmp = Float.compare(f1.accuracy, f2.accuracy);
        return order == SortOrder.DESCENDING ? -cmp : cmp;
    }

    private int compareFletchingModifier(Material m1, Material m2) {
        FletchingMaterialStats f1 = m1.getStats(MaterialTypes.FLETCHING);
        FletchingMaterialStats f2 = m2.getStats(MaterialTypes.FLETCHING);
        boolean has1 = f1 != null;
        boolean has2 = f2 != null;
        if (has1 != has2) {
            return has1 ? -1 : 1;
        }
        if (!has1) {
            return 0;
        }
        int cmp = Float.compare(f1.modifier, f2.modifier);
        return order == SortOrder.DESCENDING ? -cmp : cmp;
    }

    private int compareGeneralModifier(Material m1, Material m2) {
        Float mod1 = getModifier(m1);
        Float mod2 = getModifier(m2);
        boolean has1 = mod1 != null;
        boolean has2 = mod2 != null;
        if (has1 != has2) {
            return has1 ? -1 : 1;
        }
        if (!has1) {
            return 0;
        }
        int cmp = Float.compare(mod1, mod2);
        return order == SortOrder.DESCENDING ? -cmp : cmp;
    }

    private Float getModifier(Material m) {
        if (targetMaterialType != null) {
            if (MaterialTypes.BOWSTRING.equals(targetMaterialType)) {
                BowStringMaterialStats s = m.getStats(MaterialTypes.BOWSTRING);
                if (s != null) return s.modifier;
            } else if (MaterialTypes.SHAFT.equals(targetMaterialType)) {
                ArrowShaftMaterialStats s = m.getStats(MaterialTypes.SHAFT);
                if (s != null) return s.modifier;
            } else if (MaterialTypes.FLETCHING.equals(targetMaterialType)) {
                FletchingMaterialStats s = m.getStats(MaterialTypes.FLETCHING);
                if (s != null) return s.modifier;
            }
        }
        BowStringMaterialStats bs = m.getStats(MaterialTypes.BOWSTRING);
        if (bs != null) return bs.modifier;
        ArrowShaftMaterialStats as = m.getStats(MaterialTypes.SHAFT);
        if (as != null) return as.modifier;
        FletchingMaterialStats fs = m.getStats(MaterialTypes.FLETCHING);
        if (fs != null) return fs.modifier;
        HandleMaterialStats hm = m.getStats(MaterialTypes.HANDLE);
        if (hm != null) return hm.modifier;
        return null;
    }
}
