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

package org.enginehub.piston.converter;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SuccessfulConversionTest {

    @Test
    void equalContentsFromDifferentCollectionTypes() {
        assertEquals(
            SuccessfulConversion.from(List.of("a", "b")),
            SuccessfulConversion.from(new LinkedHashSet<>(List.of("a", "b")))
        );
    }

    @Test
    void equalContentsFromCollectionWithoutEquals() {
        assertEquals(
            SuccessfulConversion.from(List.of("a", "b")),
            SuccessfulConversion.from(new ArrayDeque<>(List.of("a", "b")))
        );
    }

    @Test
    void equalContentsHashConsistently() {
        assertEquals(
            SuccessfulConversion.from(List.of("a", "b")).hashCode(),
            SuccessfulConversion.from(new ArrayDeque<>(List.of("a", "b"))).hashCode()
        );
    }

    @Test
    void differingContentsAreNotEqual() {
        assertNotEquals(
            SuccessfulConversion.from(List.of("a", "b")),
            SuccessfulConversion.from(List.of("b", "a"))
        );
    }

    @Test
    void mutatingTheSourceDoesNotAffectTheResult() {
        List<String> source = new ArrayList<>(List.of("a"));
        SuccessfulConversion<String> conversion = SuccessfulConversion.from(source);
        source.add("b");
        assertEquals(List.of("a"), conversion.get());
    }

}
