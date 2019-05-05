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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;

@AutoValue
abstract class TypeAnnotationWrapper implements AnnotationWrapper {

    static TypeAnnotationWrapper from(Class<? extends Annotation> annotationType) {
        return new AutoValue_TypeAnnotationWrapper(annotationType);
    }

    TypeAnnotationWrapper() {
    }

    @Override
    public abstract @NonNull Class<? extends Annotation> getAnnotationType();

    @Override
    public final @Nullable Annotation getAnnotation() {
        return null;
    }
}
