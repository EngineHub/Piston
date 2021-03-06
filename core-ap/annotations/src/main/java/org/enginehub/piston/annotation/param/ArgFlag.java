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

package org.enginehub.piston.annotation.param;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bind this parameter as an argument flag.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.CLASS)
public @interface ArgFlag {

    /**
     * The name of the flag.
     */
    char name();

    String ARG_NAME_IS_PARAMETER_NAME = "__ARG_NAME_IS_PARAMETER_NAME__";

    /**
     * The name of the argument. If not specified, defaults to the name of
     * the parameter.
     */
    String argName() default ARG_NAME_IS_PARAMETER_NAME;

    /**
     * A description of the flag.
     */
    String desc();

    /**
     * Default values for the flag if it isn't present.
     */
    String[] def() default {};

}
