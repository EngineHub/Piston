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

package org.enginehub.piston.gen.util

import com.google.auto.common.BasicAnnotationProcessor
import com.google.auto.common.MoreElements.getAnnotationMirror
import com.google.common.base.Throwables
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.google.common.collect.SetMultimap
import com.google.testing.compile.Compilation
import com.google.testing.compile.CompilationSubject.assertThat
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.TypeSpec
import org.enginehub.piston.gen.className
import org.enginehub.piston.gen.compiler
import org.enginehub.piston.gen.toFileInPackage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.tools.Diagnostic

@DisplayName("AnnoValueExtraction")
class AnnoValueExtractionTest {

    annotation class ProcessingKey(
        val value: String,
        val many: Array<String>
    )

    private val value = "test-value"
    private val value2 = "tester-value"
    private val annoCode = TypeSpec.classBuilder("AnnoContainer")
        .addAnnotation(AnnotationSpec.builder(className<ProcessingKey>())
            .addMember("value", "\$S", value)
            .addMember("many", "{ \$S, \$S }", value, value2)
            .build())
        .build()
        .toFileInPackage()

    private fun withTargetClass(doProcess: (Element) -> Unit): Compilation {
        return compiler().withProcessors(
            object : BasicAnnotationProcessor() {

                override fun getSupportedSourceVersion(): SourceVersion {
                    return SourceVersion.latestSupported()
                }

                override fun initSteps(): Iterable<ProcessingStep> {
                    return ImmutableList.of(object : ProcessingStep {
                        override fun annotations(): Set<Class<out Annotation>> {
                            return ImmutableSet.of(ProcessingKey::class.java)
                        }

                        override fun process(elementsByAnnotation: SetMultimap<Class<out Annotation>, Element>): Set<Element> {
                            try {
                                return doProcess(elementsByAnnotation.get(ProcessingKey::class.java))
                            } catch (e: ProcessingException) {
                                val message = StringBuilder(e.message)
                                val cause = e.cause
                                if (cause != null) {
                                    message.append('\n')
                                        .append(Throwables.getStackTraceAsString(cause))
                                }
                                processingEnv.messager.printMessage(
                                    Diagnostic.Kind.ERROR,
                                    message,
                                    e.causingElement,
                                    e.causingMirror
                                )
                                return ImmutableSet.of()
                            }

                        }

                        private fun doProcess(elements: Set<Element>): Set<Element> {
                            doProcess(elements.first())
                            return ImmutableSet.of()
                        }
                    })
                }
            }
        ).compile(annoCode)
    }

    @DisplayName("can extract String from annotation")
    @Test
    fun extractStringFromAnnotation() {
        var str: String? = null
        val compilation = withTargetClass { element ->
            val annotation = getAnnotationMirror(element, ProcessingKey::class.java)
                .toJavaUtil().orElseThrow { AssertionError("No processing key annotation") }
            str = AnnoValueExtraction.getValue(element, annotation, "value", String::class.java)
        }
        assertThat(compilation).succeededWithoutWarnings()
        assertEquals(value, str)
    }

    @DisplayName("cannot extract wrong type from annotation")
    @Test
    fun extractWrongTypeFromAnnotation() {
        val compilation = withTargetClass { element ->
            val annotation = getAnnotationMirror(element, ProcessingKey::class.java)
                .toJavaUtil().orElseThrow { AssertionError("No processing key annotation") }
            AnnoValueExtraction.getValue(element, annotation, "value", Int::class.java)
        }
        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("Value is not of expected type java.lang.Integer")
            .inFile(annoCode)
            .onLineContaining("ProcessingKey(")
    }

    @DisplayName("can extract multiple String from annotation")
    @Test
    fun extractMultipleStringFromAnnotation() {
        var strs: List<String>? = null
        val compilation = withTargetClass { element ->
            val annotation = getAnnotationMirror(element, ProcessingKey::class.java)
                .toJavaUtil().orElseThrow { AssertionError("No processing key annotation") }
            strs = AnnoValueExtraction.getList(element, annotation, "many", String::class.java)
        }
        assertThat(compilation).succeededWithoutWarnings()
        assertEquals(listOf(value, value2), strs)
    }

}