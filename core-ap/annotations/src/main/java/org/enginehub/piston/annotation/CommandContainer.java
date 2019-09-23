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

package org.enginehub.piston.annotation;

import org.enginehub.piston.gen.CommandConditionGenerator;
import org.enginehub.piston.gen.CommandRegistration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks this class to be searched for {@link Command} annotations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface CommandContainer {

    /**
     * Super-types for the generated registration class to implement.
     *
     * <p>
     * Note that this does not force the class to implement new methods,
     * but if you add an interface that matches existing methods, then
     * those will be overriden, allowing for more generic configuration.
     * </p>
     * <p>
     * For example, you could add an interface representing the addition
     * of a {@link CommandConditionGenerator} to the registration builder,
     * and then you will be able to inject all builders using a single method.
     * </p>
     * <p>
     * N.B.: The registration class always implements {@link CommandRegistration},
     * regardless of the content of this array.
     * </p>
     *
     * @return the super-types for the generated registration class
     */
    Class<?>[] superTypes() default {};

}
