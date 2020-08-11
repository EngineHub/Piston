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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Color configuration.
 */
public class ColorConfig extends Config<TextColor> {

    private static final ColorConfig TEXT_MODIFIER =
        new ColorConfig("piston.style.text.modifier", NamedTextColor.YELLOW);
    private static final ColorConfig MAIN_TEXT =
        new ColorConfig("piston.style.main.text", NamedTextColor.GOLD);
    private static final ColorConfig HELP_TEXT =
        new ColorConfig("piston.style.help.text", NamedTextColor.GRAY);
    private static final ColorConfig PART_WRAPPING =
        new ColorConfig("piston.style.part.wrapping", NamedTextColor.YELLOW);

    private static final Set<Style.Merge> MERGE_NO_COLOR = Style.Merge.of(
        Style.Merge.DECORATIONS, Style.Merge.INSERTION, Style.Merge.EVENTS
    );

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

    private ColorConfig(String key, @Nullable TextColor color) {
        super(key, color);
    }

    @Override
    protected Config<TextColor> copyForDefault() {
        return new ColorConfig(getKey(), getValue());
    }

    @Override
    public ColorConfig value(@Nullable TextColor value) {
        super.value(value);
        return this;
    }

    public Component wrap(String text) {
        return wrap(ImmutableList.of(TextComponent.of(text)));
    }

    public Component wrap(Component... args) {
        return wrap(Arrays.asList(args));
    }

    public Component wrap(List<Component> args) {
        return super.wrapInternal(args);
    }

    @Override
    protected Component apply(TranslatableComponent placeholder) {
        return renderFromArgs(placeholder.args())
            .mergeStyle(placeholder, MERGE_NO_COLOR)
            .append(placeholder.children())
            .build();
    }

    private TextComponent.Builder renderFromArgs(List<Component> args) {
        TextColor color = getValue();
        switch (args.size()) {
            case 0:
                return TextComponent.builder("", color);
            case 1:
                Component only = args.get(0);
                if (only instanceof TextComponent) {
                    return ((TextComponent) only).toBuilder().color(color);
                }
                // fall-through
            default:
                return TextComponent.builder()
                    .color(color)
                    .append(args);
        }
    }
}
