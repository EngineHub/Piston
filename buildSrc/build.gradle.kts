plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    jcenter()
    gradlePluginPortal()
    maven {
        name = "EngineHub Repository"
        url = uri("https://maven.enginehub.org/repo/")
    }
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation("org.enginehub.gradle:gradle-codecov-plugin:0.1.0-SNAPSHOT")
    implementation("gradle.plugin.net.minecrell:licenser:0.4.1")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.13.0")
}
