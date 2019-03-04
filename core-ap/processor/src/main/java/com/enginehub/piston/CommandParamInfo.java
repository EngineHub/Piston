/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) EngineHub <http://www.enginehub.com>
 * Copyright (C) oblique-commands contributors
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

package com.enginehub.piston;

import com.google.auto.value.AutoValue;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import javax.annotation.Nullable;

/**
 * Information that can be used to supply a parameter for a
 * registered command method.
 */
@AutoValue
abstract class CommandParamInfo {

    public static Builder builder() {
        return new AutoValue_CommandParamInfo.Builder();
    }

    @AutoValue.Builder
    public interface Builder {

        Builder partVariable(@Nullable String variable);

        Builder construction(@Nullable CodeBlock construction);

        Builder extractMethod(MethodSpec extraction);

        CommandParamInfo build();
    }

    /**
     * Variable name, if used to store data.
     */
    @Nullable
    abstract String getPartVariable();

    /**
     * Code for initializing data the extraction code uses.
     */
    @Nullable
    abstract CodeBlock getConstruction();

    /**
     * Code for extracting the value from a {@link CommandParameters} instance.
     *
     * <p>The parameters instance is always available as {@link ReservedVariables#PARAMETERS}.</p>
     */
    abstract MethodSpec getExtractMethod();

}
