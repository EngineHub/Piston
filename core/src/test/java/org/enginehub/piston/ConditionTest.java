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

package org.enginehub.piston;

import org.junit.jupiter.api.Test;

import static org.enginehub.piston.Command.Condition.FALSE;
import static org.enginehub.piston.Command.Condition.TRUE;
import static org.enginehub.piston.inject.InjectedValueAccess.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConditionTest {

    @Test
    void not() {
        assertTrue(FALSE.not().satisfied(EMPTY));
        assertFalse(TRUE.not().satisfied(EMPTY));
    }

    @Test
    void and() {
        assertFalse(FALSE.and(FALSE).satisfied(EMPTY));
        assertFalse(FALSE.and(TRUE).satisfied(EMPTY));
        assertFalse(TRUE.and(FALSE).satisfied(EMPTY));
        assertTrue(TRUE.and(TRUE).satisfied(EMPTY));
    }

    @Test
    void or() {
        assertFalse(FALSE.or(FALSE).satisfied(EMPTY));
        assertTrue(FALSE.or(TRUE).satisfied(EMPTY));
        assertTrue(TRUE.or(FALSE).satisfied(EMPTY));
        assertTrue(TRUE.or(TRUE).satisfied(EMPTY));
    }

    interface ConditionSubtype extends Command.Condition {

    }

    @Test
    void as() {
        assertFalse(FALSE.as(ConditionSubtype.class).isPresent());
        ConditionSubtype st = ctx -> true;
        assertEquals(st, st.as(ConditionSubtype.class).orElse(null));
    }

}
