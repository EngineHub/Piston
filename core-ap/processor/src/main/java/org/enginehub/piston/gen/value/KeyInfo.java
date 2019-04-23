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

package org.enginehub.piston.gen.value;

import com.google.auto.value.AutoValue;
import com.google.common.reflect.TypeToken;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.enginehub.piston.gen.util.CodeBlockUtil;
import org.enginehub.piston.gen.util.SafeName;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.util.CaseHelper;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@AutoValue
public abstract class KeyInfo {

    public static KeyInfo of(TypeName typeName, @Nullable AnnotationSpec annotationSpec) {
        return new AutoValue_KeyInfo(typeName, annotationSpec);
    }

    KeyInfo() {
    }

    public abstract TypeName typeName();

    public final TypeName wrappedTypeName(Class<?> wrapper) {
        return ParameterizedTypeName.get(ClassName.get(wrapper), typeName());
    }

    @Nullable
    public abstract AnnotationSpec annotationSpec();

    public final String getVariableName() {
        AnnotationSpec spec = annotationSpec();
        return SafeName.getNameAsIdentifier(typeName()) +
            "_" +
            (spec == null
                ? ""
                : getSpecName(spec) + "_") +
            "Key";
    }

    private String getSpecName(AnnotationSpec spec) {
        StringBuilder name = new StringBuilder();
        name.append(SafeName.getNameAsIdentifier(spec.type));
        for (Iterator<Map.Entry<String, List<CodeBlock>>> iterator
             = spec.members.entrySet().iterator();
             iterator.hasNext(); ) {
            Map.Entry<String, List<CodeBlock>> entry = iterator.next();
            if (!entry.getKey().equals("value")) {
                name.append(CaseHelper.camelToTitle(entry.getKey()));
            }
            int size = entry.getValue().size();
            if (size == 1) {
                name.append(entry.getValue().get(0));
            } else {
                entry.getValue().forEach(name::append);
            }
            if (iterator.hasNext()) {
                name.append('$');
            }
        }
        return SafeName.from(name.toString());
    }

    public final CodeBlock keyMaker() {
        CodeBlock typeArgument = getTypeArgumentCode();
        CodeBlock annotationArgumentCode = getAnnotationArgumentCode();

        return Stream.of(typeArgument, annotationArgumentCode)
            .filter(Objects::nonNull)
            .collect(CodeBlockUtil.joining(
                CodeBlock.of("$T.of(", Key.class),
                CodeBlock.of(", "),
                CodeBlock.of(")")
            ));
    }

    private CodeBlock getTypeArgumentCode() {
        if (typeName() instanceof ClassName) {
            return CodeBlock.of("$T.class", typeName());
        }
        return CodeBlock.of("$L", TypeSpec.anonymousClassBuilder("")
            .superclass(wrappedTypeName(TypeToken.class))
            .build());
    }

    @Nullable
    private CodeBlock getAnnotationArgumentCode() {
        AnnotationSpec spec = annotationSpec();
        if (spec == null) {
            return null;
        }
        if (spec.members.isEmpty()) {
            return CodeBlock.of("$T.class", spec.type);
        }
        return runtimeAnnotationExtractor(spec);
    }

    private CodeBlock runtimeAnnotationExtractor(AnnotationSpec annotationSpec) {
        // _technically_ the spec can only be applied to parameters
        // so we'll whip up a fake inner method with the annotation
        // and at runtime, reflect out the instance
        TypeSpec annoExtractor = TypeSpec.anonymousClassBuilder("")
            // `a` = "annotation", extracts the annotation from its own parameter `ah`
            .addMethod(MethodSpec.methodBuilder("a")
                .returns(Annotation.class)
                // `ah` = "annotation holder", holds the annotation on this parameter
                .addParameter(ParameterSpec.builder(Object.class, "ah")
                    .addAnnotation(annotationSpec)
                    .build())
                .addStatement(
                    // from this class
                    "return getClass()" +
                        // retrieve this method (there's only one)
                        ".getDeclaredMethods()[0]" +
                        // and get its first parameter's first annotation (again, only one)
                        ".getParameterAnnotations()[0][0]")
                .build())
            .build();
        // call the method with a null parameter, since it doesn't really matter
        return CodeBlock.of("$L.a(null)", annoExtractor);
    }

}
