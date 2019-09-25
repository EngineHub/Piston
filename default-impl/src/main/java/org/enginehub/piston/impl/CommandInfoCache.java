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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.enginehub.piston.Command;

/**
 * Caches important information about a command.
 */
class CommandInfoCache {

    // Cache information like flags if we can, but let GC clear it if needed.
    private final LoadingCache<Command, CommandInfo> commandCache = CacheBuilder.newBuilder()
        .softValues()
        .build(CacheLoader.from(CommandInfo::from));

    public CommandInfo getInfo(Command command) {
        return commandCache.getUnchecked(command);
    }

}
