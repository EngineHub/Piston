package org.enginehub.piston.value;

import com.google.auto.value.AutoValue;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Specification for extracting a value.
 */
@AutoValue
public abstract class ExtractSpec {

    public static Builder builder() {
        return new AutoValue_ExtractSpec.Builder();
    }

    @AutoValue.Builder
    public interface Builder {

        Builder name(String name);

        Builder type(TypeName type);

        Builder extractMethodBody(CodeBlock body);

        ExtractSpec build();
    }

    /**
     * Name for the extracted value.
     */
    public abstract String getName();

    /**
     * Type of the value extracted.
     */
    public abstract TypeName getType();

    /**
     * Code for the extract method, if generated.
     */
    public abstract CodeBlock getExtractMethodBody();

    public abstract Builder toBuilder();

}
