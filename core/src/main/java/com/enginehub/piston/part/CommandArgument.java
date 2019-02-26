package com.enginehub.piston.part;

import com.enginehub.piston.converter.ArgumentConverter;
import com.enginehub.piston.converter.ArgumentConverters;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;

@AutoValue
public abstract class CommandArgument<T> implements ArgAcceptingCommandPart<T> {

    public static <T> Builder<T> builder(String name,
                                         String description,
                                         ArgumentConverter<T> converter) {
        return new AutoValue_CommandArgument.Builder<>()
            .named(name)
            .describedBy(description)
            .convertedBy(converter)
            .required()
            .defaultsTo(ImmutableList.of());
    }

    @AutoValue.Builder
    public abstract static class Builder<T> {

        public final Builder<T> named(String name) {
            return name(name);
        }

        abstract Builder<T> name(String name);

        public final Builder<T> describedBy(String description) {
            return description(description);
        }

        abstract Builder<T> description(String description);

        public final Builder<T> required() {
            return required(true);
        }

        public final Builder<T> optional() {
            return required(false);
        }

        abstract Builder<T> required(boolean required);

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
            return defaults(defaults).optional();
        }

        abstract Builder<T> defaults(Iterable<T> defaults);

        public abstract CommandArgument<T> build();
    }

    public abstract String getName();

    @Override
    public String getTextRepresentation() {
        return isRequired() ? getName() : "[" + getName() + "]";
    }

}
