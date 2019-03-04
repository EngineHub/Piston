/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) EngineHub <http://www.enginehub.com>
 * Copyright (C) oblique-commands contributors
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

import org.enginehub.piston.util.CaseHelper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A collection of default argument converters.
 */
public class ArgumentConverters {

    private static final ArgumentConverter<String> STRING_ARGUMENT_CONVERTER =
        SimpleArgumentConverter.fromSingle(Function.identity(), "any text");

    public static ArgumentConverter<String> forString() {
        return STRING_ARGUMENT_CONVERTER;
    }

    /**
     * Finds a method with signature {@code public static T valueOf(String)} and makes a converter
     * with description {@code "any T"}, or returns empty if no such method exists.
     */
    private static <T> Optional<ArgumentConverter<T>> valueOfConverters(TypeToken<T> type) {
        Class<?> c = type.wrap().getRawType();
        MethodHandle handle;
        try {
            handle = MethodHandles.publicLookup().findStatic(
                c, "valueOf", MethodType.methodType(c, String.class)
            );
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return Optional.empty();
        }

        return Optional.of(converterForHandle(handle, c));
    }

    /**
     * Finds a constructor with signature {@code public T(String)} and makes a converter
     * with description {@code "any T"}, or returns empty if no such constructor exists.
     */
    private static <T> Optional<ArgumentConverter<T>> constructorConverters(TypeToken<T> type) {
        Class<?> c = type.wrap().getRawType();
        MethodHandle handle;
        try {
            handle = MethodHandles.publicLookup().findConstructor(
                c, MethodType.methodType(void.class, String.class)
            );
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return Optional.empty();
        }

        return Optional.of(converterForHandle(handle, c));
    }

    private static <T> SimpleArgumentConverter<T> converterForHandle(MethodHandle handle, Class<?> type) {
        return SimpleArgumentConverter.fromSingle(
            arg -> {
                try {
                    // safe, the handle has a return type of `c`.
                    // `c`'s type is T.
                    @SuppressWarnings("unchecked")
                    T result = (T) handle.invokeExact(arg);
                    return result;
                } catch (Throwable throwable) {
                    Throwables.throwIfUnchecked(throwable);
                    throw new RuntimeException(throwable);
                }
            },
            "any " + CaseHelper.titleToSpacedLower(type.getSimpleName())
        );
    }

    private interface ACProvider<T> {
        Optional<ArgumentConverter<T>> provideAc(TypeToken<T> type);
    }

    private static final List<ACProvider<Object>> PROVIDERS = ImmutableList.of(
        ArgumentConverters::valueOfConverters,
        ArgumentConverters::constructorConverters
    );

    public static <T> ArgumentConverter<T> get(TypeToken<T> type) {
        if (type.getRawType().equals(String.class)) {
            @SuppressWarnings("unchecked")
            ArgumentConverter<T> stringConv = (ArgumentConverter<T>) forString();
            return stringConv;
        }
        // object is always common supertype, so this is fine
        @SuppressWarnings("unchecked")
        TypeToken<Object> raw = (TypeToken<Object>) type;
        // `type` was originally T, so the converter will provide T.
        @SuppressWarnings("unchecked")
        ArgumentConverter<T> result = (ArgumentConverter<T>) PROVIDERS.stream()
            .map(x -> x.provideAc(raw))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("No built-in converters for " + type));
        return result;
    }

    private ArgumentConverters() {
    }

}
