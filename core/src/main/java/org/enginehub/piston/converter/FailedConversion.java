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

import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

public final class FailedConversion<T> extends ConversionResult<T> {

    public static <T> FailedConversion<T> from(Throwable error) {
        return from(error, ImmutableSet.of());
    }

    public static <T> FailedConversion<T> from(Throwable error, Collection<FailedConversion<T>> otherFailures) {
        return new FailedConversion<>(error, otherFailures);
    }

    private final Throwable error;
    private final ImmutableSet<FailedConversion<T>> otherFailures;

    public FailedConversion(Throwable error,
                            Collection<FailedConversion<T>> otherFailures) {
        this.error = error;
        this.otherFailures = ImmutableSet.copyOf(otherFailures);
    }


    @Override
    public boolean isSuccessful() {
        return false;
    }

    @Override
    // we contain no T, so this is safe
    @SuppressWarnings("unchecked")
    public <U> ConversionResult<U> failureAsAny() {
        return (FailedConversion<U>) this;
    }

    @Override
    public ConversionResult<T> orElse(ConversionResult<T> result) {
        return result.isSuccessful()
            ? result
            : from(error, ImmutableSet.<FailedConversion<T>>builder()
            .addAll(otherFailures)
            .add((FailedConversion<T>) result)
            .build());
    }

    @Override
    public <U> ConversionResult<U> map(Function<? super Collection<T>, ? extends Collection<U>> mapper) {
        return failureAsAny();
    }

    public Throwable getError() {
        return error;
    }

    public ImmutableSet<FailedConversion<T>> getOtherFailures() {
        return otherFailures;
    }

    @Override
    public Collection<T> get() {
        throw FailedConversionMapper.mapOnto(NoSuchElementException::new, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FailedConversion<?> that = (FailedConversion<?>) o;
        return error.equals(that.error) &&
            otherFailures.equals(that.otherFailures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error, otherFailures);
    }
}
