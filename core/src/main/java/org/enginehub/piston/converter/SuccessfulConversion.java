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

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

public final class SuccessfulConversion<T> extends ConversionResult<T> {

    public static <T> SuccessfulConversion<T> fromSingle(T result) {
        return from(ImmutableList.of(result));
    }

    public static <T> SuccessfulConversion<T> fromSingle(T result, boolean exactMatch) {
        return from(ImmutableList.of(result), exactMatch);
    }

    public static <T> SuccessfulConversion<T> from(Collection<T> result) {
        return new SuccessfulConversion<>(result, true);
    }

    public static <T> SuccessfulConversion<T> from(Collection<T> result, boolean exactMatch) {
        return new SuccessfulConversion<>(result, exactMatch);
    }

    private final Collection<T> result;
    private final boolean exactMatch;

    private SuccessfulConversion(Collection<T> result, boolean exactMatch) {
        this.result = result;
        this.exactMatch = exactMatch;
    }

    /**
     * Is this conversion an exact match for a complete input?
     *
     * <p>
     * This may be {@code false} if the conversion is a partial match, or if the input was
     * unknown and the conversion was a fallback.
     * </p>
     *
     * @return {@code true} if this conversion is an exact match
     * @since 0.5.8
     */
    public boolean isExactMatch() {
        return exactMatch;
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
    public <U> ConversionResult<U> map(Function<? super Collection<T>, ? extends Collection<U>> mapper) {
        Collection<U> mapped;
        try {
            mapped = mapper.apply(get());
        } catch (Throwable t) {
            return FailedConversion.from(t);
        }
        if (mapped == null) {
            return FailedConversion.from(new NullPointerException());
        }
        return from(mapped, exactMatch);
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
