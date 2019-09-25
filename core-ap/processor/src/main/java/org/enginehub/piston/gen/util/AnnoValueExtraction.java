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

package org.enginehub.piston.gen.util;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Primitives;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;

/**
 * Provides a simple interface for extracting various types from annotations,
 * versus the complicated mess of a visitor system that is given by the Java SDK.
 */
public class AnnoValueExtraction {

    private static final class GeneralResult {
        @Nullable
        private final Object good;
        @Nullable
        private final Object bad;

        public GeneralResult(@Nullable Object good, @Nullable Object bad) {
            this.good = good;
            this.bad = bad;
        }
    }

    private static final AnnotationValueVisitor<GeneralResult, Predicate<Object>> GENERAL_VISITOR =
        new SimpleAnnotationValueVisitor8<GeneralResult, Predicate<Object>>() {
            @Override
            protected GeneralResult defaultAction(Object o, Predicate<Object> objectPredicate) {
                if (objectPredicate.test(o)) {
                    return new GeneralResult(o, null);
                }
                return new GeneralResult(null, o);
            }
        };

    public static <T> T getValue(Element annotated,
                                 AnnotationMirror mirror,
                                 String name,
                                 Class<T> type) {
        AnnotationValue value = getAnnotationValue(mirror, name);
        return getValueCommon(annotated, mirror, type, value);
    }

    private static <T> T getValueCommon(Element annotated,
                                        AnnotationMirror mirror,
                                        Class<T> type,
                                        AnnotationValue value) {
        Class<T> boxed = Primitives.wrap(type);
        GeneralResult result = value.accept(GENERAL_VISITOR, boxed::isInstance);
        if (result.bad != null) {
            throw new ProcessingException(
                "Value is not of expected type " + boxed.getCanonicalName()
                    + ", got " + result.bad.getClass().getCanonicalName())
                .withElement(annotated).withAnnotation(mirror);
        }
        return Objects.requireNonNull(boxed.cast(result.good));
    }

    public static <T> List<T> getList(Element annotated,
                                      AnnotationMirror mirror,
                                      String name,
                                      Class<T> type) {
        return getAnnotationValue(mirror, name)
            .accept(new SimpleAnnotationValueVisitor8<List<T>, Void>() {
                @Override
                public List<T> visitArray(List<? extends AnnotationValue> vals, Void unused) {
                    ImmutableList.Builder<T> builder = ImmutableList.builder();
                    for (AnnotationValue val : vals) {
                        builder.add(getValueCommon(
                            annotated,
                            mirror,
                            type,
                            val
                        ));
                    }
                    return builder.build();
                }
            }, null);
    }

}
