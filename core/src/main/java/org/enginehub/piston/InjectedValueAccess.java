package org.enginehub.piston;

import com.google.inject.Key;
import org.enginehub.piston.util.ValueProvider;

import java.util.Optional;

/**
 * Common access declarations for injected values.
 */
public interface InjectedValueAccess {
    /**
     * Get an injected value.
     *
     * <p>
     * Provide value injectors to the {@linkplain CommandManager manager}.
     * </p>
     *
     * @return the value, or {@link Optional#empty()} if not provided
     * @see CommandManager#injectValue(Key, ValueProvider)
     */
    <T> Optional<T> injectedValue(Key<T> key);
}
