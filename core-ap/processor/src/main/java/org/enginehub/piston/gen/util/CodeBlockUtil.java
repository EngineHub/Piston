/*
 * Piston, a flexible command management system.
 * Copyright (C) EngineHub <http://www.enginehub.com>
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

package org.enginehub.piston.gen.util;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;

import javax.annotation.Nullable;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class CodeBlockUtil {
    public static CodeBlock stringListForGen(Stream<String> strings) {
        return listForGen(strings.map(x -> CodeBlock.of("$S", x)));
    }

    public static CodeBlock listForGen(Stream<CodeBlock> rawCode) {
        return rawCode.collect(joining(
            CodeBlock.of("$T.of(", ImmutableList.class),
            CodeBlock.of(")"),
            CodeBlock.of(", ")
        ));
    }

    public static Collector<CodeBlock, ?, CodeBlock> joining(String delimiter) {
        return joining(CodeBlock.of(""), CodeBlock.of(""), CodeBlock.of("$L", delimiter));
    }

    public static Collector<CodeBlock, ?, CodeBlock> joining(
        CodeBlock prefix, CodeBlock suffix, CodeBlock delimiter
    ) {
        return Collector.of(
            () -> new CodeBlockJoiner(prefix, suffix, delimiter),
            CodeBlockJoiner::add,
            CodeBlockJoiner::merge,
            CodeBlockJoiner::finish
        );
    }

    private static final class CodeBlockJoiner {

        private final CodeBlock prefix;
        private final CodeBlock suffix;
        private final CodeBlock delimiter;
        @Nullable
        private CodeBlock.Builder value;

        CodeBlockJoiner(CodeBlock prefix, CodeBlock suffix, CodeBlock delimiter) {
            this.prefix = requireNonNull(prefix);
            this.suffix = requireNonNull(suffix);
            this.delimiter = requireNonNull(delimiter);
        }

        private CodeBlock.Builder prepareBuilder() {
            if (value != null) {
                value.add(delimiter);
            } else {
                value = CodeBlock.builder();
            }
            return value;
        }

        public void add(CodeBlock block) {
            prepareBuilder().add(block);
        }

        public CodeBlockJoiner merge(CodeBlockJoiner joiner) {
            // do not inline -- want to finish the joiner before adding,
            // in case `joiner` == `this`
            CodeBlock content = joiner.finishRaw().build();
            prepareBuilder().add(content);
            return this;
        }

        /**
         * The raw content of the `value` builder, non-null.
         */
        private CodeBlock.Builder finishRaw() {
            return value == null ? CodeBlock.builder() : value;
        }

        public CodeBlock finish() {
            return prefix.toBuilder()
                .add(finishRaw().build())
                .add(suffix)
                .build();
        }
    }
}
