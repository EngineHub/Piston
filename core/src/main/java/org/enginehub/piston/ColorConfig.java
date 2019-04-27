package org.enginehub.piston;

import net.kyori.text.format.TextColor;

import javax.annotation.Nullable;

/**
 * Global color configuration.
 */
public class ColorConfig {

    @Nullable
    private static volatile TextColor TEXT_MODIFIER = TextColor.YELLOW;

    public static void setTextModifier(@Nullable TextColor textModifier) {
        TEXT_MODIFIER = textModifier;
    }

    /**
     * Color for text that modifies the main text.
     */
    @Nullable
    public static TextColor getTextModifier() {
        return TEXT_MODIFIER;
    }

    @Nullable
    private static volatile TextColor MAIN_TEXT = TextColor.GOLD;

    public static void setMainText(@Nullable TextColor mainText) {
        MAIN_TEXT = mainText;
    }

    /**
     * Color for a command part label.
     */
    @Nullable
    public static TextColor getMainText() {
        return MAIN_TEXT;
    }

    @Nullable
    private static volatile TextColor HELP_TEXT = TextColor.GRAY;

    public static void setHelpText(@Nullable TextColor helpText) {
        HELP_TEXT = helpText;
    }

    /**
     * Color for help text.
     */
    @Nullable
    public static TextColor getHelpText() {
        return HELP_TEXT;
    }

    @Nullable
    private static volatile TextColor PART_WRAPPING = TextColor.YELLOW;

    public static void setPartWrapping(@Nullable TextColor partWrapping) {
        PART_WRAPPING = partWrapping;
    }

    /**
     * Color for the wrapping text of a command part, e.g. {@code <>} or {@code []}.
     */
    @Nullable
    public static TextColor getPartWrapping() {
        return PART_WRAPPING;
    }
}
