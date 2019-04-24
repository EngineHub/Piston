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
import org.enginehub.piston.inject.InjectedValueAccess;

import java.util.Collection;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;

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

    /**
     * If successful, map the result using the given mapper. Otherwise,
     * return {@link #failureAsAny()}.
     *
     * <p>
     * If {@code mapper} returns {@code null}, this becomes a failure,
     * with a {@link NullPointerException} as the reason.
     * </p>
     *
     * <p>
     * If {@code mapper} throws, this becomes a failure,
     * with the exception as the reason.
     * </p>
     *
     * @param mapper the function to call if successful
     * @param <U> the new type
     * @return the new result
     */
    public abstract <U> ConversionResult<U> map(Function<? super Collection<T>, ? extends Collection<U>> mapper);

    public final <U> ConversionResult<U> mapSingle(Function<? super T, ? extends U> mapper) {
        return map(many -> {
            checkArgument(many.size() == 1, "Need exactly one result");
            U result = mapper.apply(many.iterator().next());
            return result == null ? null : ImmutableList.of(result);
        });
    }

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
