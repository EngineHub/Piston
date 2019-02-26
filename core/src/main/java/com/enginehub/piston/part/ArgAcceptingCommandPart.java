package com.enginehub.piston.part;

import com.enginehub.piston.CommandParameters;
import com.enginehub.piston.converter.ArgumentConverter;
import com.google.common.collect.ImmutableList;

import java.util.List;

public interface ArgAcceptingCommandPart<T> extends CommandPart {

    default T value(CommandParameters<?> parameters) {
        return parameters.valueOf(this);
    }

    default List<T> values(CommandParameters<?> parameters) {
        return parameters.valuesOf(this);
    }

    ArgumentConverter<T> getConverter();

    ImmutableList<T> getDefaults();

}
