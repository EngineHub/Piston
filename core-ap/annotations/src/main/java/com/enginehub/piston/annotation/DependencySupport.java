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

package com.enginehub.piston.annotation;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;

public interface DependencySupport {

    /**
     * Request that a variable with the given type and name be in-scope
     * for the code block returned by the method using this support object.
     *
     * <p>
     * The annotations are added the parameters wherever they are added,
     * usually in the constructor. This allows for the use of {@code @Provided} or similar
     * injection mechanisms.
     * </p>
     *
     * @param type the type of the variable
     * @param name the base name of the variable
     * @param annotations the annotations to add to any parameter
     * @return the actual name of the variable. You must use this to reference it.
     */
    String requestInScope(TypeName type, String name, AnnotationSpec... annotations);

}
