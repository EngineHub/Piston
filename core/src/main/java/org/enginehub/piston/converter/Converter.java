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

import java.util.Iterator;

/**
 * Simplified interface to {@link ArgumentConverter}. Only contains the conversion.
 */
@FunctionalInterface
public interface Converter<T> {

    /**
     * Converts the argument inputs to a collection of argument values.
     *
     * <p>
     * This method should be <em>pure</em>, i.e. it should not send any
     * error messages to anything in the {@code context}. This could
     * result in spurious error messages during parsing.
     * </p>
     *
     * <p>
     * The converter must not be over-eager in accepting arguments. Any arguments
     * consumed from the provided iterator will be considered taken by the converter if
     * a successful result is returned, and will not be passed to another converter.
     * </p>
     *
     * @param arguments the argument inputs to convert
     * @param context the context to convert in
     * @return the result of attempting to convert the argument
     */
    ConversionResult<T> convert(Iterator<String> arguments, InjectedValueAccess context);

}
