package codecov

import org.gradle.api.Project
import org.gradle.kotlin.dsl.property
import org.gradle.testing.jacoco.tasks.JacocoReport

open class CodecovExtension(
    project: Project
) {
    val version = project.objects.property<String>().convention("1.7.2")
    val token = project.objects.property<String>().convention(System.getenv("CODECOV_TOKEN"))
    val reportTask = project.objects.property<JacocoReport>()
}
