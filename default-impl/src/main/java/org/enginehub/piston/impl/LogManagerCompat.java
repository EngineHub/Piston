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

package org.enginehub.piston.impl;

import com.google.common.base.Throwables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Internal use only.
 */
public class LogManagerCompat {

    public static Logger getLogger() {
        return LogManager.getLogger(getCallerCallerClassName());
    }

    private static String getCallerCallerClassName() {
        List<StackTraceElement> lazyStack = Throwables.lazyStackTrace(new Throwable());
        // 0 - this method
        // 1 - caller
        // 2 - caller caller
        return lazyStack.get(2).getClassName();
    }

    private LogManagerCompat() {
    }
}
