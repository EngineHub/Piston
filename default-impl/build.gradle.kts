applyCommonConfig()

dependencies {
    "api"(project(":core"))
    "implementation"(Libs.slf4j)
    "compileOnly"(Libs.autoValueAnnotations)
    "annotationProcessor"(Libs.autoValueProcessor)
    "compileOnly"(Libs.autoService)
    "annotationProcessor"(Libs.autoService)
    "testImplementation"(Libs.logbackCore)
    "testImplementation"(Libs.logbackClassic)
}
