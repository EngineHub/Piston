plugins {
    id("piston.java-conventions")
}

dependencies {
    api(libs.guava)
    api(libs.kyoriAdventure)
    api(libs.jspecify)
    implementation(libs.kyoriAdventureTextPlain)
    compileOnly(libs.autoValueAnnotations)
    annotationProcessor(libs.autoValueProcessor)
    testImplementation(libs.mockito)
}
