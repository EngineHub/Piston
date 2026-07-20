plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinKapt)
    id("piston.core-ap-conventions")
}

kapt.includeCompileClasspath = false

tasks.test {
    // Crack open the compiler for compile testing
    jvmArgs(
        "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
    )
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-ap:annotations"))
    implementation(project(":core-ap:runtime"))
    implementation(libs.guava)
    implementation(libs.javapoet)
    implementation(libs.autoCommon)
    compileOnly(libs.autoValueAnnotations)
    kapt(libs.autoValueProcessor)
    compileOnly(libs.autoService)
    kapt(libs.autoService)

    testImplementation(kotlin("stdlib"))
    testImplementation(libs.compileTesting) {
        exclude("junit", "junit")
    }

    testImplementation(libs.mockito)
    testRuntimeOnly(libs.log4jCore)
    testImplementation(project(":default-impl"))
    testCompileOnly(libs.autoService)
    kaptTest(libs.autoService)
    kaptTest(project(":core-ap:processor"))
}
