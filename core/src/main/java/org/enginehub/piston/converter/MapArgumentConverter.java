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

import com.google.common.collect.ImmutableMap;
import net.kyori.text.Component;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.inject.InjectedValueAccess;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.enginehub.piston.converter.SuggestionHelper.limitByPrefix;
import static org.enginehub.piston.util.ComponentHelper.joiningWithBar;

/**
 * Converts using a Map.
 */
public final class MapArgumentConverter<T> implements ArgumentConverter<T> {

    /**
     * Construct a converter for simple string choices from a set.
     */
    public static MapArgumentConverter<String> forChoices(Set<String> choices) {
        ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
        choices.forEach(c -> map.put(c, c));
        return from(map.build());
    }

    public static <T> MapArgumentConverter<T> from(Map<String, T> map) {
        return new MapArgumentConverter<>(map);
    }

    private final ImmutableMap<String, T> map;

    private MapArgumentConverter(Map<String, T> map) {
        this.map = ImmutableMap.copyOf(map);
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
        return map.keySet().stream()
            .map(ColorConfig.mainText()::wrap)
            .collect(joiningWithBar());
    }

    @Override
    public List<String> getSuggestions(String input, InjectedValueAccess context) {
        return limitByPrefix(map.keySet().stream(), input);
    }
}
