package org.enginehub.piston.exception;

import org.enginehub.piston.Command;

import javax.annotation.Nullable;

/**
 * Signal to stop executing a command. Command reference not required.
 */
public class StopExecutionException extends CommandException {
    public StopExecutionException(String message) {
        this(message, null);
    }

    public StopExecutionException(String message, @Nullable Command command) {
        super(message, command);
    }
}
