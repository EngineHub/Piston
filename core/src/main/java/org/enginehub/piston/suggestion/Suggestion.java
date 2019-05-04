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

package org.enginehub.piston.suggestion;

import com.google.auto.value.AutoValue;

/**
 * Represents a suggestion.
 */
@AutoValue
public abstract class Suggestion {

    public static Builder builder() {
        return new AutoValue_Suggestion.Builder();
    }

    @AutoValue.Builder
    public interface Builder {

        Builder suggestion(String suggestion);

        Builder replacedArgument(int replaced);

        Suggestion build();

    }

    Suggestion() {
    }

    /**
     * The suggestion value, to be inserted instead of the argument
     * at index {@link #getReplacedArgument() replacedArgument}.
     *
     * @return the suggestion value
     */
    public abstract String getSuggestion();

    /**
     * The argument index to replace in the original input.
     */
    public abstract int getReplacedArgument();

    /**
     * Convert this suggestion back to a builder.
     */
    public abstract Builder toBuilder();

}
