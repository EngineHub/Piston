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

package org.enginehub.piston.gen.value;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;

import java.util.Collection;

@AutoValue
public abstract class RequiredVariable {

    public static Builder builder() {
        return new AutoValue_RequiredVariable.Builder()
            .inherited(false)
            .annotations(ImmutableList.of());
    }

    @AutoValue.Builder
    public interface Builder {

        Builder inherited(boolean inherited);

        Builder type(TypeName type);

        Builder name(String name);

        Builder annotations(Collection<AnnotationSpec> annotations);

        RequiredVariable build();

    }

    RequiredVariable() {
    }

    /**
     * Is this variable inherited from another interface?
     */
    public abstract boolean isInherited();

    public abstract TypeName getType();

    public abstract String getName();

    public abstract ImmutableList<AnnotationSpec> getAnnotations();


}
