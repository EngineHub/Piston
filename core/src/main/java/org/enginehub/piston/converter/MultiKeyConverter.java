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

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import net.kyori.text.Component;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.inject.InjectedValueAccess;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Multimaps.asMap;
import static org.enginehub.piston.converter.SuggestionHelper.limitByPrefix;
import static org.enginehub.piston.util.ComponentHelper.joiningWithBar;

public class MultiKeyConverter<E> implements ArgumentConverter<E> {

    public static <E> Builder<E> builder(SetMultimap<E, String> items) {
        return new AutoValue_MultiKeyConverter_Arguments.Builder<E>()
            .errorMessage(arg -> "Not a valid argument: " + arg)
            .items(items);
    }

    public static <E> Builder<E> builder(Collection<E> items,
                                         Function<E, Set<String>> lookupKeys) {
        ImmutableSetMultimap.Builder<E, String> map = ImmutableSetMultimap.builder();
        for (E item : items) {
            map.putAll(item, lookupKeys.apply(item));
        }
        return builder(map.build());
    }

    public static <E> MultiKeyConverter<E> from(SetMultimap<E, String> items) {
        return from(items, null);
    }

    public static <E> MultiKeyConverter<E> from(Collection<E> items,
                                                Function<E, Set<String>> lookupKeys) {
        return from(items, lookupKeys, null);
    }

    public static <E> MultiKeyConverter<E> from(SetMultimap<E, String> items,
                                                @Nullable E unknownValue) {
        return builder(items).unknownValue(unknownValue).build();
    }

    public static <E> MultiKeyConverter<E> from(Collection<E> items,
                                                Function<E, Set<String>> lookupKeys,
                                                @Nullable E unknownValue) {
        return builder(items, lookupKeys).unknownValue(unknownValue).build();
    }

    public interface Builder<E> {
        Builder<E> items(SetMultimap<E, String> items);

        Builder<E> unknownValue(@Nullable E unknownValue);

        Builder<E> errorMessage(UnaryOperator<String> handler);

        MultiKeyConverter<E> build();
    }

    @AutoValue
    abstract static class Arguments<E> {

        @AutoValue.Builder
        interface Builder<E> extends MultiKeyConverter.Builder<E> {

            @Override
            Builder<E> items(SetMultimap<E, String> items);

            @Override
            Builder<E> unknownValue(@Nullable E unknownValue);

            @Override
            Builder<E> errorMessage(UnaryOperator<String> handler);

            Arguments<E> autoBuild();

            @Override
            default MultiKeyConverter<E> build() {
                return new MultiKeyConverter<>(autoBuild());
            }
        }

        abstract ImmutableSetMultimap<E, String> items();

        @Nullable
        abstract E unknownValue();

        abstract UnaryOperator<String> errorMessage();

    }

    private final Component choices;
    private final ImmutableSet<String> primaryKeys;
    private final ImmutableMap<String, E> map;
    @Nullable
    private final E unknownValue;
    private final UnaryOperator<String> errorMessage;

    private MultiKeyConverter(Arguments<E> arguments) {
        ImmutableSortedMap.Builder<String, E> map = ImmutableSortedMap.orderedBy(String.CASE_INSENSITIVE_ORDER);
        ImmutableSet.Builder<String> primaryKeysBuilder = ImmutableSet.builder();
        Maps.filterKeys(asMap(arguments.items()), k -> k != arguments.unknownValue())
            .forEach((item, keys) -> {
                checkState(keys.size() > 0, "No lookup keys for value %s", item);
                primaryKeysBuilder.add(keys.iterator().next());
                for (String key : keys) {
                    map.put(key, item);
                }
            });
        this.primaryKeys = primaryKeysBuilder.build();
        this.choices = primaryKeys.stream()
            .map(ColorConfig.mainText()::wrap)
            .collect(joiningWithBar());
        this.map = map.build();
        this.unknownValue = arguments.unknownValue();
        this.errorMessage = arguments.errorMessage();
    }

    @Override
    public Component describeAcceptableArguments() {
        return choices;
    }

    @Override
    public List<String> getSuggestions(String input, InjectedValueAccess context) {
        return limitByPrefix(primaryKeys.stream(), input);
    }

    @Override
    public ConversionResult<E> convert(String argument, InjectedValueAccess context) {
        E result = map.getOrDefault(argument, unknownValue);
        return result == null
            ? FailedConversion.from(new IllegalArgumentException(errorMessage.apply(argument)))
            : SuccessfulConversion.fromSingle(result);
    }
}
