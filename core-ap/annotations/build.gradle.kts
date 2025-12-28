plugins {
    id("buildlogic.core-ap")
}

dependencies {
    "api"(project(":core"))
    "api"(project(":core-ap:runtime"))
}
