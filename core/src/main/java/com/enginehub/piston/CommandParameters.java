package com.enginehub.piston;

import com.enginehub.piston.part.ArgAcceptingCommandPart;
import com.enginehub.piston.part.CommandPart;

import javax.annotation.Nullable;
import java.util.List;

public interface CommandParameters<C> {

    /**
     * Determine if the execution will also include a sub-command.
     */
    boolean isRunningSubcommand();

    /**
     * Checks if the parameters contain the specified part.
     *
     * @param part - the part to look for
     * @return if the parameters contain the specified part
     */
    boolean has(CommandPart part);

    /**
     * Gets the value of the specified part, throwing if it
     * is not {@linkplain #has(CommandPart) present} or if
     * there are multiple values.
     *
     * @param part - the part to look for
     * @return the value
     */
    <T> T valueOf(ArgAcceptingCommandPart<T> part);

    /**
     * Gets all the values of the specified part, throwing if it
     * is not {@linkplain #has(CommandPart) present}.
     *
     * @param part - the part to look for
     * @return the value
     */
    <T> List<T> valuesOf(ArgAcceptingCommandPart<T> part);

    /**
     * Get the command context.
     */
    @Nullable
    C getContext();

}
