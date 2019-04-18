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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Collection;

@AutoValue
public abstract class RegistrationInfo {

    public static Builder builder() {
        Builder builder = new AutoValue_RegistrationInfo.Builder();
        builder.injectedVariablesBuilder();
        builder.declaredFieldsBuilder();
        builder.keyTypesBuilder();
        return builder;
    }

    @AutoValue.Builder
    public interface Builder {

        Builder name(String name);

        Builder targetClassName(ClassName className);

        Builder classVisibility(@Nullable Modifier visibility);

        Builder commands(Collection<CommandInfo> commands);

        ImmutableList.Builder<RequiredVariable> injectedVariablesBuilder();

        default Builder addInjectedVariable(RequiredVariable var) {
            injectedVariablesBuilder().add(var);
            return this;
        }

        ImmutableList.Builder<RequiredVariable> declaredFieldsBuilder();

        default Builder addDeclaredField(RequiredVariable var) {
            declaredFieldsBuilder().add(var);
            return this;
        }

        ImmutableSet.Builder<KeyInfo> keyTypesBuilder();

        default Builder addKeyType(KeyInfo keyInfo) {
            keyTypesBuilder().add(keyInfo);
            return this;
        }

        ImmutableSet.Builder<TypeElement> superTypesBuilder();

        default Builder addSuperType(TypeElement superType) {
            superTypesBuilder().add(superType);
            return this;
        }

        RegistrationInfo build();

    }

    RegistrationInfo() {
    }

    public abstract String getName();

    public abstract ClassName getTargetClassName();

    @Nullable
    public abstract Modifier getClassVisibility();

    public abstract ImmutableList<CommandInfo> getCommands();

    public abstract ImmutableList<RequiredVariable> getInjectedVariables();

    public abstract ImmutableList<RequiredVariable> getDeclaredFields();

    public abstract ImmutableSet<KeyInfo> getKeyTypes();

    public abstract ImmutableSet<TypeElement> getSuperTypes();

}
