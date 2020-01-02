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

package org.enginehub.piston.util;

import com.google.common.collect.ImmutableList;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.NoInputCommandParameters;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.internal.RegistrationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@DisplayName("RegistrationUtil")
public class RegistrationUtilTest {

    @Test
    void noConstruction() throws Exception {
        Constructor<RegistrationUtil> constructor = RegistrationUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    void requireOptional() {
        assertEquals("x",
            RegistrationUtil.requireOptional(Key.of(String.class), "name", Optional.of("x")));
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            RegistrationUtil.requireOptional(Key.of(String.class), "name", Optional.empty())
        );
        assertTrue(ex.getMessage().contains("name"));
        assertTrue(ex.getMessage().contains(Key.of(String.class).toString()));
    }

    private final Method fakeCommandMethod;

    {
        try {
            fakeCommandMethod = getClass().getDeclaredMethod("requireOptional");
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void getCommandMethod() {
        assertEquals(fakeCommandMethod,
            RegistrationUtil.getCommandMethod(getClass(), "requireOptional"));
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            RegistrationUtil.getCommandMethod(getClass(), "requireOptional", String.class));
        assertTrue(ex.getMessage().contains("Missing command method"));
    }

    private final CommandParameters parameters = NoInputCommandParameters.builder()
        .build();
    private final CommandCallListener listener = mock(CommandCallListener.class);

    @Test
    void listenersBeforeCall() {
        RegistrationUtil.listenersBeforeCall(ImmutableList.of(listener), fakeCommandMethod, parameters);

        verify(listener).beforeCall(fakeCommandMethod, parameters);
        verifyNoMoreInteractions(listener);
    }
    @Test
    void listenersAfterCall() {
        RegistrationUtil.listenersAfterCall(ImmutableList.of(listener), fakeCommandMethod, parameters);

        verify(listener).afterCall(fakeCommandMethod, parameters);
        verifyNoMoreInteractions(listener);
    }
    @Test
    void listenersAfterThrow() {
        Throwable ex = new RuntimeException();
        RegistrationUtil.listenersAfterThrow(ImmutableList.of(listener), fakeCommandMethod, parameters, ex);

        verify(listener).afterThrow(fakeCommandMethod, parameters, ex);
        verifyNoMoreInteractions(listener);
    }

}
