package org.enginehub.piston.util;

import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.enginehub.piston.ColorConfig;

import javax.annotation.Nullable;
import java.util.stream.Collector;

public class ComponentHelper {

    /**
     * Join components together with a `|`, coloring the bar with {@link
     * ColorConfig#getPartWrapping()}.
     */
    public static Collector<Component, ?, Component> joiningWithBar() {
        return joiningTexts(
            Component.empty(),
            TextComponent.of("|", ColorConfig.getPartWrapping()),
            Component.empty()
        );
    }

    public static Collector<Component, ?, Component> joiningTexts(Component prefix, Component delimiter, Component suffix) {
        return Collector.of(
            () -> new ComponentJoiner(prefix, delimiter, suffix),
            ComponentJoiner::add,
            ComponentJoiner::merge,
            ComponentJoiner::finish
        );
    }

    private static final class ComponentJoiner {

        private final Component prefix;
        private final Component suffix;
        private final Component delimiter;

        @Nullable
        private TextComponent.Builder value;

        @Nullable
        private Component nullValue;

        private ComponentJoiner(Component prefix, Component delimiter, Component suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.delimiter = delimiter;
        }

        private TextComponent.Builder initBuilder() {
            if (value == null) {
                value = TextComponent.builder();
            } else {
                value.append(delimiter);
            }
            return value;
        }

        public void add(Component component) {
            initBuilder().append(component);
        }

        public ComponentJoiner merge(ComponentJoiner other) {
            if (other.value != null) {
                initBuilder().append(other.value.build());
            }
            return this;
        }

        public Component finish() {
            if (value == null) {
                if (nullValue != null) {
                    return nullValue;
                }
                return initBuilder().append(prefix).append(suffix).build();
            }
            return TextComponent.builder("")
                .append(prefix)
                .append(value.build().children())
                .append(suffix)
                .build();
        }

    }

    private ComponentHelper() {
    }
}
