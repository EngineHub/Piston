package com.enginehub.piston;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class CommandPartInfo {

    public static Builder builder() {
        return new AutoValue_CommandPartInfo.Builder();
    }

    @AutoValue.Builder
    public interface Builder {
        CommandPartInfo build();
    }
}
