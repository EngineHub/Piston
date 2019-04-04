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

import org.enginehub.piston.Command.Condition;
import org.enginehub.piston.gen.CommandConditionGenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a condition that must be satisfied before the command
 * executes. This may be used as a meta-annotation, to allow it to be combined with a custom
 * annotation that has additional parameters.
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface CommandCondition {

    /**
     * A class implementing {@link CommandConditionGenerator}, that will be called at runtime
     * to provide the {@link Condition} for the method. The instance must be injected into the
     * registration class.
     */
    Class<? extends CommandConditionGenerator> value();

}
