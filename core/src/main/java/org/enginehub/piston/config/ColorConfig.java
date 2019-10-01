/*
 * Piston, a flexible command management system.
 * Copyright (C) EngineHub <https://www.enginehub.org>
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

package org.enginehub.piston.config;

import com.google.common.collect.ImmutableList;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;

import java.util.List;

/**
 * Color configuration.
 */
public class ColorConfig extends Config<TextColor> {

    private static final ColorConfig TEXT_MODIFIER =
        new ColorConfig("piston.style.text.modifier", TextColor.YELLOW);
    private static final ColorConfig MAIN_TEXT =
        new ColorConfig("piston.style.main.text", TextColor.GOLD);
    private static final ColorConfig HELP_TEXT =
        new ColorConfig("piston.style.help.text", TextColor.GRAY);
    private static final ColorConfig PART_WRAPPING =
        new ColorConfig("piston.style.part.wrapping", TextColor.YELLOW);

    /**
     * Color for text that modifies the main text.
     */
    public static ColorConfig textModifier() {
        return TEXT_MODIFIER;
    }

    /**
     * Color for a command part label.
     */
    public static ColorConfig mainText() {
        return MAIN_TEXT;
    }

    /**
     * Color for help text.
     */
    public static ColorConfig helpText() {
        return HELP_TEXT;
    }

    /**
     * Color for the wrapping text of a command part, e.g. {@code <>} or {@code []}.
     */
    public static ColorConfig partWrapping() {
        return PART_WRAPPING;
    }

    private ColorConfig(String key, TextColor color) {
        super(key, color);
    }

    @Override
    protected Config<TextColor> copyForDefault() {
        return new ColorConfig(getKey(), getValue());
    }

    @Override
    public ColorConfig value(TextColor value) {
        super.value(value);
        return this;
    }

    public Component wrap(String text) {
        return wrap(ImmutableList.of(TextComponent.of(text)));
    }

    @Override
    protected Component apply(List<Component> input) {
        TextColor color = getValue();
        switch (input.size()) {
            case 0:
                return TextComponent.of("", color);
            case 1:
                return input.get(0).color(color);
            default:
                return TextComponent.builder()
                    .color(color)
                    .append(input)
                    .build();
        }
    }
}
