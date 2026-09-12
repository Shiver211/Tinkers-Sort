package com.shiver.tinkers_sort.integration;

import net.minecraftforge.fml.common.Loader;

import java.text.Collator;

public class JechHelper {

    private static Boolean jechLoaded = null;

    public static boolean isJechLoaded() {
        if (jechLoaded == null) {
            try {
                Class.forName("me.towdium.jecharacters.util.Match");
                jechLoaded = Loader.isModLoaded("jecharacters");
            } catch (Throwable t) {
                jechLoaded = false;
            }
        }
        return jechLoaded;
    }

    public static void setForceLoadedForTest(Boolean loaded) {
        jechLoaded = loaded;
    }

    public static boolean contains(String target, String query) {
        if (target == null || query == null) return false;
        if (query.isEmpty()) return true;

        if (isJechLoaded()) {
            try {
                return JechAdapter.contains(target, query);
            } catch (Throwable t) {
                // fallback on error
            }
        }
        return target.toLowerCase().contains(query.toLowerCase());
    }

    public static String toPinyin(String text) {
        if (text == null || text.isEmpty()) return "";
        if (isJechLoaded()) {
            try {
                return JechAdapter.toPinyin(text);
            } catch (Throwable t) {
                // fallback on error
            }
        }
        return text;
    }

    public static int compare(String s1, String s2, Collator collator) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return 1;
        if (s2 == null) return -1;

        if (isJechLoaded()) {
            try {
                String py1 = toPinyin(s1);
                String py2 = toPinyin(s2);
                int cmp = py1.compareToIgnoreCase(py2);
                if (cmp != 0) {
                    return cmp;
                }
            } catch (Throwable ignored) {}
        }
        return collator.compare(s1, s2);
    }

    /**
     * Isolated inner class to avoid ClassNotFoundException when JECH is not installed.
     */
    private static class JechAdapter {

        static boolean contains(String target, String query) {
            return me.towdium.jecharacters.util.Match.contains(target, query);
        }

        static String toPinyin(String text) {
            if (text == null || text.isEmpty()) return "";
            me.towdium.pinin.PinIn context = me.towdium.jecharacters.util.Match.context;
            if (context == null) return text;

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                // Strip Minecraft formatting code (§x)
                if (c == '\u00A7' && i + 1 < text.length()) {
                    i++;
                    continue;
                }
                if (c >= 0x4E00 && c <= 0x9FA5) {
                    me.towdium.pinin.elements.Char ch = context.getChar(c);
                    if (ch != null) {
                        me.towdium.pinin.elements.Pinyin[] pinyins = ch.pinyins();
                        if (pinyins != null && pinyins.length > 0) {
                            String py = pinyins[0].toString();
                            // strip trailing tone numbers like "gu3" -> "gu"
                            int len = py.length();
                            while (len > 0 && Character.isDigit(py.charAt(len - 1))) {
                                len--;
                            }
                            sb.append(py.substring(0, len));
                            continue;
                        }
                    }
                }
                sb.append(Character.toLowerCase(c));
            }
            return sb.toString();
        }
    }
}

