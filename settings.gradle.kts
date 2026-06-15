plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "piston"

include(":core", ":default-impl")
listOf("annotations", "processor", "runtime").forEach {
    include(":core-ap:$it")
}
