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
        return newSpecMapping.computeIfAbsent(input, i ->
            i.toBuilder().name(identifierTracker.methodName(i.getName())).build()
        );
    }
}
