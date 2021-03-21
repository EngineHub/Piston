applyCommonConfig()

dependencies {
    "api"(project(":core"))
    "implementation"(Libs.log4jApi)
    "compileOnly"(Libs.autoValueAnnotations)
    "annotationProcessor"(Libs.autoValueProcessor)
    "compileOnly"(Libs.autoService)
    "annotationProcessor"(Libs.autoService)
    "testRuntimeOnly"(Libs.log4jCore)
}
