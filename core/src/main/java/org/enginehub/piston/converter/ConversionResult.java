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

import org.enginehub.piston.inject.InjectedValueAccess;

import java.util.Collection;

/**
 * Represents the result of {@link ArgumentConverter#convert(String, InjectedValueAccess)}.
 *
 * <p>
 * This either wraps the result, or contains errors explaining why it didn't match.
 * </p>
 */
public abstract class ConversionResult<T> {

    ConversionResult() {
    }

    public abstract boolean isSuccessful();

    /**
     * If this result is a failure, recast as a different result type.
     *
     * <p>
     * This is useful for result collectors, where the result is a different
     * type than the inputs.
     * </p>
     */
    public abstract <U> ConversionResult<U> failureAsAny();

    /**
     * Pick the successful result, or merge the errors of the two unsuccessful results.
     */
    public abstract ConversionResult<T> orElse(ConversionResult<T> result);

    public final Collection<T> orElse(Collection<T> other) {
        if (isSuccessful()) {
            return get();
        }
        return other;
    }

    /**
     * Get the result, or throw an exception with all collected errors.
     */
    public abstract Collection<T> get();

}
