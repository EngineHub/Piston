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

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;

import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;
import java.util.Collection;

@AutoValue
abstract class RegistrationInfo {

    static Builder builder() {
        return new AutoValue_RegistrationInfo.Builder();
    }

    @AutoValue.Builder
    interface Builder {

        Builder name(String name);

        Builder targetClassName(ClassName className);

        Builder classVisibility(@Nullable Modifier visibility);

        Builder javaxInjectClassName(@Nullable ClassName className);

        Builder commands(Collection<CommandInfo> commands);

        RegistrationInfo build();

    }

    RegistrationInfo() {
    }

    abstract String getName();

    abstract ClassName getTargetClassName();

    @Nullable
    abstract Modifier getClassVisibility();

    @Nullable
    abstract ClassName getJavaxInjectClassName();

    abstract ImmutableList<CommandInfo> getCommands();

}
