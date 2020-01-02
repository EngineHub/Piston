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

package org.enginehub.piston.converter;

import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.enginehub.piston.inject.InjectedValueAccess;

import java.util.List;

/**
 * A simple base implementation of {@link ArgumentConverter}. Provides no suggestions,
 * but you can call {@link #withSuggestions(SuggestionProvider)} to supply them.
 */
public class SimpleArgumentConverter<T> implements ArgumentConverter<T> {

    public static <T> SimpleArgumentConverter<T> from(Converter<T> converter, String description) {
        return from(converter, TextComponent.of(description));
    }

    /**
     * Implements {@link ArgumentConverter#convert(String, InjectedValueAccess)} using the provided
     * function, and supplies the provided description for
     * {@link ArgumentConverter#describeAcceptableArguments()}.
     *
     * @param converter the converter function
     * @param description the acceptable arguments description
     * @param <T> the type of the argument
     * @return a converter using the given function and description
     */
    public static <T> SimpleArgumentConverter<T> from(Converter<T> converter, Component description) {
        return new SimpleArgumentConverter<>(converter, description);
    }

    private final Converter<T> converter;
    private final Component description;

    private SimpleArgumentConverter(Converter<T> converter, Component description) {
        this.converter = converter;
        this.description = description;
    }

    @Override
    public ConversionResult<T> convert(String argument, InjectedValueAccess context) {
        return converter.convert(argument, context);
    }

    @Override
    public Component describeAcceptableArguments() {
        return description;
    }

    public ArgumentConverter<T> withSuggestions(SuggestionProvider suggestions) {
        return new ForwardingArgumentConverter<T>(this) {
            @Override
            public List<String> getSuggestions(String input, InjectedValueAccess context) {
                return suggestions.getSuggestions(input, context);
            }
        };
    }
}
