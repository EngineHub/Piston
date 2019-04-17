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

import com.google.inject.Key;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Memoizes accesses, so that only one value is used.
 */
public final class MemoizingValueAccess implements InjectedValueAccess {

    public static MemoizingValueAccess wrap(InjectedValueAccess delegate) {
        return new MemoizingValueAccess(delegate);
    }

    private final Map<Key<?>, Optional<?>> memory = new ConcurrentHashMap<>();
    private final InjectedValueAccess delegate;

    private MemoizingValueAccess(InjectedValueAccess delegate) {
        this.delegate = delegate;
    }

    /**
     * Get an injected value, but only if it is currently memoized.
     *
     * @return the value, or {@link Optional#empty()} if not provided and memoized
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> injectedValueIfMemoized(Key<T> key) {
        return (Optional<T>) memory.getOrDefault(key, Optional.empty());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> injectedValue(Key<T> key) {
        return (Optional<T>) memory.computeIfAbsent(key, delegate::injectedValue);
    }
}
