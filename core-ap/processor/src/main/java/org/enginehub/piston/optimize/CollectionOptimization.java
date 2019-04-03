package org.enginehub.piston.optimize;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents an optimization over a collection.
 *
 * @param <T>
 */
@FunctionalInterface
public interface CollectionOptimization<T> extends Optimization<Collection<T>> {

    @Override
    default Collection<T> optimize(Collection<T> input) {
        Collection<T> out = new ArrayList<>();
        for (T t : input) {
            out.add(optimizeSingle(t));
        }
        return out;
    }

    T optimizeSingle(T input);

}
