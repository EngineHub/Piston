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

import com.google.common.base.Strings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Text configuration.
 */
public class TextConfig extends Config<String> {

    private static final TextConfig COMMAND_PREFIX = new TextConfig("piston.text.command.prefix");

    /**
     * Output command prefix -- all commands will be output with this prefix before their name.
     */
    public static TextConfig commandPrefix() {
        return COMMAND_PREFIX;
    }

    public static Component commandPrefixValue() {
        return commandPrefix().value();
    }

    private TextConfig(String key) {
        super(key, "");
    }

    @Override
    protected Config<String> copyForDefault() {
        return new TextConfig(getKey());
    }

    @Override
    public TextConfig value(@Nullable String value) {
        super.value(value);
        return this;
    }

    @Override
    protected void checkValue(@Nullable String value) {
        checkNotNull(value);
    }

    @Override
    protected Component apply(TranslatableComponent placeholder) {
        checkState(placeholder.args().isEmpty(), "TextConfig takes no arguments");
        return Component.text()
            .content(Strings.nullToEmpty(getValue()))
            .mergeStyle(placeholder)
            .append(placeholder.children())
            .build();
    }
}
