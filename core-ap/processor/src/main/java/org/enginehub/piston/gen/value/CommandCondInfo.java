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

package org.enginehub.piston.gen.value;

import com.google.auto.value.AutoValue;
import com.squareup.javapoet.CodeBlock;

/**
 * Information that can be used to supply the condition for a
 * registered command method.
 */
@AutoValue
public abstract class CommandCondInfo {

    public static Builder builder() {
        return new AutoValue_CommandCondInfo.Builder();
    }

    @AutoValue.Builder
    public interface Builder {

        Builder condVariable(String variable);

        Builder construction(CodeBlock construction);

        CommandCondInfo build();
    }

    /**
     * Variable name the condition is stored under.
     */
    public abstract String getCondVariable();

    /**
     * Code for initializing the condition.
     */
    public abstract CodeBlock getConstruction();

}
