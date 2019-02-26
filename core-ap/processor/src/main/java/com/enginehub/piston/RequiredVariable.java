package com.enginehub.piston;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;

import java.util.Collection;

@AutoValue
public abstract class RequiredVariable {

    public static Builder builder() {
        return new AutoValue_RequiredVariable.Builder();
    }

    @AutoValue.Builder
    public interface Builder {

        Builder type(TypeName type);

        Builder name(String name);

        Builder annotations(Collection<AnnotationSpec> annotations);

        RequiredVariable build();

    }

    RequiredVariable() {
    }

    public abstract TypeName getType();

    public abstract String getName();

    public abstract ImmutableList<AnnotationSpec> getAnnotations();


}
