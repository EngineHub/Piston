package com.enginehub.piston.converter;

import java.util.Collection;
import java.util.List;

public class ForwardingArgumentConverter<T> implements ArgumentConverter<T> {

    private final ArgumentConverter<T> delegate;

    protected ForwardingArgumentConverter(ArgumentConverter<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Collection<T> convert(String argument) {
        return delegate.convert(argument);
    }

    @Override
    public String describeAcceptableArguments() {
        return delegate.describeAcceptableArguments();
    }

    @Override
    public List<String> getSuggestions(String input) {
        return delegate.getSuggestions(input);
    }
}
