package org.enginehub.piston.optimize;

import org.enginehub.piston.IdentifierTracker;
import org.enginehub.piston.value.ExtractSpec;

import java.util.HashMap;
import java.util.Map;

public class ExtractSpecOptimization implements Optimization<ExtractSpec> {

    private final IdentifierTracker identifierTracker;
    // map from an original spec to one with the modified name
    private final Map<ExtractSpec, ExtractSpec> newSpecMapping = new HashMap<>();

    public ExtractSpecOptimization(IdentifierTracker identifierTracker) {
        this.identifierTracker = identifierTracker;
    }

    @Override
    public ExtractSpec optimize(ExtractSpec input) {
        ExtractSpec out = newSpecMapping.get(input);
        if (out == null) {
            // we need a new mapping for this one
            out = input.toBuilder().name(identifierTracker.methodName(input.getName())).build();
            newSpecMapping.put(input, out);
        }
        return out;
    }
}
