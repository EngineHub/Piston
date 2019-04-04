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

package org.enginehub.piston.optimize;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents an optimization over a collection.
 *
 * @param <T>
 */
@FunctionalInterface
public interface CollectionOptimization<T> extends Optimization<Collection<T>> {

    @Override
    default Collection<T> optimize(Collection<T> input) {
        Collection<T> out = new ArrayList<>();
        for (T t : input) {
            out.add(optimizeSingle(t));
        }
        return out;
    }

    T optimizeSingle(T input);

}
