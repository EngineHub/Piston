package org.enginehub.piston.util;

import java.util.Optional;

/**
 * Provides a value, given a context argument.
 */
public interface ValueProvider<C, T> {
    /**
     * Compute the value from the context.
     *
     * @param context the context, never {@code null}
     * @return the value, may be {@link Optional#empty()} to indicate no value
     */
    Optional<T> value(C context);
}
