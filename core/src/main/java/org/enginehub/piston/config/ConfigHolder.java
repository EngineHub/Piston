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

import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;

public class ConfigHolder {

    static {
        // Initialize Config subclasses
        ColorConfig.mainText();
        TextConfig.commandPrefix();
    }

    public static ConfigHolder create() {
        return new ConfigHolder(Config.defaultInstances);
    }

    private final Map<String, Config<?>> configs = new HashMap<>();

    private ConfigHolder(Map<String, Config<?>> configs) {
        this.configs.putAll(configs);
    }

    public Map<String, Config<?>> getConfigs() {
        return configs;
    }

    public <T> Config<T> getConfig(Config<T> defaultValue) {
        @SuppressWarnings("unchecked")
        Config<T> c = (Config<T>) configs.computeIfAbsent(defaultValue.getKey(), k -> defaultValue);
        return c;
    }

    public void addConfig(Config<?> config) {
        configs.put(config.getKey(), config);
    }

    /**
     * Replaces config placeholders in the component.
     *
     * @param input the component to replace placeholders in
     * @return the new component with no placeholders
     * @deprecated Use {@link ConfigRenderer} instead
     */
    @Deprecated
    public Component replace(Component input) {
        return ConfigRenderer.getInstance().render(input, this);
    }

}
