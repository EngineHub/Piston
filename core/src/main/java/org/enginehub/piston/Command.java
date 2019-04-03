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

import org.enginehub.piston.part.CommandPart;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;

/**
 * Represents a command. Commands can be modified using {@code with/add} functions,
 * which return new instances with the specified changes. Only the name of a command
 * is required, so be careful not to make commands that don't do anything.
 *
 * <p>
 * This class is immutable.
 * </p>
 */
public interface Command {

    /**
     * Represents what a command does. An action usually does one or more things,
     * and those should be counted and returned for user feedback.
     */
    interface Action {

        /**
         * An action that does nothing, and returns 0. Useful for
         * commands that only have subcommands, and do nothing.
         */
        Action NULL_ACTION = params -> 0;

        /**
         * Run the action.
         *
         * @param parameters the command parameters
         * @return a count for the number of things done by the action
         */
        int run(CommandParameters parameters) throws Exception;

    }

    /**
     * Represents a condition for the execution of a command.
     */
    interface Condition {

        /**
         * An condition that always returns true.
         */
        Condition TRUE = p -> true;

        /**
         * An condition that always returns false.
         */
        Condition FALSE = p -> false;

        /**
         * Determine if the condition is satisfied.
         *
         * @param parameters the command parameters
         */
        boolean satisfied(CommandParameters parameters);

        default Condition and(Condition other) {
            return p -> satisfied(p) && other.satisfied(p);
        }

        default Condition or(Condition other) {
            return p -> satisfied(p) || other.satisfied(p);
        }

        default Condition not() {
            return p -> !satisfied(p);
        }

    }

    /**
     * Builder for a command.
     */
    interface Builder {

        Builder name(String name);

        Builder aliases(Collection<String> aliases);

        Builder description(String description);

        Builder footer(@Nullable String footer);

        Builder parts(Collection<CommandPart> parts);

        Builder addPart(CommandPart part);

        Builder addParts(CommandPart... parts);

        Builder addParts(Iterable<CommandPart> parts);

        Builder action(Action action);

        Builder condition(Condition condition);

        Command build();

    }

    String getName();

    ImmutableList<String> getAliases();

    String getDescription();

    Optional<String> getFooter();

    ImmutableList<CommandPart> getParts();

    Condition getCondition();

    Action getAction();

    Builder toBuilder();

    /**
     * Get the usage text for this command.
     */
    String getUsage();

    String getFullHelp();

}
