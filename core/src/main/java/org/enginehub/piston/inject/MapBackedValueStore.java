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

package org.enginehub.piston.inject;

import org.enginehub.piston.util.ValueProvider;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link InjectedValueStore} backed by a {@link Map}.
 */
public final class MapBackedValueStore implements InjectedValueStore {

    public static MapBackedValueStore create() {
        return create(new ConcurrentHashMap<>());
    }

    public static MapBackedValueStore create(Map<Key<?>, ValueProvider<InjectedValueAccess, ?>> providers) {
        return new MapBackedValueStore(providers);
    }

    private final Map<Key<?>, ValueProvider<InjectedValueAccess, ?>> providers;

    private MapBackedValueStore(Map<Key<?>, ValueProvider<InjectedValueAccess, ?>> providers) {
        this.providers = providers;
    }

    @Override
    public <T> void injectValue(Key<T> key, ValueProvider<InjectedValueAccess, T> provider) {
        providers.put(key, provider);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> injectedValue(Key<T> key) {
        ValueProvider<InjectedValueAccess, T> provider = (ValueProvider<InjectedValueAccess, T>) providers.get(key);
        return Optional.ofNullable(provider).flatMap(p -> p.value(this));
    }
}
