package com.enginehub.piston.part;

import com.enginehub.piston.CommandParameters;

/**
 * Represents a part of a command, i.e. anything that can be found on the command line: flags,
 * arguments, and commands themselves.
 */
public interface CommandPart {

    default boolean in(CommandParameters parameters) {
        return parameters.has(this);
    }

    /**
     * Returns the text representation of this part. This is used to show
     * the user where the part belongs, and what it should look like.
     *
     * <p>
     * For flags, this could be something like `--flag`.
     * For arguments and commands, this could be their name.
     * </p>
     */
    String getTextRepresentation();

    /**
     * Returns the description of this part. This should describe what
     * the part controls in the command's execution.
     */
    String getDescription();

    /**
     * Returns {@code true} if this part is required, and may not be
     * missing from the command line.
     */
    boolean isRequired();
}
