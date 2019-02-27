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
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;

import javax.annotation.Nullable;
import javax.lang.model.element.ExecutableElement;
import java.util.Collection;
import java.util.Optional;

@AutoValue
public abstract class CommandInfo {

    public static Builder builder() {
        Builder builder = new AutoValue_CommandInfo.Builder();
        builder.requiredVariablesBuilder();
        builder.footer(null);
        builder.parts(ImmutableList.of());
        return builder;
    }

    @AutoValue.Builder
    public interface Builder {

        Builder commandMethod(ExecutableElement method);

        Builder name(String name);

        Builder aliases(Collection<String> aliases);

        Builder description(String description);

        Builder footer(@Nullable String footer);

        Builder parts(Collection<CommandPartInfo> parts);

        ImmutableList.Builder<RequiredVariable> requiredVariablesBuilder();

        default Builder addRequiredVariable(RequiredVariable var) {
            requiredVariablesBuilder().add(var);
            return this;
        }

        Builder condition(@Nullable CodeBlock condition);

        CommandInfo build();
    }

    CommandInfo() {
    }

    public abstract ExecutableElement getCommandMethod();

    public abstract String getName();

    public abstract ImmutableList<String> getAliases();

    public abstract String getDescription();

    public abstract Optional<String> getFooter();

    public abstract ImmutableList<CommandPartInfo> getParts();

    public abstract ImmutableList<RequiredVariable> getRequiredVariables();

    public abstract Optional<CodeBlock> getCondition();

}
