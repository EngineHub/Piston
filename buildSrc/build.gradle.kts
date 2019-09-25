plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation("gradle.plugin.net.minecrell:licenser:0.4.1")
    implementation("net.ltgt.apt-idea:net.ltgt.apt-idea.gradle.plugin:0.21")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.9.5")

    // Codecov plugin internals
    implementation("com.google.gradle:osdetector-gradle-plugin:1.6.2")
    implementation("de.undercouch:gradle-download-task:4.0.0")
}
