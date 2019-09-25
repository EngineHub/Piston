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
import org.enginehub.piston.gen.util.SafeName;
import org.enginehub.piston.gen.value.CommandInfo;

public class CommandInfoOptimization implements CollectionOptimization<CommandInfo> {

    private final CommandParamInfoOptimization commandParamInfoOptimization;
    private final IdentifierTracker identifierTracker;

    public CommandInfoOptimization(CommandParamInfoOptimization commandParamInfoOptimization,
                                   IdentifierTracker identifierTracker) {
        this.commandParamInfoOptimization = commandParamInfoOptimization;
        this.identifierTracker = identifierTracker;
    }

    @Override
    public CommandInfo optimizeSingle(CommandInfo input) {
        return input.toBuilder()
            .generatedName(identifierTracker.methodName(
                SafeName.from(input.getGeneratedName())
            ))
            .params(commandParamInfoOptimization.optimize(input.getParams()))
            .build();
    }
}
