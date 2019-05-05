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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

class Annotations {

    private static final class MethodKey {
        static MethodKey from(Method method) {
            return of(method.getReturnType(), method.getName(), method.getParameterTypes());
        }

        static MethodKey of(Class<?> rtype, String name, Class<?>... ptypes) {
            return new MethodKey(name, ImmutableList.<Class<?>>builder()
                .add(rtype)
                .add(ptypes)
                .build());
        }

        private final String name;
        private final ImmutableList<Class<?>> signature;

        private MethodKey(String name, List<Class<?>> signature) {
            this.name = name;
            this.signature = ImmutableList.copyOf(signature);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodKey methodKey = (MethodKey) o;
            return name.equals(methodKey.name) &&
                signature.equals(methodKey.signature);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, signature);
        }
    }

    @FunctionalInterface
    private interface AnnoMethod {
        Object invoke(Class<? extends Annotation> type,
                      Map<String, Object> members,
                      Object[] args) throws Exception;
    }

    private static final Joiner.MapJoiner JOINER = Joiner.on(", ").withKeyValueSeparator("=");

    private static final Map<MethodKey, AnnoMethod> ANNOTATION_METHODS =
        ImmutableMap.of(
            MethodKey.of(Class.class, "annotationType"), (type, members, args) -> type,
            MethodKey.of(boolean.class, "equals", Object.class), (type, members, args) -> {
                if (!type.isInstance(args[0])) {
                    return false;
                }

                for (Method method : type.getDeclaredMethods()) {
                    String name = method.getName();
                    if (!Objects.equals(method.invoke(args[0]), members.get(name))) {
                        return false;
                    }
                }
                return true;
            },
            MethodKey.of(int.class, "hashCode"), (type, members, args) -> {
                int result = 0;
                for (String name : members.keySet()) {
                    Object value = members.get(name);
                    result += (127 * name.hashCode()) ^ (Objects.hash(value) - 31);
                }
                return result;
            },
            MethodKey.of(String.class, "toString"), (type, members, args) -> {
                StringBuilder output = new StringBuilder("@")
                    .append(type.getName())
                    .append('(');
                JOINER.appendTo(output, members.entrySet().stream()
                    .map(e -> Maps.immutableEntry(
                        e.getKey(),
                        valueToString(e.getValue())
                    ))
                    .iterator());
                return output.append(')').toString();
            }
        );

    private static String valueToString(@Nullable Object value) {
        if (value == null) {
            return "null";
        }
        if (value.getClass().isArray()) {
            return Arrays.deepToString((Object[]) value);
        }
        return value.toString();
    }

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    static Annotation allDefaultsAnnotation(Class<? extends Annotation> annotationType) {
        Map<String, Object> members = Stream.of(annotationType.getDeclaredMethods())
            .collect(toMap(
                Method::getName,
                m -> requireNonNull(m.getDefaultValue())
            ));
        return (Annotation) Proxy.newProxyInstance(
            requireNonNull(Key.class.getClassLoader()),
            new Class[] {annotationType},
            (proxy, method, args) -> {
                AnnoMethod call = ANNOTATION_METHODS.get(MethodKey.from(method));
                if (call != null) {
                    return call.invoke(annotationType, members, args);
                }
                Object value = members.get(method.getName());
                if (value != null) {
                    return value;
                }
                throw new IllegalStateException("Unknown method on " + annotationType + ": " + method);
            }
        );
    }
}
