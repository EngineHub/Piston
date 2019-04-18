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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import org.enginehub.piston.gen.util.SafeName;
import org.enginehub.piston.gen.value.KeyInfo;
import org.enginehub.piston.gen.value.RegistrationInfo;
import org.enginehub.piston.gen.value.RequiredVariable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class GenerationSupportImpl implements GenerationSupport {

    private static final class ShareKey {
        private final TypeName type;
        private final String name;
        private final Object shareKey;

        ShareKey(TypeName type, String name, Object shareKey) {
            this.type = type;
            this.name = name;
            this.shareKey = shareKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ShareKey shareKey1 = (ShareKey) o;
            return type.equals(shareKey1.type) &&
                name.equals(shareKey1.name) &&
                shareKey.equals(shareKey1.shareKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name, shareKey);
        }
    }

    private final IdentifierTracker identifierTracker;
    private final RegistrationInfo.Builder builder;
    private final Map<ShareKey, String> sharedDepNames = new HashMap<>();
    private final Map<ShareKey, String> sharedFieldNames = new HashMap<>();

    public GenerationSupportImpl(IdentifierTracker identifierTracker,
                                 RegistrationInfo.Builder builder) {
        this.identifierTracker = identifierTracker;
        this.builder = builder;
    }

    @Override
    public String requestDependency(TypeName type, String name, @Nullable Object shareKey) {
        ShareKey hashKey = shareKey == null ? null : new ShareKey(type, name, shareKey);
        if (hashKey != null) {
            return sharedDepNames.computeIfAbsent(hashKey,
                k -> requestDependencyUnshared(type, name)
            );
        }
        return requestDependencyUnshared(type, name);
    }

    private String requestDependencyUnshared(TypeName type, String name) {
        String realName = identifierTracker.fieldName(name);
        builder.addInjectedVariable(RequiredVariable.builder()
            .type(type)
            .name(realName)
            .build());
        return realName;
    }

    @Override
    public String requestField(TypeName type, String name, @Nullable Object shareKey) {
        ShareKey hashKey = shareKey == null ? null : new ShareKey(type, name, shareKey);
        if (hashKey != null) {
            return sharedFieldNames.computeIfAbsent(hashKey,
                k -> requestFieldUnshared(type, name)
            );
        }
        return requestFieldUnshared(type, name);
    }

    private String requestFieldUnshared(TypeName type, String name) {
        String realName = identifierTracker.fieldName(name);
        builder.addDeclaredField(RequiredVariable.builder()
            .type(type)
            .name(realName)
            .build());
        return realName;
    }

    @Override
    public String requestMethodName(String name) {
        return identifierTracker.methodName(name);
    }

    @Override
    public CodeBlock requestKey(TypeName type, @Nullable AnnotationSpec annotationSpec) {
        type = type.box();
        builder.addKeyType(KeyInfo.of(type, annotationSpec));
        return CodeBlock.of("$L", SafeName.getNameAsIdentifier(type) + "Key");
    }
}
