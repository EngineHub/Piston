/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) EngineHub <http://www.enginehub.com>
 * Copyright (C) oblique-commands contributors
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
