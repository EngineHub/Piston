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

package org.enginehub.piston.util;

import com.google.common.base.Joiner;
import net.kyori.text.Component;
import net.kyori.text.KeybindComponent;
import net.kyori.text.TranslatableComponent;
import net.kyori.text.serializer.plain.PlainComponentSerializer;
import org.enginehub.piston.config.ConfigHolder;

import java.util.Iterator;
import java.util.List;

public class TextHelper {

    private static final PlainComponentSerializer PLAIN_COMPONENT_SERIALIZER = new PlainComponentSerializer(
        KeybindComponent::keybind,
        translatableComponent -> {
            StringBuilder builder = new StringBuilder();
            appendTranslatableTo(builder, translatableComponent);
            return builder.toString();
        }
    );

    private static final ConfigHolder CONFIG = ConfigHolder.create();

    public static String reduceToText(Component component) {
        StringBuilder text = new StringBuilder();
        appendTextTo(text, CONFIG.replace(component));
        return text.toString();
    }

    private static void appendTextTo(StringBuilder builder, Component component) {
        PLAIN_COMPONENT_SERIALIZER.serialize(builder, component);
    }

    private static void appendTranslatableTo(StringBuilder builder, TranslatableComponent component) {
        builder.append(component.key());
        List<Component> args = component.args();
        if (args.size() > 0) {
            builder.append('[');
            for (Iterator<Component> parts = args.iterator(); parts.hasNext(); ) {
                appendTextTo(builder, parts.next());
                while (parts.hasNext()) {
                    builder.append(", ");
                    appendTextTo(builder, parts.next());
                }
            }
            Joiner.on(", ").appendTo(builder, args);
            builder.append(']');
        }
    }

    private TextHelper() {
    }

}
