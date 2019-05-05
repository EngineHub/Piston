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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Represents the result of parsing a command from arguments.
 */
public interface CommandParseResult {

    /**
     * Get the path of commands to be executed.
     */
    ImmutableList<Command> getExecutionPath();

    /**
     * Get the command who's action should be executed.
     */
    default Command getPrimaryCommand() {
        return Iterables.getLast(getExecutionPath());
    }

    /**
     * Get the raw argument bindings.
     */
    ImmutableList<ArgBinding> getBoundArguments();

    default List<String> getOriginalArguments() {
        return Lists.transform(getBoundArguments(), a -> requireNonNull(a).getInput());
    }

    /**
     * Get the parameters to execute the command with.
     */
    CommandParameters getParameters();

}
