package org.enginehub.piston.converter;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import org.enginehub.piston.inject.InjectedValueAccess;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converts using a BiMap.
 */
public final class BiMapArgumentConverter<T> implements ArgumentConverter<T> {

    /**
     * Construct a converter for simple string choices from a set.
     */
    public static BiMapArgumentConverter<String> forChoices(Set<String> choices) {
        ImmutableBiMap.Builder<String, String> map = ImmutableBiMap.builder();
        choices.forEach(c -> map.put(c, c));
        return from(map.build());
    }

    public static <T> BiMapArgumentConverter<T> from(BiMap<String, T> map) {
        return new BiMapArgumentConverter<>(map);
    }

    private final ImmutableBiMap<String, T> map;

    private BiMapArgumentConverter(BiMap<String, T> map) {
        this.map = ImmutableBiMap.copyOf(map);
    }

    @Override
    public ConversionResult<T> convert(String argument, InjectedValueAccess context) {
        T result = map.get(argument);
        if (result == null) {
            return FailedConversion.from(new IllegalArgumentException("Invalid value: " + argument));
        }
        return SuccessfulConversion.fromSingle(result);
    }

    @Override
    public String describeAcceptableArguments() {
        return String.join("|", map.keySet());
    }

    @Override
    public List<String> getSuggestions(String input) {
        return map.keySet().stream()
            .filter(s -> s.startsWith(input))
            .collect(Collectors.toList());
    }
}
