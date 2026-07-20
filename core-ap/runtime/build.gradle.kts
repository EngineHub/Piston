plugins {
    id("piston.core-ap-conventions")
}

dependencies {
    api(project(":core"))

    testImplementation(libs.mockito)
}
