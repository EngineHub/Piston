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

package org.enginehub.piston.converter;

import com.google.common.reflect.TypeToken;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.enginehub.piston.inject.InjectedValueAccess;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A simple base implementation of {@link ArgumentConverter}. Provides no suggestions,
 * but you can call {@link #withSuggestions(Function)} to supply them.
 */
public class BaseArgumentConverter<T> implements ArgumentConverter<T> {

    public static <T> BaseArgumentConverter<T> from(Converter<T> converter, String description,
                                                    TypeToken<? extends T> type) {
        return from(converter, TextComponent.of(description), type);
    }

    public static <T> BaseArgumentConverter<T> fromSingle(SimpleConverter<T> converter, String description,
                                                          TypeToken<? extends T> type) {
        return from(converter.asConverter(), description, type);
    }

    /**
     * Implements {@link ArgumentConverter#convert(Iterator, InjectedValueAccess)} using the
     * provided
     * function, and supplies the provided description for
     * {@link ArgumentConverter#describeAcceptableArguments()}.
     *
     * @param converter the converter function
     * @param description the acceptable arguments description
     * @param type the runtime representation of the type
     * @param <T> the type of the argument
     * @return a converter using the given function and description
     */
    public static <T> BaseArgumentConverter<T> from(Converter<T> converter, Component description,
                                                    TypeToken<? extends T> type) {
        return new BaseArgumentConverter<>(converter, description, type);
    }

    public static <T> BaseArgumentConverter<T> fromSingle(SimpleConverter<T> converter, Component description,
                                                          TypeToken<? extends T> type) {
        return from(converter.asConverter(), description, type);
    }

    private final Converter<T> converter;
    private final Component description;
    private final TypeToken<? extends T> type;

    protected BaseArgumentConverter(Converter<T> converter, Component description,
                                    TypeToken<? extends T> type) {
        this.converter = converter;
        this.description = description;
        this.type = type;
    }

    @Override
    public ConversionResult<T> convert(Iterator<String> arguments, InjectedValueAccess context) {
        return converter.convert(arguments, context);
    }

    @Override
    public Component describeAcceptableArguments() {
        return description;
    }

    @Override
    public TypeToken<? extends T> getType() {
        return type;
    }

    public ArgumentConverter<T> withSuggestions(Function<String, Stream<String>> suggestions) {
        return new ForwardingArgumentConverter<T>(this) {
            @Override
            public Stream<String> getSuggestions(String input) {
                return suggestions.apply(input);
            }
        };
    }
}
