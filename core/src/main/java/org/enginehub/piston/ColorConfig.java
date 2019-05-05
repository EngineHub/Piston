/*
 * Piston, a flexible command management system.
 * Copyright (C) EngineHub <http://www.enginehub.com>
 * Copyright (C) Piston contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.piston;

import net.kyori.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.Nullable;

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
