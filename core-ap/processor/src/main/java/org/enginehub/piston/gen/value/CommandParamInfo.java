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
import com.squareup.javapoet.TypeName;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.enginehub.piston.CommandParameters;

/**
 * Information that can be used to supply a parameter for a
 * registered command method.
 */
@AutoValue
public abstract class CommandParamInfo {

    public static Builder builder() {
        return new AutoValue_CommandParamInfo.Builder();
    }

    @AutoValue.Builder
    public interface Builder {

        Builder name(@Nullable String name);

        Builder type(@Nullable TypeName type);

        Builder construction(@Nullable CodeBlock construction);

        Builder extractSpec(ExtractSpec extraction);

        CommandParamInfo build();
    }

    /**
     * Variable name, if used to store data.
     *
     * <p>This is merely a suggestion. Do not rely on this name existing in generated code.</p>
     */
    @Nullable
    public abstract String getName();

    /**
     * Type of the param variable, if used to store data.
     */
    @Nullable
    public abstract TypeName getType();

    /**
     * Code for initializing data the extraction code uses.
     */
    @Nullable
    public abstract CodeBlock getConstruction();

    /**
     * Spec for extracting the value from a {@link CommandParameters} instance.
     */
    public abstract ExtractSpec getExtractSpec();

    public abstract Builder toBuilder();

}
