/*
 * Piston, a flexible command management system.
 * Copyright (C) EngineHub <https://www.enginehub.org>
 * Copyright (C) Piston contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.piston.gen.optimize;

import org.enginehub.piston.gen.IdentifierTracker;
import org.enginehub.piston.gen.value.CommandParamInfo;
import org.enginehub.piston.gen.value.ExtractSpec;

import java.util.HashMap;
import java.util.Map;

public class CommandParamInfoOptimization implements CollectionOptimization<CommandParamInfo> {

    private final ExtractSpecOptimization extractSpecOptimization;

    private final IdentifierTracker identifierTracker;
    // map from an original param to one with the modified name
    private final Map<CommandParamInfo, CommandParamInfo> newSpecMapping = new HashMap<>();

    public CommandParamInfoOptimization(
        ExtractSpecOptimization extractSpecOptimization,
        IdentifierTracker identifierTracker
    ) {
        this.extractSpecOptimization = extractSpecOptimization;
        this.identifierTracker = identifierTracker;
    }

    @Override
    public CommandParamInfo optimizeSingle(CommandParamInfo input) {
        return newSpecMapping.computeIfAbsent(input, i -> {
            CommandParamInfo.Builder builder = i.toBuilder();
            ExtractSpec extractSpec = i.getExtractSpec();
            if (i.getName() != null) {
                String newName = identifierTracker.fieldName(i.getName());
                builder.name(newName);
                extractSpec = extractSpec.toBuilder()
                    .name(identifierTracker.methodName(extractSpec.getName()))
                    .build();
            }
            return builder.extractSpec(extractSpec).build();
        });
    }
}
