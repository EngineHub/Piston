package org.enginehub.piston.config;

import com.google.common.collect.ImmutableList;
import net.kyori.text.Component;
import net.kyori.text.TranslatableComponent;
import net.kyori.text.renderer.ComponentRenderer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

/**
 * Utility to render any config placeholders in a component using a given {@link ConfigHolder}.
 */
public class ConfigRenderer implements ComponentRenderer<ConfigHolder> {

    private static final ConfigRenderer INSTANCE = new ConfigRenderer();

    /**
     * Get an instance of the renderer.
     */
    public static ConfigRenderer getInstance() {
        return INSTANCE;
    }

    private ConfigRenderer() {
    }

    @Override
    public @NonNull Component render(@NonNull Component component, @NonNull ConfigHolder context) {
        component = replaceSubcomponents(component, context);
        if (component instanceof TranslatableComponent) {
            // check if replacing
            TranslatableComponent tc = (TranslatableComponent) component;
            Config<?> config = context.getConfigs().get(tc.key());
            if (config != null) {
                component = config.apply(tc);
            }
        }
        return component;
    }

    private Component replaceSubcomponents(Component component, ConfigHolder context) {
        if (component instanceof TranslatableComponent) {
            TranslatableComponent tc = (TranslatableComponent) component;
            List<Component> originalArgs = tc.args();
            List<Component> replacementArgs = renderList(originalArgs, context);
            if (originalArgs != replacementArgs) {
                component = tc.args(replacementArgs);
            }
            // fall-through to replace children if needed
        }

        List<Component> original = component.children();
        List<Component> replacement = renderList(original, context);
        return original != replacement
            ? component.children(replacement)
            : component;
    }

    private List<Component> renderList(List<Component> input, ConfigHolder context) {
        if (input.isEmpty()) {
            return input;
        }
        ImmutableList.Builder<Component> copy = ImmutableList.builder();
        boolean modified = false;
        for (Component component : input) {
            Component replacement = render(component, context);
            if (replacement != component) {
                modified = true;
            }
            copy.add(replacement);
        }
        return modified ? copy.build() : input;
    }
}
