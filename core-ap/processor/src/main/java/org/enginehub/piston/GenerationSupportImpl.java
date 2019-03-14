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

package org.enginehub.piston;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;
import org.enginehub.piston.annotation.GenerationSupport;

class GenerationSupportImpl implements GenerationSupport {

    private static String realName(Multiset<String> memory, String name) {
        // Make the name safe first
        name = javaSafeName(name);
        memory.add(name);
        int count = memory.count(name);
        return count == 1 ? name : name + count;
    }

    private static String javaSafeName(String name) {
        return name.codePoints()
            .map(point -> Character.isJavaIdentifierPart(point) ? point : '_')
            .collect(StringBuilder::new,
                StringBuilder::appendCodePoint,
                StringBuilder::append).toString();
    }

    private final Multiset<String> fieldNames = HashMultiset.create(ReservedVariables.names());
    private final Multiset<String> methodNames = HashMultiset.create();
    private final RegistrationInfo.Builder builder;

    public GenerationSupportImpl(RegistrationInfo.Builder builder) {
        this.builder = builder;
    }

    @Override
    public String requestDependency(TypeName type, String name, AnnotationSpec... annotations) {
        String realName = realName(fieldNames, name);
        builder.addInjectedVariable(RequiredVariable.builder()
            .type(type)
            .name(realName)
            .annotations(ImmutableList.copyOf(annotations))
            .build());
        return realName;
    }

    @Override
    public String requestField(TypeName type, String name, AnnotationSpec... annotations) {
        String realName = realName(fieldNames, name);
        builder.addDeclaredField(RequiredVariable.builder()
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
