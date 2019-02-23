applyCommonConfig()

dependencies {
    "api"(project(":core"))
    "compileOnly"(Libs.autoValueAnnotations)
    "annotationProcessor"(Libs.autoValueProcessor)
    "compileOnly"(Libs.autoService)
    "annotationProcessor"(Libs.autoService)

    "testImplementation"(Libs.junit)
}
