package com.enginehub.piston;

import com.enginehub.piston.annotation.DependencySupport;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;

class CommandInfoDependencySupport implements DependencySupport {
    private final Multiset<String> nameMemory = HashMultiset.create();
    private final CommandInfo.Builder builder;

    public CommandInfoDependencySupport(CommandInfo.Builder builder) {
        this.builder = builder;
    }

    @Override
    public String requestInScope(TypeName type, String name, AnnotationSpec... annotations) {
        String realName = nameMemory.add(name)
            ? name
            : name + (nameMemory.count(name) - 1);
        builder.addRequiredVariable(RequiredVariable.builder()
            .type(type)
            .name(name)
            .annotations(ImmutableList.copyOf(annotations))
            .build());
        return realName;
    }
}
