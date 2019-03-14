applyCommonConfig()

dependencies {
    "implementation"(project(":core"))
    "implementation"(project(":core-ap:core-ap-annotations"))
    "implementation"(Libs.guava)
    "implementation"(Libs.javapoet)
    "implementation"(Libs.autoCommon)
    "compileOnly"(Libs.autoValueAnnotations)
    "annotationProcessor"(Libs.autoValueProcessor)
    "compileOnly"(Libs.autoService)
    "annotationProcessor"(Libs.autoService)

    "testImplementation"(Libs.compileTesting)
    "testImplementation"(Libs.mockito)
    "testImplementation"(project(":default-impl"))
    "testAnnotationProcessor"(project(":core-ap:core-ap-processor"))
}

configurations.getByName("testAnnotationProcessor")
        .extendsFrom(configurations.getByName("runtimeClasspath"))
