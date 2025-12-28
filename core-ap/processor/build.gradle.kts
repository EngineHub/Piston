plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("kapt") version "2.3.0"
    id("buildlogic.core-ap")
}

kapt.includeCompileClasspath = false

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
    "implementation"(libs.guava)
    "implementation"(libs.javapoet)
    "implementation"(libs.autoCommon)
    "compileOnly"(libs.autoValue.annotations)
    "kapt"(libs.autoValue)
    "compileOnly"(libs.autoService)
    "kapt"(libs.autoService)

    "testImplementation"(kotlin("stdlib-jdk8"))
    "testImplementation"(libs.compileTesting) {
        exclude("junit", "junit")
    }

    "testImplementation"(libs.guava)

    "testRuntimeOnly"(libs.log4j.core)
    "testImplementation"(project(":default-impl"))
    "testCompileOnly"(libs.autoService)
    "kaptTest"(libs.autoService)
    "kaptTest"(project(":core-ap:processor"))
}
