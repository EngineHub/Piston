plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("kapt") version "2.0.0"
}

applyCoreApConfig()

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

    "testImplementation"(Libs.guava)

    "testImplementation"(Libs.mockito)
    "testRuntimeOnly"(Libs.log4jCore)
    "testImplementation"(project(":default-impl"))
    "testCompileOnly"(Libs.autoService)
    "kaptTest"(Libs.autoService)
    "kaptTest"(project(":core-ap:processor"))
}
