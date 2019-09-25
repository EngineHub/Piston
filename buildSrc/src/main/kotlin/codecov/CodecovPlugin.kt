package codecov

import com.google.gradle.osdetector.OsDetector
import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.KotlinClosure0
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import java.nio.file.Files
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermissions

val Project.codecov get() = the<CodecovExtension>()

class CodecovPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            apply(plugin = "com.google.osdetector")
            apply(plugin = "de.undercouch.download")
            extensions.create<CodecovExtension>("codecov", project)
            setupTasks()
        }
    }

    private fun Project.setupTasks() {
        val downloadCodecov = tasks.register<Download>("downloadCodecov") {
            val codecovCache = gradle.gradleUserHomeDir.resolve("codecov")
            // no support for provider yet
            // https://github.com/michel-kraemer/gradle-download-task/issues/142
            val src = KotlinClosure0({
                "https://github.com/codecov/codecov-exe/releases/download/${codecov.version.get()}/${codecovPackageName()}"
            })
            src(src)
            val dest = KotlinClosure0({
                codecovCache.resolve("${codecov.version.get()}/codecov-executable")
            })
            dest(dest)
            downloadTaskDir(codecovCache)
            onlyIfModified(true)
            useETag(true)
            tempAndMove(true)
        }
        tasks.register("uploadCodecov") {
            val reportFile = codecov.reportTask.map { it.reports.xml.destination }
            dependsOn(codecov.reportTask, downloadCodecov)
            inputs.files(reportFile, downloadCodecov)
            inputs.property("token", codecov.token)
            doFirst {
                // chmod executable
                val executableFile = downloadCodecov.get().dest.toPath()
                Files.getFileAttributeView(executableFile, PosixFileAttributeView::class.java)?.let { posix ->
                    val perms = posix.readAttributes().permissions() + PosixFilePermissions.fromString("--x--x--x")
                    posix.setPermissions(perms)
                }
                exec {
                    executable(executableFile)
                    args("-f", reportFile.get(), "-t", codecov.token.get())
                }
            }
        }
    }

    private fun Project.codecovPackageName(): String {
        return when (the<OsDetector>().os) {
            "windows" -> "codecov-windows-x64.exe"
            "osx" -> "codecov-osx-x64"
            else -> "codecov-linux-x64"
        }
    }

}
