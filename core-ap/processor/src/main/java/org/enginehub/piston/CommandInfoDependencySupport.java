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

package org.enginehub.piston;

import org.enginehub.piston.annotation.DependencySupport;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;

class CommandInfoDependencySupport implements DependencySupport {
    private final Multiset<String> fieldNames = HashMultiset.create(ReservedVariables.names());
    private final Multiset<String> methodNames = HashMultiset.create();
    private final CommandInfo.Builder builder;

    public CommandInfoDependencySupport(CommandInfo.Builder builder) {
        this.builder = builder;
    }

    private static String realName(Multiset<String> memory, String name) {
        return memory.add(name)
            ? name
            : name + (memory.count(name) - 1);
    }

    @Override
    public String requestInScope(TypeName type, String name, AnnotationSpec... annotations) {
        String realName = realName(fieldNames, name);
        builder.addRequiredVariable(RequiredVariable.builder()
            .type(type)
            .name(realName)
            .annotations(ImmutableList.copyOf(annotations))
            .build());
        return realName;
    }

    @Override
    public String requestMethodName(String name) {
        return realName(methodNames, name);
    }
}
