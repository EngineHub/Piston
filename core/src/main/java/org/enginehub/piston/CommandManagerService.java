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

package org.enginehub.piston;

/**
 * Service for providing {@link CommandManager}s.
 *
 * <p>
 * If registered as specified in {@link java.util.ServiceLoader}, {@link
 * DefaultCommandManagerService} will automatically
 * load the service and use it for providing {@link CommandManager}s.
 * </p>
 */
public interface CommandManagerService {

    /**
     * The unique ID for this service. Typically the name of the implementation.
     */
    String id();

    /**
     * Creates a new command manager.
     */
    CommandManager newCommandManager();

}
