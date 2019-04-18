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

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.util.CaseHelper;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodType.methodType;

/**
 * A collection of default argument converters.
 */
public class ArgumentConverters {

    private static final ArgumentConverter<String> STRING_ARGUMENT_CONVERTER =
        SimpleArgumentConverter.fromSingle((s, c) -> s, "any text");

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
                c, "valueOf", methodType(c, String.class)
            );
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return Optional.empty();
        }

        handle = noContextConverter(handle);

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
                c, methodType(void.class, String.class)
            );
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return Optional.empty();
        }

        handle = noContextConverter(handle);

        return Optional.of(converterForHandle(handle, c));
    }

    /**
     * Convert a handle with signature {@code (String)T} to {@code (String,InjectedValueAccess)T}.
     */
    private static MethodHandle noContextConverter(MethodHandle noContextHandle) {
        return dropArguments(noContextHandle, 1, InjectedValueAccess.class);
    }

    private static final String BI_FUNCTION_APPLY = "apply";
    private static final MethodType HANDLE_TO_BI_FUNCTION = methodType(BiFunction.class, MethodHandle.class);
    private static final MethodType BI_FUNCTION_RAW_SIG = methodType(Object.class, Object.class, Object.class);
    private static final MethodType BI_FUNCTION_SIG = methodType(Object.class, String.class, InjectedValueAccess.class);

    private static final MethodHandle HANDLE_TO_BI_FUNCTION_CONVERTER;

    static {
        MethodHandle handleInvoker = MethodHandles.invoker(BI_FUNCTION_SIG);
        try {
            HANDLE_TO_BI_FUNCTION_CONVERTER = LambdaMetafactory.metafactory(
                MethodHandles.lookup(),
                // Implementing BiFunction.apply
                BI_FUNCTION_APPLY,
                // Take a handle, to be converter to BiFunction
                HANDLE_TO_BI_FUNCTION,
                // Raw signature for SAM type
                BI_FUNCTION_RAW_SIG,
                // Handle to call the captured handle.
                handleInvoker,
                // Actual signature at invoke time
                BI_FUNCTION_SIG
            ).dynamicInvoker();
        } catch (LambdaConversionException e) {
            throw new IllegalStateException("Failed to load ArgumentConverter MetaFactory", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> SimpleArgumentConverter<T> converterForHandle(MethodHandle handle, Class<?> type) {
        MethodType mType = handle.type();
        checkArgument(mType.parameterType(0).isAssignableFrom(String.class)
                && mType.parameterType(1).isAssignableFrom(InjectedValueAccess.class)
                && type.isAssignableFrom(mType.returnType()),
            "Incorrect signature: %s", handle);
        BiFunction<String, InjectedValueAccess, T> biFunction;
        try {
            biFunction = (BiFunction<String, InjectedValueAccess, T>)
                HANDLE_TO_BI_FUNCTION_CONVERTER.invokeExact(handle);
        } catch (Throwable throwable) {
            Throwables.throwIfUnchecked(throwable);
            throw new RuntimeException(throwable);
        }
        return SimpleArgumentConverter.fromSingle(
            biFunction,
            "any " + CaseHelper.titleToSpacedLower(type.getSimpleName())
        );
    }

    private interface ACProvider<T> {
        Optional<ArgumentConverter<T>> provideAc(TypeToken<T> type);
    }

    private static final List<ACProvider<Object>> PROVIDERS = ImmutableList.of(
        ArgumentConverters::valueOfConverters,
        ArgumentConverters::constructorConverters,
        type -> {
            if (Objects.equals(type.wrap().getRawType(), Character.class)) {
                return Optional.of(SimpleArgumentConverter.fromSingle(
                    (s, c) -> s.charAt(0),
                    "any character"
                ));
            }
            return Optional.empty();
        }
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
