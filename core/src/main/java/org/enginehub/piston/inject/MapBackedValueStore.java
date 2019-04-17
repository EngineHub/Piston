package org.enginehub.piston.inject;

import com.google.inject.Key;
import org.enginehub.piston.util.ValueProvider;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link InjectedValueStore} backed by a {@link Map}.
 */
public final class MapBackedValueStore implements InjectedValueStore {

    public static MapBackedValueStore create() {
        return create(new ConcurrentHashMap<>());
    }

    public static MapBackedValueStore create(Map<Key<?>, ValueProvider<InjectedValueAccess, ?>> providers) {
        return new MapBackedValueStore(providers);
    }

    private final Map<Key<?>, ValueProvider<InjectedValueAccess, ?>> providers;

    private MapBackedValueStore(Map<Key<?>, ValueProvider<InjectedValueAccess, ?>> providers) {
        this.providers = providers;
    }

    @Override
    public <T> void injectValue(Key<T> key, ValueProvider<InjectedValueAccess, T> provider) {
        providers.put(key, provider);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> injectedValue(Key<T> key) {
        ValueProvider<InjectedValueAccess, T> provider = (ValueProvider<InjectedValueAccess, T>) providers.get(key);
        return Optional.ofNullable(provider).flatMap(p -> p.value(this));
    }
}
