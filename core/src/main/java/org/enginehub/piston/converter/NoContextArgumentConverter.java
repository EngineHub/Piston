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

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Argument converter that supports working without a context.
 *
 * @param <T> the type of the result
 */
public interface NoContextArgumentConverter<T> extends ArgumentConverter<T> {

    /**
     * Converts the argument input to a collection of argument values.
     *
     * <p>
     * If it can't be converted, return {@code null}.
     * </p>
     *
     * @param argument the argument input to convert
     * @return the argument values
     */
    @Nullable
    Collection<T> convert(String argument);

    @Nullable
    @Override
    default Collection<T> convert(String argument, InjectedValueAccess context) {
        return convert(argument);
    }

}
