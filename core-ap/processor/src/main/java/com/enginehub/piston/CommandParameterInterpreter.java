package com.enginehub.piston;

import com.google.common.collect.ImmutableList;

import javax.lang.model.element.ExecutableElement;
import java.util.List;

class CommandParameterInterpreter {
    private final ExecutableElement method;

    CommandParameterInterpreter(ExecutableElement method) {
        this.method = method;
    }

    List<CommandPartInfo> getParts() {
        return ImmutableList.of();
    }
}
