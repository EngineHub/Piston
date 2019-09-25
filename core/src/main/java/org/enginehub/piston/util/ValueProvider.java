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

package org.enginehub.piston.util;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Provides a value, given a context argument.
 */
public interface ValueProvider<C, T> {

    static <C, T> ValueProvider<C, T> constant(@Nullable T value) {
        Optional<T> opt = Optional.ofNullable(value);
        return context -> opt;
    }

    /**
     * Compute the value from the context.
     *
     * @param context the context, never {@code null}
     * @return the value, may be {@link Optional#empty()} to indicate no value
     */
    Optional<T> value(C context);
}
