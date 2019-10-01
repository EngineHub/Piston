applyCommonConfig()

dependencies {
    "api"(Libs.guava)
    "api"(Libs.kyoriText)
    "api"(Libs.javaxAnnotations)
    "implementation"(Libs.kyoriTextPlain)
    "compileOnly"(Libs.autoValueAnnotations)
    "annotationProcessor"(Libs.autoValueProcessor)
    "testImplementation"(Libs.mockito)
}
