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

package org.enginehub.piston.annotation;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

public interface CommandConditionGenerator {

    /**
     * Bake the parameters given by the annotation into a fast runtime check.
     *
     * <p>
     * This method is called during annotation processing.
     * </p>
     *
     * @param condition the annotation that defines the check. The type of the mirror is
     *     guaranteed to be {@link CommandCondition}.
     * @param enclosingMethod the method that the condition was found on. This is useful for
     *     gathering information via custom annotations
     * @param depSupport variable dependency support, if you need an object instance request
     *     it via this interface
     * @return a block of code representing a method that returns a boolean
     */
    CodeBlock generateCondition(AnnotationMirror condition,
                                ExecutableElement enclosingMethod,
                                DependencySupport depSupport);

}
