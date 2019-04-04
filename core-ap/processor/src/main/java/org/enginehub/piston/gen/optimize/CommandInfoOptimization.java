/*
 * Piston, a flexible command management system.
 * Copyright (C) EngineHub <http://www.enginehub.com>
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

import org.enginehub.piston.gen.value.CommandInfo;

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
