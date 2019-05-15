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

import com.google.common.collect.ImmutableMap;
import org.enginehub.piston.util.ValueProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Memoizes accesses, so that only one value is used.
 */
public final class MemoizingValueAccess implements InjectedValueAccess {

    public static MemoizingValueAccess wrap(InjectedValueAccess delegate) {
        if (delegate instanceof MemoizingValueAccess) {
            return (MemoizingValueAccess) delegate;
        }
        return new MemoizingValueAccess(delegate);
    }

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Key<?>, Optional<?>> memory = new HashMap<>();
    private final InjectedValueAccess delegate;

    private MemoizingValueAccess(InjectedValueAccess delegate) {
        this.delegate = delegate;
    }

    /**
     * Snapshot the current memory for reading.
     */
    public InjectedValueAccess snapshotMemory() {
        ImmutableMap.Builder<Key<?>, ValueProvider<InjectedValueAccess, ?>> snapshot
            = ImmutableMap.builder();
        lock.readLock().lock();
        try {
            memory.forEach((k, v) -> snapshot.put(k, ValueProvider.constant(v.orElse(null))));
        } finally {
            lock.readLock().unlock();
        }
        return MapBackedValueStore.create(snapshot.build());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> injectedValue(Key<T> key, InjectedValueAccess context) {
        // Try to read out a value quickly
        lock.readLock().lock();
        try {
            Optional<?> result = memory.get(key);
            if (result != null) {
                return (Optional<T>) result;
            }
        } finally {
            lock.readLock().unlock();
        }
        // no dice -- exclusively compute it.
        lock.writeLock().lock();
        try {
            Optional<?> result = memory.get(key);
            if (result == null) {
                result = delegate.injectedValue(key, context);
                memory.put(key, result);
            }
            return (Optional<T>) result;
        } finally {
            lock.writeLock().unlock();
        }
    }

}
