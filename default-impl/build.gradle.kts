plugins {
    id("piston.java-conventions")
}

dependencies {
    api(project(":core"))
    implementation(libs.log4jApi)
    compileOnly(libs.autoValueAnnotations)
    annotationProcessor(libs.autoValueProcessor)
    compileOnlyApi(libs.autoService)
    annotationProcessor(libs.autoService)
    testRuntimeOnly(libs.log4jCore)
}
