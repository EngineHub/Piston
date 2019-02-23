package com.enginehub.piston;

import com.enginehub.piston.part.CommandPart;
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
        int run(CommandParameters parameters);

    }

    /**
     * Represents a condition for the execution of a command.
     */
    interface Condition {

        /**
         * An condition that always returns true.
         */
        Condition TRUE = () -> true;

        /**
         * An condition that always returns false.
         */
        Condition FALSE = () -> false;

        /**
         * Determine if the condition is satisfied.
         */
        boolean satisfied();

        default Condition and(Condition other) {
            return () -> satisfied() && other.satisfied();
        }

        default Condition or(Condition other) {
            return () -> satisfied() || other.satisfied();
        }

        default Condition not() {
            return () -> !satisfied();
        }

    }

    /**
     * Builder for a command.
     */
    interface Builder {

        Builder name(String name);

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
