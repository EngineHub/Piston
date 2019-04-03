package org.enginehub.piston.optimize;

import org.enginehub.piston.value.CommandParamInfo;

public class CommandParamInfoOptimization implements CollectionOptimization<CommandParamInfo> {

    private final ExtractSpecOptimization extractSpecOptimization;

    public CommandParamInfoOptimization(ExtractSpecOptimization extractSpecOptimization) {
        this.extractSpecOptimization = extractSpecOptimization;
    }

    @Override
    public CommandParamInfo optimizeSingle(CommandParamInfo input) {
        return input.toBuilder().extractSpec(
            extractSpecOptimization.optimize(input.getExtractSpec())
        ).build();
    }
}
