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

package org.enginehub.piston.gen;

import org.enginehub.piston.CommandParameters;

import java.lang.reflect.Method;

/**
 * Listener for command calls, both before and after.
 */
public interface CommandCallListener {

    /**
     * Called before invoking the actual command method.
     *
     * @param commandMethod the method
     * @param parameters the full parameters for the command
     */
    default void beforeCall(Method commandMethod, CommandParameters parameters) {
    }

    /**
     * Called just after invoking the actual command method,
     * if it was successful.
     *
     * @param commandMethod the method
     * @param parameters the full parameters for the command
     */
    default void afterCall(Method commandMethod, CommandParameters parameters) {
    }

    /**
     * Called just after invoking the actual command method,
     * if it threw an exception.
     *
     * @param commandMethod the method
     * @param error the exception thrown by the method
     * @param parameters the full parameters for the command
     */
    default void afterThrow(Method commandMethod, CommandParameters parameters, Throwable error) {
    }

}
