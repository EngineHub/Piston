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
import org.enginehub.piston.ArgBinding;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.CommandParseResult;

@AutoValue
abstract class CommandParseResultImpl implements CommandParseResult {

    static Builder builder() {
        return new AutoValue_CommandParseResultImpl.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {

        public final Builder addCommand(Command command) {
            executionPathBuilder().add(command);
            return this;
        }

        abstract ImmutableList.Builder<Command> executionPathBuilder();

        public abstract ImmutableList<Command> getExecutionPath();

        public final Builder addArgument(ArgBinding argBinding) {
            boundArgumentsBuilder().add(argBinding);
            return this;
        }

        abstract ImmutableList.Builder<ArgBinding> boundArgumentsBuilder();

        public abstract Builder parameters(CommandParameters parameters);

        public abstract CommandParseResultImpl build();
    }

    CommandParseResultImpl() {
    }

    @Override
    public abstract ImmutableList<Command> getExecutionPath();

    @Override
    public abstract ImmutableList<ArgBinding> getBoundArguments();

    @Override
    public abstract CommandParameters getParameters();
}
