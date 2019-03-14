rootProject.name = "piston"

include(":core", ":default-impl")
include(":core-ap:annotations", ":core-ap:processor")
project(":core-ap:annotations").name = "core-ap-annotations"
project(":core-ap:processor").name = "core-ap-processor"
