plugins {
    id("buildlogic.common")
}

dependencies {
    "api"(project(":core"))
    "implementation"(libs.log4j.api)
    "compileOnly"(libs.autoValue.annotations)
    "annotationProcessor"(libs.autoValue)
    "compileOnlyApi"(libs.autoService)
    "annotationProcessor"(libs.autoService)
    "testRuntimeOnly"(libs.log4j.core)
}
