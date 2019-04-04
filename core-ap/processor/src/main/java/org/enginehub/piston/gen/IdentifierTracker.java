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

package org.enginehub.piston.gen;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.enginehub.piston.gen.util.SafeName;
import org.enginehub.piston.gen.value.ReservedNames;

public class IdentifierTracker {

    private static String realName(Multiset<String> memory, String name) {
        // Make the name safe first
        name = SafeName.from(name);
        memory.add(name);
        int count = memory.count(name);
        return count == 1 ? name : name + count;
    }

    private final Multiset<String> fieldNames = HashMultiset.create(ReservedNames.fieldNames());
    private final Multiset<String> methodNames = HashMultiset.create(ReservedNames.methodNames());

    public String fieldName(String requested) {
        return realName(fieldNames, requested);
    }

    public String methodName(String requested) {
        return realName(methodNames, requested);
    }
}
