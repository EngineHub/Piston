package com.enginehub.piston;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;

@AutoValue
public abstract class CommandInfo {

    public static Builder builder() {
        Builder builder = new AutoValue_CommandInfo.Builder();
        builder.requiredVariablesBuilder();
        builder.footer(null);
        builder.parts(ImmutableList.of());
        return builder;
    }

    @AutoValue.Builder
    public interface Builder {
        Builder name(String name);

        Builder aliases(Collection<String> aliases);

        Builder description(String description);

        Builder footer(@Nullable String footer);

        Builder parts(Collection<CommandPartInfo> parts);

        ImmutableList.Builder<RequiredVariable> requiredVariablesBuilder();

        default Builder addRequiredVariable(RequiredVariable var) {
            requiredVariablesBuilder().add(var);
            return this;
        }

        Builder condition(@Nullable CodeBlock condition);

        CommandInfo build();
    }

    CommandInfo() {
    }

    public abstract String getName();

    public abstract ImmutableList<String> getAliases();

    public abstract String getDescription();

    public abstract Optional<String> getFooter();

    public abstract ImmutableList<CommandPartInfo> getParts();

    public abstract ImmutableList<RequiredVariable> getRequiredVariables();

    public abstract Optional<CodeBlock> getCondition();

}
