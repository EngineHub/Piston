package org.enginehub.piston.optimize;

import org.enginehub.piston.value.CommandInfo;

public class CommandInfoOptimization implements CollectionOptimization<CommandInfo> {

    private final CommandParamInfoOptimization commandParamInfoOptimization;

    public CommandInfoOptimization(CommandParamInfoOptimization commandParamInfoOptimization) {
        this.commandParamInfoOptimization = commandParamInfoOptimization;
    }

    @Override
    public CommandInfo optimizeSingle(CommandInfo input) {
        return input.toBuilder()
            .params(commandParamInfoOptimization.optimize(input.getParams()))
            .build();
    }
}
