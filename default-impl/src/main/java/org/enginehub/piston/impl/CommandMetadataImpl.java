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

package org.enginehub.piston.impl;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import org.enginehub.piston.CommandMetadata;

import java.util.Collection;

@AutoValue
abstract class CommandMetadataImpl implements CommandMetadata {

    static Builder builder() {
        return new AutoValue_CommandMetadataImpl.Builder();
    }

    @AutoValue.Builder
    interface Builder {

        Builder calledName(String name);

        Builder arguments(Collection<String> args);

        CommandMetadataImpl build();
    }

    CommandMetadataImpl() {
    }

    @Override
    public abstract String getCalledName();

    @Override
    public abstract ImmutableList<String> getArguments();
}
