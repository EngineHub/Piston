rootProject.name = "piston"

include(":core", ":default-impl")
listOf("annotations", "processor", "runtime").forEach {
    include(":core-ap:$it")
}
