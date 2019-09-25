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

import org.enginehub.piston.inject.Key;

/**
 * Store for {@link ArgumentConverter ArgumentConverters}.
 */
public interface ArgumentConverterStore extends ArgumentConverterAccess {

    /**
     * Register a converter for a given key.
     *
     * @param key the key to register the converter under
     * @param converter the converter to register
     * @param <T> the type of value returned by the converter
     */
    <T> void registerConverter(Key<T> key, ArgumentConverter<T> converter);

}
