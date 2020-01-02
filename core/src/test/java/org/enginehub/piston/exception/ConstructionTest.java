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

package org.enginehub.piston.exception;

import com.google.common.collect.ImmutableList;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.NoInputCommandParameters;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.CommandParts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Piston Exceptions can be constructed")
public class ConstructionTest {

    @Test
    void conditionFailedException() {
        Command command = mock(Command.class);
        Command.Condition uniqCondition = p -> false;
        when(command.getCondition()).thenReturn(uniqCondition);
        ImmutableList<Command> commands = ImmutableList.of(command);
        ConditionFailedException ex = new ConditionFailedException(commands);
        assertSame(commands, ex.getCommands());
        assertSame(uniqCondition, ex.getCondition());
    }

    @Test
    void commandExecutionException() {
        Throwable cause = new Throwable();
        ImmutableList<Command> commands = ImmutableList.of(mock(Command.class));
        CommandExecutionException ex = new CommandExecutionException(cause, commands);
        assertSame(cause, ex.getCause());
        assertSame(commands, ex.getCommands());
    }

    @Test
    void stopExecutionException() {
        Component message = TextComponent.of("stop");
        ImmutableList<Command> commands = ImmutableList.of(mock(Command.class));
        StopExecutionException ex = new StopExecutionException(message, commands);
        assertSame(message, ex.getRichMessage());
        assertEquals("stop", ex.getMessage());
        assertSame(commands, ex.getCommands());
    }

    @Test
    void stopExecutionExceptionNoCommands() {
        Component message = TextComponent.of("stop");
        StopExecutionException ex = new StopExecutionException(message);
        assertSame(message, ex.getRichMessage());
        assertEquals("stop", ex.getMessage());
        assertEquals(ImmutableList.of(), ex.getCommands());
    }

    @Test
    void noSuchCommandException() {
        String command = "invalid-command";
        NoSuchCommandException ex = new NoSuchCommandException(command);
        assertSame(command, ex.getRequestedCommand());
        assertTrue(ex.getMessage().contains(command));
    }

    private ImmutableList<Command> mockExecutionPath(ImmutableList<CommandPart> parts) {
        Command command = mock(Command.class);
        when(command.getName()).thenReturn("mock");
        when(command.getParts()).thenReturn(parts);
        return ImmutableList.of(command);
    }

    @Test
    void noSuchFlagExceptionNoFlags() {
        CommandParseResult mock = mock(CommandParseResult.class);
        ImmutableList<Command> executionPath = mockExecutionPath(ImmutableList.of());
        when(mock.getExecutionPath()).thenReturn(executionPath);
        when(mock.getParameters()).thenReturn(NoInputCommandParameters.builder().build());
        char flag = 'Z';
        NoSuchFlagException ex = new NoSuchFlagException(mock, flag);
        assertSame(flag, ex.getRequestedFlag());
        assertTrue(ex.getMessage().indexOf(flag) >= 0);
        assertTrue(ex.getMessage().contains("does not have any flags"));
    }

    @Test
    void noSuchFlagExceptionWrongFlag() {
        CommandParseResult mock = mock(CommandParseResult.class);
        ImmutableList<Command> executionPath = mockExecutionPath(ImmutableList.of(
            CommandParts.flag('q', TextComponent.of("q flag")).build()
        ));
        when(mock.getExecutionPath()).thenReturn(executionPath);
        when(mock.getParameters()).thenReturn(NoInputCommandParameters.builder().build());
        char flag = 'Z';
        NoSuchFlagException ex = new NoSuchFlagException(mock, flag);
        assertSame(flag, ex.getRequestedFlag());
        assertTrue(ex.getMessage().indexOf(flag) >= 0);
        assertTrue(ex.getMessage().contains("q"));
    }

    @Test
    void usageExceptionNoMessage() {
        CommandParseResult mock = mock(CommandParseResult.class);
        ImmutableList<Command> executionPath = mockExecutionPath(ImmutableList.of());
        when(mock.getExecutionPath()).thenReturn(executionPath);
        when(mock.getParameters()).thenReturn(NoInputCommandParameters.builder().build());
        UsageException ex = new UsageException(mock);
        assertSame(executionPath, ex.getCommands());
        assertSame(mock, ex.getCommandParseResult());
        assertEquals(TextComponent.empty(), ex.getRichMessage());
        assertNull(ex.getMessage());
    }

    @Test
    void usageExceptionMessage() {
        CommandParseResult mock = mock(CommandParseResult.class);
        ImmutableList<Command> executionPath = mockExecutionPath(ImmutableList.of());
        when(mock.getExecutionPath()).thenReturn(executionPath);
        when(mock.getParameters()).thenReturn(NoInputCommandParameters.builder().build());
        Component message = TextComponent.of("message");
        UsageException ex = new UsageException(message, mock);
        assertSame(executionPath, ex.getCommands());
        assertSame(mock, ex.getCommandParseResult());
        assertSame(message, ex.getRichMessage());
        assertEquals("message", ex.getMessage());
    }

    @Test
    void usageExceptionMessageCause() {
        CommandParseResult mock = mock(CommandParseResult.class);
        ImmutableList<Command> executionPath = mockExecutionPath(ImmutableList.of());
        when(mock.getExecutionPath()).thenReturn(executionPath);
        when(mock.getParameters()).thenReturn(NoInputCommandParameters.builder().build());
        Component message = TextComponent.of("message");
        Throwable cause = new Throwable();
        UsageException ex = new UsageException(message, cause, mock);
        assertSame(executionPath, ex.getCommands());
        assertSame(mock, ex.getCommandParseResult());
        assertSame(message, ex.getRichMessage());
        assertEquals("message", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void usageExceptionCause() {
        CommandParseResult mock = mock(CommandParseResult.class);
        ImmutableList<Command> executionPath = mockExecutionPath(ImmutableList.of());
        when(mock.getExecutionPath()).thenReturn(executionPath);
        when(mock.getParameters()).thenReturn(NoInputCommandParameters.builder().build());
        Throwable cause = new Throwable();
        UsageException ex = new UsageException(cause, mock);
        assertSame(executionPath, ex.getCommands());
        assertSame(mock, ex.getCommandParseResult());
        assertEquals(TextComponent.empty(), ex.getRichMessage());
        assertEquals(cause.getClass().getName(), ex.getMessage());
        assertSame(cause, ex.getCause());
    }

}
