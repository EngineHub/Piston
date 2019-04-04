rootProject.name = "piston"

include(":core", ":default-impl")
listOf("annotations", "processor", "runtime").forEach {
    include(":core-ap:$it")
    project(":core-ap:$it").name = "core-ap-$it"
}
