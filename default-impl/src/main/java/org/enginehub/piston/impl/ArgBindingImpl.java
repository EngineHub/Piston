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
import com.google.common.collect.ImmutableSet;
import org.enginehub.piston.ArgBinding;
import org.enginehub.piston.part.CommandPart;

import java.util.Collection;

@AutoValue
abstract class ArgBindingImpl implements ArgBinding {

    static Builder builder() {
        return new AutoValue_ArgBindingImpl.Builder();
    }

    @AutoValue.Builder
    interface Builder {

        Builder input(String name);

        Builder parts(Collection<CommandPart> args);

        ArgBindingImpl build();
    }

    ArgBindingImpl() {
    }

    @Override
    public abstract String getInput();

    @Override
    public abstract ImmutableSet<CommandPart> getParts();

}
