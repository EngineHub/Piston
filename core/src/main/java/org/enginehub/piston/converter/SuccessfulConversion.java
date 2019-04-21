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

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Objects;

public final class SuccessfulConversion<T> extends ConversionResult<T> {

    public static <T> SuccessfulConversion<T> fromSingle(T result) {
        return from(ImmutableList.of(result));
    }

    public static <T> SuccessfulConversion<T> from(Collection<T> result) {
        return new SuccessfulConversion<>(result);
    }

    private final Collection<T> result;

    private SuccessfulConversion(Collection<T> result) {
        this.result = result;
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public <U> ConversionResult<U> failureAsAny() {
        throw new IllegalStateException("This is not a failed conversion result.");
    }

    @Override
    public ConversionResult<T> orElse(ConversionResult<T> result) {
        return this;
    }

    @Override
    public Collection<T> get() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SuccessfulConversion<?> that = (SuccessfulConversion<?>) o;
        return result.equals(that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result);
    }
}
