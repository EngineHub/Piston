package org.enginehub.piston.converter;

import org.enginehub.piston.inject.InjectedValueAccess;

@FunctionalInterface
public interface SimpleConverter<T> {

    T convert(String argument, InjectedValueAccess context);

    default Converter<T> asConverter() {
        return (arguments, context) -> {
            try {
                return SuccessfulConversion.fromSingle(convert(arguments.next(), context));
            } catch (Throwable t) {
                return FailedConversion.from(t);
            }
        };
    }

}
