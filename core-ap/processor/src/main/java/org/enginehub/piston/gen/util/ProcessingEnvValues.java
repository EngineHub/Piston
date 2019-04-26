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

package org.enginehub.piston.gen.util;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * Value extraction from {@link ProcessingEnvironment}.
 */
public class ProcessingEnvValues {

    public static final String ARG_NAME_KEY_PREFIX = "arg.name.key.prefix";

    public static String prefixArgName(ProcessingEnvironment env, String name) {
        String prefix = env.getOptions().getOrDefault(ARG_NAME_KEY_PREFIX, "piston.argument");
        return prefix.isEmpty() ? name : prefix + "." + name;
    }

    private ProcessingEnvValues() {
    }
}
