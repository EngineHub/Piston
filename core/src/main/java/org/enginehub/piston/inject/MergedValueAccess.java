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

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Optional;

/**
 * Combination between multiple {@link InjectedValueStore} instances.
 * Order matters.
 *
 * <p>
 * This does not perform caching, so it may be wise to wrap it in a {@link MemoizingValueAccess}.
 * </p>
 */
public final class MergedValueAccess implements InjectedValueAccess {

    public static MergedValueAccess of(InjectedValueAccess... delegates) {
        return of(ImmutableList.copyOf(delegates));
    }

    public static MergedValueAccess of(Collection<? extends InjectedValueAccess> delegates) {
        return new MergedValueAccess(delegates);
    }

    private final ImmutableList<InjectedValueAccess> delegates;

    private MergedValueAccess(Collection<? extends InjectedValueAccess> delegates) {
        this.delegates = ImmutableList.copyOf(delegates);
    }

    @Override
    public <T> Optional<T> injectedValue(Key<T> key) {
        return delegates.stream()
            .map(access -> access.injectedValue(key))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }
}
