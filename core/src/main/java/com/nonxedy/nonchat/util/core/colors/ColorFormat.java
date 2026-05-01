package com.nonxedy.nonchat.util.core.colors;

import java.util.EnumSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public enum ColorFormat {

    /** {@code &a}, {@code &l}, {@code §a}, etc. */
    LEGACY {
        @Override public boolean presentIn(String s) {
            return ColorUtil.LEGACY_COLOR_PATTERN.matcher(s).find()
                || ColorUtil.SECTION_COLOR_PATTERN.matcher(s).find();
        }
        @Override public String strip(String s) {
            s = ColorUtil.LEGACY_COLOR_PATTERN.matcher(s).replaceAll("");
            return ColorUtil.SECTION_COLOR_PATTERN.matcher(s).replaceAll("");
        }
    },

    /** {@code &#RRGGBB} */
    AMPERSAND_HEX {
        @Override public boolean presentIn(String s) {
            return ColorUtil.HEX_PATTERN.matcher(s).find()
                || ColorUtil.AMPERSAND_HEX_PATTERN.matcher(s).find();
        }
        @Override public String strip(String s) {
            s = ColorUtil.HEX_PATTERN.matcher(s).replaceAll("");
            return ColorUtil.AMPERSAND_HEX_PATTERN.matcher(s).replaceAll("");
        }
    },

    /** {@code §x§R§G§B§R§G§B} (BungeeCord section hex) */
    BUNGEE_HEX {
        @Override public boolean presentIn(String s) {
            return ColorUtil.BUNGEE_HEX_PATTERN.matcher(s).find();
        }
        @Override public String strip(String s) {
            return ColorUtil.BUNGEE_HEX_PATTERN.matcher(s).replaceAll("");
        }
    },

    /** {@code <red>}, {@code <#FFFFFF>}, {@code <gradient:...>}, etc. */
    MINI_MESSAGE {
        @Override public boolean presentIn(String s) {
            return ColorUtil.MINIMESSAGE_TAG_PATTERN.matcher(s).find();
        }
        @Override public String strip(String s) {
            return ColorUtil.MINIMESSAGE_TAG_PATTERN.matcher(s).replaceAll("");
        }
    },

    /** No formatting detected. */
    NONE {
        @Override public boolean presentIn(String s) { return false; }
        @Override public String strip(String s) { return s; }
    };

    /** Returns {@code true} if this format is present in the given string. */
    public abstract boolean presentIn(String s);

    /** Removes occurrences of this format from the string. */
    public abstract String strip(String s);

    /** Detects the primary (first matched) format in {@code s}. */
    public static ColorFormat detect(@NotNull String s) {
        for (ColorFormat fmt : values()) {
            if (fmt != NONE && fmt.presentIn(s)) return fmt;
        }
        return NONE;
    }

    /** Returns every format present in {@code s}. */
    public static Set<ColorFormat> detectAll(@NotNull String s) {
        EnumSet<ColorFormat> result = EnumSet.noneOf(ColorFormat.class);
        for (ColorFormat fmt : values()) {
            if (fmt != NONE && fmt.presentIn(s)) result.add(fmt);
        }
        return result.isEmpty() ? EnumSet.of(NONE) : result;
    }
}
