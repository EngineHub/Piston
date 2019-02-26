package com.enginehub.piston.part;

import com.enginehub.piston.converter.ArgumentConverters;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class NoArgCommandFlag implements CommandFlag {

    public static NoArgCommandFlag.Builder builder(char name,
                                                   String description) {
        return new AutoValue_NoArgCommandFlag.Builder()
            .named(name)
            .describedBy(description);
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public final Builder named(char name) {
            return name(name);
        }

        abstract Builder name(char name);

        public final Builder describedBy(String description) {
            return description(description);
        }

        abstract Builder description(String description);

        public final ArgAcceptingCommandFlag.Builder<String> withRequiredArg() {
            NoArgCommandFlag flag = build();
            return ArgAcceptingCommandFlag.builder(
                flag.getName(),
                flag.getDescription(),
                ArgumentConverters.forString()
            );
        }

        public abstract NoArgCommandFlag build();

    }

    NoArgCommandFlag() {
    }

    @Override
    public String getTextRepresentation() {
        return "[-" + getName() + "]";
    }
}
