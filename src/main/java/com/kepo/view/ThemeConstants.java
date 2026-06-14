package com.kepo.view;

public final class ThemeConstants {

    private ThemeConstants() {}

    // === Color Hex Codes for CSS ===
    public static final String PRIMARY = "#006591";
    public static final String PRIMARY_LIGHT = "#0ea5e9";
    public static final String ON_PRIMARY = "#ffffff";
    
    public static final String SECONDARY = "#006c49";
    public static final String ON_SECONDARY = "#ffffff";
    
    public static final String BACKGROUND = "#f8fafc";
    public static final String SURFACE = "#ffffff";
    public static final String BORDER = "#e2e8f0";
    
    public static final String DANGER = "#ef4444";
    public static final String WARNING = "#f59e0b";
    public static final String INFO = "#3b82f6";
    
    public static final String ON_SURFACE = "#111c2d";
    public static final String ON_SURFACE_VARIANT = "#475569";

    // === Dark AI Styling ===
    public static final String AI_BG = "#111c2d";
    public static final String AI_SURFACE = "#1e293b";
    public static final String AI_BORDER = "#334155";
    public static final String AI_TEXT = "#f8fafc";
    public static final String AI_MUTED = "#94a3b8";

    // === Common Stylesheets ===
    public static final String CARD_STYLE = 
        "-fx-background-color: " + SURFACE + ";" +
        "-fx-background-radius: 12;" +
        "-fx-border-color: " + BORDER + ";" +
        "-fx-border-radius: 12;" +
        "-fx-border-width: 1;" +
        "-fx-effect: dropshadow(three-pass-box, rgba(17,28,45,0.05), 5, 0, 0, 1);";

    public static final String PRIMARY_BTN_STYLE =
        "-fx-background-color: " + PRIMARY + ";" +
        "-fx-text-fill: " + ON_PRIMARY + ";" +
        "-fx-font-weight: bold;" +
        "-fx-background-radius: 12;" +
        "-fx-cursor: hand;" +
        "-fx-padding: 8 16 8 16;";

    public static final String SECONDARY_BTN_STYLE =
        "-fx-background-color: " + SECONDARY + ";" +
        "-fx-text-fill: " + ON_SECONDARY + ";" +
        "-fx-font-weight: bold;" +
        "-fx-background-radius: 12;" +
        "-fx-cursor: hand;" +
        "-fx-padding: 8 16 8 16;";

    public static final String OUTLINE_BTN_STYLE =
        "-fx-background-color: transparent;" +
        "-fx-border-color: " + BORDER + ";" +
        "-fx-border-width: 1;" +
        "-fx-border-radius: 12;" +
        "-fx-text-fill: " + ON_SURFACE_VARIANT + ";" +
        "-fx-font-weight: bold;" +
        "-fx-background-radius: 12;" +
        "-fx-cursor: hand;" +
        "-fx-padding: 8 16 8 16;";

    public static final String DANGER_BTN_STYLE =
        "-fx-background-color: " + DANGER + ";" +
        "-fx-text-fill: #ffffff;" +
        "-fx-font-weight: bold;" +
        "-fx-background-radius: 12;" +
        "-fx-cursor: hand;" +
        "-fx-padding: 8 16 8 16;";

    public static final String INPUT_STYLE =
        "-fx-background-color: #f0f3ff;" +
        "-fx-background-radius: 12;" +
        "-fx-border-color: transparent;" +
        "-fx-text-fill: " + ON_SURFACE + ";" +
        "-fx-padding: 8 12 8 12;";

    // === Badge Styles ===
    public static final String BADGE_ACTIVE =
        "-fx-background-color: #e0f2fe;" +
        "-fx-text-fill: #0369a1;" +
        "-fx-font-weight: bold;" +
        "-fx-font-size: 11;" +
        "-fx-background-radius: 20;" +
        "-fx-padding: 4 10 4 10;";

    public static final String BADGE_CLOSED =
        "-fx-background-color: #f1f5f9;" +
        "-fx-text-fill: #475569;" +
        "-fx-font-weight: bold;" +
        "-fx-font-size: 11;" +
        "-fx-background-radius: 20;" +
        "-fx-padding: 4 10 4 10;";

    public static final String BADGE_CRITICAL =
        "-fx-background-color: #fee2e2;" +
        "-fx-text-fill: #b91c1c;" +
        "-fx-font-weight: bold;" +
        "-fx-font-size: 11;" +
        "-fx-background-radius: 20;" +
        "-fx-padding: 4 10 4 10;";

    public static final String BADGE_WARNING =
        "-fx-background-color: #fef3c7;" +
        "-fx-text-fill: #b45309;" +
        "-fx-font-weight: bold;" +
        "-fx-font-size: 11;" +
        "-fx-background-radius: 20;" +
        "-fx-padding: 4 10 4 10;";

    public static final String BADGE_SAFE =
        "-fx-background-color: #dcfce7;" +
        "-fx-text-fill: #15803d;" +
        "-fx-font-weight: bold;" +
        "-fx-font-size: 11;" +
        "-fx-background-radius: 20;" +
        "-fx-padding: 4 10 4 10;";
}
