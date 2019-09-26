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

package org.enginehub.piston.internal;

import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.inject.Key;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Internal API. Used by generated registration classes, to reduce duplicated code.
 */
public class RegistrationUtil {

    public static <T> T requireOptional(Key<T> type, String name, Optional<T> optional) {
        return optional.orElseThrow(() ->
            new IllegalStateException("No injected value for " + name + " (type " + type + ")")
        );
    }

    public static Method getCommandMethod(Class<?> registrationClass, String methodName, Class... parameterTypes) {
        try {
            return registrationClass.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Missing command method: " + methodName);
        }
    }

    public static void listenersBeforeCall(List<CommandCallListener> listeners,
                                           Method commandMethod,
                                           CommandParameters parameters) {
        for (CommandCallListener listener : listeners) {
            listener.beforeCall(commandMethod, parameters);
        }
    }

    public static void listenersAfterCall(List<CommandCallListener> listeners,
                                          Method commandMethod,
                                          CommandParameters parameters) {
        for (CommandCallListener listener : listeners) {
            listener.afterCall(commandMethod, parameters);
        }
    }

    public static void listenersAfterThrow(List<CommandCallListener> listeners,
                                           Method commandMethod,
                                           CommandParameters parameters,
                                           Throwable error) {
        for (CommandCallListener listener : listeners) {
            listener.afterThrow(commandMethod, parameters, error);
        }
    }

    private RegistrationUtil() {
        throw new RuntimeException();
    }

}
