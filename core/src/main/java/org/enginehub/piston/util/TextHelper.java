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
import net.kyori.text.TextComponent;
import net.kyori.text.TranslatableComponent;

import java.util.Iterator;
import java.util.List;

public class TextHelper {

    public static String reduceToText(Component component) {
        StringBuilder text = new StringBuilder();
        appendTextTo(text, component);
        return text.toString();
    }

    private static void appendTextTo(StringBuilder builder, Component component) {
        if (component instanceof TextComponent) {
            builder.append(((TextComponent) component).content());
        } else if (component instanceof TranslatableComponent) {
            appendTranslatableTo(builder, (TranslatableComponent) component);
        }
        for (Component child : component.children()) {
            appendTextTo(builder, child);
        }
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
