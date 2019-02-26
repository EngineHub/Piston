package com.enginehub.piston.converter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.singleton;

/**
 * A simple base implementation of {@link ArgumentConverter}. Provides no suggestions,
 * but you can call {@link #withSuggestions(Function)} to supply them.
 */
public class SimpleArgumentConverter<T> implements ArgumentConverter<T> {

    /**
     * Implements {@link ArgumentConverter#convert(String)} using the provided function,
     * and supplies the provided description for
     * {@link ArgumentConverter#describeAcceptableArguments()}.
     *
     * @param converter the converter function
     * @param description the acceptable arguments description
     * @param <T> the type of the argument
     * @return a converter using the given function and description
     */
    public static <T> SimpleArgumentConverter<T> from(
        Function<String, Collection<T>> converter,
        String description
    ) {
        return new SimpleArgumentConverter<>(converter, description);
    }

    /**
     * Implements {@link ArgumentConverter#convert(String)} by wrapping the result of
     * {@code converter} in a {@linkplain Collections#singleton(Object) singleton set},
     * and supplies the provided description for
     * {@link ArgumentConverter#describeAcceptableArguments()}.
     *
     * @param converter the converter function
     * @param description the acceptable arguments description
     * @param <T> the type of the argument
     * @return a converter using the given function and description
     */
    public static <T> SimpleArgumentConverter<T> fromSingle(
        Function<String, T> converter,
        String description
    ) {
        return from(x -> singleton(converter.apply(x)), description);
    }

    private final Function<String, Collection<T>> converter;
    private final String description;

    private SimpleArgumentConverter(Function<String, Collection<T>> converter, String description) {
        this.converter = converter;
        this.description = description;
    }

    @Override
    public Collection<T> convert(String argument) {
        return converter.apply(argument);
    }

    @Override
    public String describeAcceptableArguments() {
        return description;
    }

    public ArgumentConverter<T> withSuggestions(Function<String, List<String>> suggestions) {
        return new ForwardingArgumentConverter<T>(this) {
            @Override
            public List<String> getSuggestions(String input) {
                return suggestions.apply(input);
            }
        };
    }
}
