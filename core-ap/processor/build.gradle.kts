import org.gradle.internal.jvm.Jvm
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
    kotlin("kapt") version "1.3.72"
}

applyCoreApConfig()

kapt.includeCompileClasspath = false

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
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

    if (JavaVersion.current() <= JavaVersion.VERSION_1_8) {
        // Needs tools.jar on JDK 8 or less
        "testRuntimeOnly"(files(Jvm.current().toolsJar ?: throw IllegalStateException("No tools.jar is present. Please ensure you are using JDK 8.")))
    }
    "testImplementation"(Libs.mockito)
    "testImplementation"(Libs.logbackCore)
    "testImplementation"(Libs.logbackClassic)
    "testImplementation"(project(":default-impl"))
    "testCompileOnly"(Libs.autoService)
    "kaptTest"(Libs.autoService)
    "kaptTest"(project(":core-ap:processor"))
}
