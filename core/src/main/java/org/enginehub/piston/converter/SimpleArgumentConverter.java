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

import org.enginehub.piston.inject.InjectedValueAccess;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Collections.singleton;

/**
 * A simple base implementation of {@link ArgumentConverter}. Provides no suggestions,
 * but you can call {@link #withSuggestions(Function)} to supply them.
 */
public class SimpleArgumentConverter<T> implements ArgumentConverter<T> {

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
    public static <T> SimpleArgumentConverter<T> from(
        BiFunction<String, InjectedValueAccess, Collection<T>> converter,
        String description
    ) {
        return new SimpleArgumentConverter<>(converter, description);
    }

    /**
     * Implements {@link ArgumentConverter#convert(String, InjectedValueAccess)} by wrapping the
     * result of
     * {@code converter} in a {@linkplain Collections#singleton(Object) singleton set},
     * and supplies the provided description for
     * {@link ArgumentConverter#describeAcceptableArguments()}.
     *
     * @param converter the converter function
     * @param description the acceptable arguments description
     * @param <T> the type of the argument
     * @return a converter using the given function and description
     */
    public static <T> SimpleArgumentConverter<T> fromSingle(
        BiFunction<String, InjectedValueAccess, T> converter,
        String description
    ) {
        return from((x, c) -> singleton(converter.apply(x, c)), description);
    }

    private final BiFunction<String, InjectedValueAccess, Collection<T>> converter;
    private final String description;

    private SimpleArgumentConverter(BiFunction<String, InjectedValueAccess, Collection<T>> converter, String description) {
        this.converter = converter;
        this.description = description;
    }

    @Nullable
    @Override
    public Collection<T> convert(String argument, InjectedValueAccess context) {
        return converter.apply(argument, context);
    }

    @Override
    public String describeAcceptableArguments() {
        return description;
    }

    public ArgumentConverter<T> withSuggestions(Function<String, List<String>> suggestions) {
        return new ForwardingArgumentConverter<T>(this) {
            @Override
            public List<String> getSuggestions(String input) {
                return suggestions.apply(input);
            }
        };
    }
}
