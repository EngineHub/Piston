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

package org.enginehub.piston.inject;

import com.google.auto.value.AutoValue;
import com.google.common.base.MoreObjects;
import com.google.common.reflect.TypeToken;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a type-based key. The key may optionally be combined with an {@linkplain
 * InjectAnnotation inject annotation} to allow multiple keys of the same type.
 *
 * @param <T> the type stored under the key
 */
@AutoValue
public abstract class Key<T> {

    public static <T> Key<T> of(Class<T> clazz) {
        return of(TypeToken.of(clazz));
    }

    public static <T> Key<T> of(TypeToken<T> typeToken) {
        return of(typeToken, NullAnnotationWrapper.getInstance());
    }

    public static <T> Key<T> of(Class<T> clazz, @Nullable Annotation annotation) {
        return of(TypeToken.of(clazz), annotation);
    }

    public static <T> Key<T> of(Class<T> clazz, @Nullable Class<? extends Annotation> annotation) {
        return of(TypeToken.of(clazz), annotation);
    }

    public static <T> Key<T> of(TypeToken<T> typeToken, @Nullable Annotation annotation) {
        return of(typeToken, strategyFor(annotation));
    }

    public static <T> Key<T> of(TypeToken<T> typeToken, @Nullable Class<? extends Annotation> annotation) {
        return of(typeToken, strategyFor(annotation));
    }

    private static <T> Key<T> of(TypeToken<T> typeToken, AnnotationWrapper annotationWrapper) {
        return new AutoValue_Key<>(typeToken.wrap(), annotationWrapper);
    }

    private static AnnotationWrapper strategyFor(@Nullable Annotation annotation) {
        if (annotation == null) {
            return NullAnnotationWrapper.getInstance();
        }

        validateAnnotationType(annotation.annotationType());
        if (annotation.annotationType().getDeclaredMethods().length == 0) {
            return TypeAnnotationWrapper.from(annotation.annotationType());
        }

        return InstanceAnnotationWrapper.from(annotation);
    }

    private static AnnotationWrapper strategyFor(@Nullable Class<? extends Annotation> annotationType) {
        if (annotationType == null) {
            return NullAnnotationWrapper.getInstance();
        }

        validateAnnotationType(annotationType);
        if (annotationType.getDeclaredMethods().length > 0
            && Stream.of(annotationType.getDeclaredMethods())
            .map(Method::getDefaultValue)
            .allMatch(Objects::nonNull)) {
            // all default values, match an instance made from them instead
            return strategyFor(Annotations.allDefaultsAnnotation(annotationType));
        }

        return TypeAnnotationWrapper.from(annotationType);
    }

    private static void validateAnnotationType(Class<? extends Annotation> annotationType) {
        Retention retention = annotationType.getAnnotation(Retention.class);
        checkArgument(retention != null && retention.value() == RetentionPolicy.RUNTIME,
            "Annotation type %s is not retained at runtime.", annotationType);
        InjectAnnotation injectAnnotation = annotationType.getAnnotation(InjectAnnotation.class);
        checkArgument(injectAnnotation != null,
            "Annotation type %s is not an inject annotation", annotationType);
    }

    public abstract TypeToken<T> getTypeToken();

    abstract AnnotationWrapper getAnnotationWrapper();

    @Nullable
    public final Annotation getAnnotation() {
        return getAnnotationWrapper().getAnnotation();
    }

    @Nullable
    public final Class<? extends Annotation> getAnnotationType() {
        return getAnnotationWrapper().getAnnotationType();
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this)
            .add("type", getTypeToken())
            .add("annotationWrapper", getAnnotationWrapper())
            .toString();
    }
}
