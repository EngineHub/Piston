pluginManagement {
    repositories {
        maven {
            name = "EngineHub"
            url = uri("https://repo.enginehub.org/libs-release/")
        }
        maven {
            name = "EngineHub Central Mirror"
            url = uri("https://repo.enginehub.org/internal/maven-central-proxy/")
        }
        maven {
            name = "EngineHub Plugin Portal Mirror"
            url = uri("https://repo.enginehub.org/internal/plugin-portal-proxy/")
        }
    }
}
plugins {
    id("org.enginehub.crankcase.repo-reconfiguration") version "0.1.2"
}
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
