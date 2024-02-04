plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "EngineHub Repository"
        url = uri("https://maven.enginehub.org/repo/")
    }
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation("org.enginehub.gradle:gradle-codecov-plugin:0.2.1-SNAPSHOT")
    implementation("gradle.plugin.org.cadixdev.gradle:licenser:0.6.1")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:5.1.14")
}
