import org.gradle.internal.jvm.Jvm
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("kapt") version "1.9.22"
}

applyCoreApConfig()

kapt.includeCompileClasspath = false

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.test {
    // Crack open the compiler for compile testing
    jvmArgs(
        "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
    )
}

dependencies {
    "implementation"(project(":core"))
    "implementation"(project(":core-ap:annotations"))
    "implementation"(project(":core-ap:runtime"))
    "implementation"(Libs.guava)
    "implementation"(Libs.javapoet)
    "implementation"(Libs.autoCommon)
    "compileOnly"(Libs.autoValueAnnotations)
    "kapt"(Libs.autoValueProcessor)
    "compileOnly"(Libs.autoService)
    "kapt"(Libs.autoService)

    "testImplementation"(kotlin("stdlib-jdk8"))
    "testRuntimeOnly"(Libs.junitVintageEngine)
    "testImplementation"(Libs.compileTesting) {
        exclude("junit", "junit")
    }

    "testImplementation"("com.google.guava:guava") {
        version {
            prefer("27.1-jre")
            reject("21.0")
            because("Compile Testing & Truth won't work with 21.0")
        }
    }

    "testImplementation"(Libs.mockito)
    "testRuntimeOnly"(Libs.log4jCore)
    "testImplementation"(project(":default-impl"))
    "testCompileOnly"(Libs.autoService)
    "kaptTest"(Libs.autoService)
    "kaptTest"(project(":core-ap:processor"))
}
