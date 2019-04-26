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

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.enginehub.piston.inject.InjectedValueAccess;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converts using a BiMap.
 */
public final class BiMapArgumentConverter<T> implements ArgumentConverter<T> {

    /**
     * Construct a converter for simple string choices from a set.
     */
    public static BiMapArgumentConverter<String> forChoices(Set<String> choices) {
        ImmutableBiMap.Builder<String, String> map = ImmutableBiMap.builder();
        choices.forEach(c -> map.put(c, c));
        return from(map.build());
    }

    public static <T> BiMapArgumentConverter<T> from(BiMap<String, T> map) {
        return new BiMapArgumentConverter<>(map);
    }

    private final ImmutableBiMap<String, T> map;

    private BiMapArgumentConverter(BiMap<String, T> map) {
        this.map = ImmutableBiMap.copyOf(map);
    }

    @Override
    public ConversionResult<T> convert(String argument, InjectedValueAccess context) {
        T result = map.get(argument);
        if (result == null) {
            return FailedConversion.from(new IllegalArgumentException("Invalid value: " + argument));
        }
        return SuccessfulConversion.fromSingle(result);
    }

    @Override
    public Component describeAcceptableArguments() {
        return TextComponent.of(String.join("|", map.keySet()));
    }

    @Override
    public List<String> getSuggestions(String input) {
        return map.keySet().stream()
            .filter(s -> s.startsWith(input))
            .collect(Collectors.toList());
    }
}
