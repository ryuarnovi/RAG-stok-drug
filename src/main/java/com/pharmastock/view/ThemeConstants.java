package com.pharmastock.view;

import java.awt.*;

/**
 * Design tokens dan konstanta tema PharmaStock.
 * Warna, font, dan dimensi mengikuti desain UI yang diberikan.
 */
public final class ThemeConstants {

    private ThemeConstants() {}

    // === Warna Utama ===
    public static final Color PRIMARY = Color.decode("#006591");
    public static final Color PRIMARY_CONTAINER = Color.decode("#0EA5E9");
    public static final Color ON_PRIMARY = Color.decode("#FFFFFF");
    public static final Color ON_PRIMARY_CONTAINER = Color.decode("#003751");

    public static final Color SECONDARY = Color.decode("#006C49");
    public static final Color SECONDARY_CONTAINER = Color.decode("#6CF8BB");
    public static final Color ON_SECONDARY = Color.decode("#FFFFFF");
    public static final Color ON_SECONDARY_CONTAINER = Color.decode("#00714D");

    // === Warna Permukaan ===
    public static final Color BACKGROUND = Color.decode("#F8FAFC");
    public static final Color SURFACE = Color.decode("#FFFFFF");
    public static final Color SURFACE_CONTAINER_LOW = Color.decode("#F0F3FF");
    public static final Color SURFACE_CONTAINER = Color.decode("#E7EEFF");
    public static final Color SURFACE_CONTAINER_HIGH = Color.decode("#DEE8FF");
    public static final Color ON_SURFACE = Color.decode("#111C2D");
    public static final Color ON_SURFACE_VARIANT = Color.decode("#3E4850");

    // === Warna Status ===
    public static final Color DANGER = Color.decode("#EF4444");
    public static final Color WARNING = Color.decode("#F59E0B");
    public static final Color SUCCESS = SECONDARY;
    public static final Color ERROR = Color.decode("#BA1A1A");

    // === Warna Border & Outline ===
    public static final Color BORDER = Color.decode("#E2E8F0");
    public static final Color OUTLINE = Color.decode("#6E7881");
    public static final Color OUTLINE_VARIANT = Color.decode("#BEC8D2");

    // === Warna Inverse ===
    public static final Color INVERSE_SURFACE = Color.decode("#263143");
    public static final Color INVERSE_ON_SURFACE = Color.decode("#ECF1FF");

    // === Warna dengan Transparansi ===
    public static final Color PRIMARY_TINT_10 = new Color(0, 101, 145, 26);
    public static final Color SECONDARY_TINT_10 = new Color(0, 108, 73, 26);
    public static final Color WARNING_TINT_10 = new Color(245, 158, 11, 26);
    public static final Color DANGER_TINT_10 = new Color(239, 68, 68, 26);
    public static final Color PRIMARY_CONTAINER_TINT_10 = new Color(14, 165, 233, 26);

    // === Font ===
    public static final String FONT_HEADLINE = "Plus Jakarta Sans";
    public static final String FONT_BODY = "Inter";
    public static final String FONT_FALLBACK = "SansSerif";

    public static Font fontHeadlineLg() {
        return new Font(FONT_HEADLINE, Font.BOLD, 24);
    }

    public static Font fontHeadlineMd() {
        return new Font(FONT_HEADLINE, Font.BOLD, 20);
    }

    public static Font fontTitleMd() {
        return new Font(FONT_BODY, Font.BOLD, 16);
    }

    public static Font fontBodyMd() {
        return new Font(FONT_BODY, Font.PLAIN, 14);
    }

    public static Font fontBodySm() {
        return new Font(FONT_BODY, Font.PLAIN, 12);
    }

    public static Font fontLabelMd() {
        return new Font(FONT_BODY, Font.BOLD, 12);
    }

    public static Font fontLabelSm() {
        return new Font(FONT_BODY, Font.BOLD, 10);
    }

    // === Dimensi ===
    public static final int RADIUS_XL = 12;
    public static final int RADIUS_LG = 8;
    public static final int RADIUS_FULL = 9999;
    public static final int PADDING = 16;
    public static final int PADDING_SM = 8;
    public static final int GAP = 12;
    public static final int TOUCH_TARGET = 44;

    // === Sidebar ===
    public static final int SIDEBAR_WIDTH = 240;
    public static final Color SIDEBAR_BG = Color.decode("#FFFFFF"); // Putih bersih
    public static final Color SIDEBAR_TEXT = Color.decode("#475569"); // Slate gray
    public static final Color SIDEBAR_ACTIVE_BG = Color.decode("#F0F9FF"); // Light sky blue
    public static final Color SIDEBAR_ACTIVE_BORDER = Color.decode("#0EA5E9"); // Sky blue border

    // === Shadow ===
    public static final Color SHADOW = new Color(17, 28, 45, 13);
}
