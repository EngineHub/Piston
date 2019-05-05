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

package org.enginehub.piston;

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.enginehub.piston.inject.Key;

import static com.google.common.base.Preconditions.checkState;

/**
 * All values provided from the command line for a part. They can be converted to a type, if the
 * appropriate converter is registered with the manager.
 */
public interface CommandValue {

    /**
     * Get all the string values.
     */
    ImmutableList<String> asStrings();

    /**
     * Get a single string value. Throws if multiple values are present.
     *
     * @throws IllegalStateException if not exactly one value is present
     */
    default String asString() {
        ImmutableList<String> values = asStrings();
        checkState(values.size() > 0, "No value present");
        checkState(values.size() == 1, "Too many values present");
        return values.get(0);
    }

    /**
     * Convert the string values using the converted registered under the
     * given key.
     *
     * @param key the key for the converter
     * @param <T> the type of the values
     * @return the converted values
     * @see #asMultiple(Key)
     */
    default <T> ImmutableList<T> asMultiple(Class<T> key) {
        return asMultiple(Key.of(key));
    }

    /**
     * Convert the string values using the converted registered under the
     * given key.
     *
     * @param key the key for the converter
     * @param <T> the type of the values
     * @return the converted values
     */
    <T> ImmutableList<T> asMultiple(Key<T> key);

    /**
     * Convert the string value using the converted registered under the
     * given key. Throws if multiple values are present.
     *
     * @param key the key for the converter
     * @param <T> the type of the value
     * @return the converted value
     * @throws IllegalStateException if not exactly one value is present
     * @see #asString()
     * @see #asSingle(Key)
     */
    @Nullable
    default <T> T asSingle(Class<T> key) {
        return asSingle(Key.of(key));
    }

    /**
     * Convert the string value using the converted registered under the
     * given key. Throws if multiple values are present.
     *
     * @param key the key for the converter
     * @param <T> the type of the value
     * @return the converted value
     * @throws IllegalStateException if not exactly one value is present
     * @see #asString()
     */
    @Nullable
    default <T> T asSingle(Key<T> key) {
        ImmutableList<T> values = asMultiple(key);
        if (values.size() == 0 && asStrings().size() > 0) {
            // special case -- this means that all values were "null", so use null here
            return null;
        }
        checkState(values.size() > 0, "No value present");
        checkState(values.size() == 1, "Too many values present");
        return values.get(0);
    }

}
