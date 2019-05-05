import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.register

private val checkers = setOf(
        "nullness.NullnessChecker"
)

/**
 * Duplicate Java compilation processes to run the Checker Framework.
 */
fun Project.addCheckerFramework() {
    val jdk = configurations.register("checkerFrameworkAnnotatedJdk") {
        description = "JDK with Checker Framework annotations"
    }
    dependencies {
        jdk.name("org.checkerframework:jdk8:${Libs.checkerFrameworkVersion}")
    }

    convention.getPlugin<JavaPluginConvention>().sourceSets.forEach { sourceSet ->
        val checkerCompileOnly = configurations.register(abbrName(sourceSet.name, "CheckerCompileOnly")) {
            description = "Compile-time-only Checker dependencies for source set '${sourceSet.name}'"
        }
        val checkerAp = configurations.register(abbrName(sourceSet.name, "CheckerProcessors")) {
            description = "Annotation processors for source set '${sourceSet.name}'"
            extendsFrom(configurations.getByName(sourceSet.annotationProcessorConfigurationName))
            configurations.findByName(abbrNameKapt(sourceSet.name))?.let { kapt ->
                extendsFrom(kapt)
            }
        }
        dependencies {
            checkerAp.name("org.checkerframework:checker:${Libs.checkerFrameworkVersion}")
            checkerCompileOnly.name(Libs.guavaCompatibleErrorprone)
            checkerCompileOnly.name(Libs.guavaCompatible305)
        }
        val checkerCompile = tasks.register<JavaCompile>(abbrName(sourceSet.name, "CompileJavaWithChecker")) {
            val javaCompile = tasks.getByName<JavaCompile>(sourceSet.compileJavaTaskName)
            source = files(
                    sourceSet.java,
                    javaCompile.options.annotationProcessorGeneratedSourcesDirectory
            ).asFileTree
            dependsOn(javaCompile)
            classpath = files(sourceSet.compileClasspath, checkerCompileOnly.get())
            destinationDir = buildDir.resolve("tmp/classes/checker/${sourceSet.name}")
            options.annotationProcessorPath = files({ checkerAp.get().resolvedConfiguration.files })
            options.compilerArgs = listOf(
                    "-processor", checkers.joinToString(",") { "org.checkerframework.checker.$it" },
                    "-AskipDefs=\\.\\\$*AutoValue_",
                    "-Xbootclasspath/p:${jdk.get().asPath}"
            )
        }
        tasks.named("check").configure {
            dependsOn(checkerCompile)
        }
    }
}

fun abbrName(parentName: String, base: String): String {
    return when (parentName) {
        "main" -> base[0].toLowerCase() + base.drop(1)
        else -> parentName + base
    }
}

// kapt has a special convention
fun abbrNameKapt(parentName: String): String {
    return when (parentName) {
        "main" -> "kapt"
        else -> "kapt" + parentName[0].toUpperCase() + parentName.drop(1)
    }
}