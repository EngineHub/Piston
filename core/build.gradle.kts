applyCommonConfig()

dependencies {
    "api"(Libs.guava)
    "api"(Libs.kyoriText)
    "api"(Libs.checkerQualAnnotations)
    "compileOnly"(Libs.autoValueAnnotations)
    "annotationProcessor"(Libs.autoValueProcessor)
}
