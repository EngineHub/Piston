applyCommonConfig()

dependencies {
    "api"(Libs.guava)
    // We take Key & friends from Guice, since we'd rather not re-invent the wheel.
    "api"(Libs.guice)
    "compileOnly"(Libs.autoValueAnnotations)
    "annotationProcessor"(Libs.autoValueProcessor)
}
