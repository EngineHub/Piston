pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "EngineHub"
            url = uri("https://maven.enginehub.org/repo/")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    pluginManagement.repositories.forEach { repositories.add(it) }
    repositories.mavenCentral()
}

rootProject.name = "piston"

includeBuild("build-logic")

include("core", "default-impl")

listOf("annotations", "processor", "runtime").forEach {
    include("core-ap:$it")
}
