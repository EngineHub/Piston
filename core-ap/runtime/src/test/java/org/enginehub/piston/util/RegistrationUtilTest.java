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

import org.enginehub.piston.inject.Key;
import org.enginehub.piston.internal.RegistrationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RegistrationUtil")
public class RegistrationUtilTest {

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

}
