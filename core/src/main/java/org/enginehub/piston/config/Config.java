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

package org.enginehub.piston.config;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Config<T> {

    static final Map<String, Config<?>> defaultInstances = new ConcurrentHashMap<>();
    private static final Set<String> defaultInstanceCheck = new CopyOnWriteArraySet<>();

    private final String key;
    private @Nullable T value;

    protected Config(String key, @Nullable T defaultValue) {
        this.key = key;
        checkValue(defaultValue);
        this.value = defaultValue;
        if (defaultInstanceCheck.add(key)) {
            defaultInstances.put(key, copyForDefault());
        }
    }

    protected abstract Config<T> copyForDefault();

    public String getKey() {
        return key;
    }

    public @Nullable
    T getValue() {
        return value;
    }

    public void setValue(@Nullable T value) {
        checkValue(value);
        this.value = value;
    }

    public Config<T> value(@Nullable T value) {
        setValue(value);
        return this;
    }

    protected void checkValue(@Nullable T value) {
    }

    public Component value() {
        return wrapInternal(ImmutableList.of());
    }

    protected Component wrapInternal(List<Component> args) {
        return Component.translatable(key, args);
    }

    protected abstract Component apply(TranslatableComponent placeholder);

}
