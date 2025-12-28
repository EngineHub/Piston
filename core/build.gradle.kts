plugins {
    id("buildlogic.common")
}

dependencies {
    "api"(libs.guava)
    "api"(libs.kyoriText.api)
    "implementation"(libs.kyoriText.serializer.plain)
    "compileOnly"(libs.autoValue.annotations)
    "annotationProcessor"(libs.autoValue)
}
