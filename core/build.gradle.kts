applyCommonConfig()

dependencies {
    "api"(Libs.guava)
    "api"(Libs.kyoriAdventure)
    "api"(Libs.jspecify)
    "implementation"(Libs.kyoriAdventureTextPlain)
    "compileOnly"(Libs.autoValueAnnotations)
    "annotationProcessor"(Libs.autoValueProcessor)
    "testImplementation"(Libs.mockito)
}
