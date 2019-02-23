applyCommonConfig()

dependencies {
    "api"(Libs.guava)
    "compileOnly"(Libs.autoValueAnnotations)
    "annotationProcessor"(Libs.autoValueProcessor)

    "testImplementation"(Libs.junit)
}
