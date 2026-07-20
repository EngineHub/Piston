plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven {
        name = "EngineHub"
        url = uri("https://repo.enginehub.org/libs-release/")
    }
}

dependencies {
    implementation(libs.crankcase.checkstyle)
    implementation(libs.crankcase.java)
    implementation(libs.crankcase.javaLibrary)
    implementation(libs.crankcase.licensing)
    implementation(libs.crankcase.publishing)
}
