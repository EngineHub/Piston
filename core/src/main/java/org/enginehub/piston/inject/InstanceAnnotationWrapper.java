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

import java.lang.annotation.Annotation;

@AutoValue
abstract class InstanceAnnotationWrapper implements AnnotationWrapper {

    static InstanceAnnotationWrapper from(Annotation annotationInstance) {
        return new AutoValue_InstanceAnnotationWrapper(annotationInstance);
    }

    InstanceAnnotationWrapper() {
    }

    @Override
    public abstract @NonNull Annotation getAnnotation();

    @Override
    public final Class<? extends Annotation> getAnnotationType() {
        return getAnnotation().annotationType();
    }
}
