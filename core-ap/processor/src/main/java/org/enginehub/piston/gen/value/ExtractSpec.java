/*
 * Piston, a flexible command management system.
 * Copyright (C) EngineHub <https://www.enginehub.org>
 * Copyright (C) Piston contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.piston.gen.value;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Specification for extracting a value.
 */
@AutoValue
public abstract class ExtractSpec {

    @FunctionalInterface
    public interface ExtractMethodBody {

        /**
         * Generate extraction code, given that the parameter's part is stored in the given field.
         *
         * <p>{@code partFieldName} will be {@code null} if there is no field.</p>
         */
        CodeBlock generate(@Nullable String partFieldName);

    }

    public static Builder builder() {
        return new AutoValue_ExtractSpec.Builder();
    }

    @AutoValue.Builder
    public interface Builder {

        Builder name(String name);

        Builder type(TypeName type);

        Builder extractMethodBody(ExtractMethodBody body);

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
    public abstract ExtractMethodBody getExtractMethodBody();

    /**
     * Generated method body using current name.
     */
    @Memoized
    CodeBlock getGeneratedMethodBody() {
        return getExtractMethodBody().generate(getName());
    }

    public abstract Builder toBuilder();

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ExtractSpec)) {
            return false;
        }
        ExtractSpec spec = (ExtractSpec) obj;
        boolean fastChecks = Objects.equals(getName(), spec.getName())
            && Objects.equals(getType(), spec.getType());
        if (!fastChecks) {
            return false;
        }
        CodeBlock body = getGeneratedMethodBody();
        CodeBlock otherBody = spec.getGeneratedMethodBody();
        return Objects.equals(body, otherBody);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getName(), getType(), getGeneratedMethodBody());
    }
}
