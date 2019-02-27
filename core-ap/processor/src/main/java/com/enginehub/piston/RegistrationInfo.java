package com.enginehub.piston;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;

import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;
import java.util.Collection;

@AutoValue
abstract class RegistrationInfo {

    static Builder builder() {
        return new AutoValue_RegistrationInfo.Builder();
    }

    @AutoValue.Builder
    interface Builder {

        Builder name(String name);

        Builder targetClassName(ClassName className);

        Builder classVisibility(@Nullable Modifier visibility);

        Builder javaxInjectClassName(@Nullable ClassName className);

        Builder commands(Collection<CommandInfo> commands);

        RegistrationInfo build();

    }

    RegistrationInfo() {
    }

    abstract String getName();

    abstract ClassName getTargetClassName();

    @Nullable
    abstract Modifier getClassVisibility();

    @Nullable
    abstract ClassName getJavaxInjectClassName();

    abstract ImmutableList<CommandInfo> getCommands();

}
