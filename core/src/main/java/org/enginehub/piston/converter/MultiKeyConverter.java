/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.enginehub.piston.ColorConfig;
import org.enginehub.piston.inject.InjectedValueAccess;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static org.enginehub.piston.util.ComponentHelper.joiningWithBar;

public class MultiKeyConverter<E> implements ArgumentConverter<E> {

    public static <E> MultiKeyConverter<E> from(Collection<E> items,
                                                Function<E, Set<String>> lookupKeys) {
        return from(items, lookupKeys, null);
    }

    public static <E> MultiKeyConverter<E> from(Collection<E> items,
                                                Function<E, Set<String>> lookupKeys,
                                                @Nullable E unknownValue) {
        return new MultiKeyConverter<>(items, lookupKeys, unknownValue);
    }

    private final Component choices;
    private final ImmutableSet<String> primaryKeys;
    private final ImmutableMap<String, E> map;
    @Nullable
    private final E unknownValue;

    private MultiKeyConverter(Collection<E> items,
                              Function<E, Set<String>> lookupKeys,
                              @Nullable E unknownValue) {
        ImmutableSortedMap.Builder<String, E> map = ImmutableSortedMap.orderedBy(String.CASE_INSENSITIVE_ORDER);
        ImmutableSet.Builder<String> primaryKeysBuilder = ImmutableSet.builder();
        for (E e : Iterables.filter(items, it -> it != unknownValue)) {
            Set<String> keys = lookupKeys.apply(e);
            checkState(keys.size() > 0, "No lookup keys for value %s", e);
            primaryKeysBuilder.add(keys.iterator().next());
            for (String key : keys) {
                map.put(key, e);
            }
        }
        this.primaryKeys = primaryKeysBuilder.build();
        this.choices = primaryKeys.stream()
            .map(choice -> TextComponent.of(choice, ColorConfig.getMainText()))
            .collect(joiningWithBar());
        this.map = map.build();
        this.unknownValue = unknownValue;
    }

    @Override
    public Component describeAcceptableArguments() {
        return choices;
    }

    @Override
    public List<String> getSuggestions(String input) {
        return primaryKeys.stream()
            .filter(s -> s.startsWith(input))
            .collect(Collectors.toList());
    }

    @Override
    public ConversionResult<E> convert(String argument, InjectedValueAccess context) {
        E result = map.getOrDefault(argument, unknownValue);
        return result == null
            ? FailedConversion.from(new IllegalArgumentException("Not a valid choice: " + argument))
            : SuccessfulConversion.fromSingle(result);
    }
}
