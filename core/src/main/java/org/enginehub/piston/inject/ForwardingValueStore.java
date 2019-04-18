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

import java.util.Optional;

/**
 * Forwards all calls to the delegate.
 */
public class ForwardingValueStore implements InjectedValueStore {
    protected final InjectedValueStore delegate;

    public ForwardingValueStore(InjectedValueStore delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> void injectValue(Key<T> key, ValueProvider<InjectedValueAccess, T> provider) {
        delegate.injectValue(key, provider);
    }

    @Override
    public <T> Optional<T> injectedValue(Key<T> key) {
        return delegate.injectedValue(key);
    }
}
