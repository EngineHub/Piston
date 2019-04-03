package org.enginehub.piston.optimize;

/**
 * Represents an optimization.
 *
 * @param <T>
 */
@FunctionalInterface
public interface Optimization<T> {

    T optimize(T input);

}
