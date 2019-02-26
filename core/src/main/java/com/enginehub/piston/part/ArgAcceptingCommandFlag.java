package com.enginehub.piston.part;

import com.enginehub.piston.converter.ArgumentConverter;
import com.enginehub.piston.converter.ArgumentConverters;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;

@AutoValue
public abstract class ArgAcceptingCommandFlag<T> implements CommandFlag, ArgAcceptingCommandPart<T> {

    public static <T> Builder<T> builder(char name,
                                         String description,
                                         ArgumentConverter<T> converter) {
        return new AutoValue_ArgAcceptingCommandFlag.Builder<>()
            .named(name)
            .describedBy(description)
            .convertedBy(converter)
            .defaultsTo(ImmutableList.of());
    }

    @AutoValue.Builder
    public abstract static class Builder<T> {

        public final Builder<T> named(char name) {
            return name(name);
        }

        abstract Builder<T> name(char name);

        public final Builder<T> describedBy(String description) {
            return description(description);
        }

        abstract Builder<T> description(String description);

        public final <U> Builder<U> ofType(Class<U> type) {
            return ofType(TypeToken.of(type));
        }

        public final <U> Builder<U> ofType(TypeToken<U> type) {
            return convertedBy(ArgumentConverters.get(type));
        }

        // auto-value workaround, since it can't tell we're changing the type of the whole
        // builder!
        @SuppressWarnings("unchecked")
        public final <U> Builder<U> convertedBy(ArgumentConverter<U> converter) {
            return (Builder<U>) converter((ArgumentConverter<T>) converter);
        }

        abstract Builder<T> converter(ArgumentConverter<T> converter);

        public final Builder<T> defaultsTo(Iterable<T> defaults) {
            return defaults(defaults);
        }

        abstract Builder<T> defaults(Iterable<T> defaults);

        public abstract ArgAcceptingCommandFlag<T> build();
    }

    ArgAcceptingCommandFlag() {
    }

    @Override
    public String getTextRepresentation() {
        return "[-" + getName() +
            " <" + getConverter().describeAcceptableArguments() + ">]";
    }
}
