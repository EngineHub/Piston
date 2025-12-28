import buildlogic.getLibrary
import buildlogic.stringyLibs
import net.octyl.levelheadered.LevelHeaderedExtension
import org.gradle.plugins.ide.idea.model.IdeaModel

plugins {
    id("eclipse")
    id("idea")
    id("net.octyl.level-headered")
    id("checkstyle")
    id("java-library")
    id("maven-publish")
}

group = rootProject.group
version = rootProject.version

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(1, TimeUnit.DAYS)
    }
}

dependencies {
    for (conf in listOf("implementation", "api")) {
        if (!configurations.names.contains(conf)) {
            continue
        }

        add(conf, platform(stringyLibs.getLibrary("log4j-bom")).map {
            val dep = create(it)
            dep.because("Mojang provides Log4j")
            dep
        })

        constraints {
            add(conf, stringyLibs.getLibrary("guava")) {
                because("Mojang provides Guava")
            }
            add(conf, stringyLibs.getLibrary("gson")) {
                because("Mojang provides Gson")
            }
            add(conf, stringyLibs.getLibrary("fastutil")) {
                because("Mojang provides FastUtil")
            }
        }
    }

    "api"(stringyLibs.getLibrary("jsr305"))
    "testImplementation"(platform(stringyLibs.getLibrary("junit-bom")))
    "testImplementation"(stringyLibs.getLibrary("junit-jupiter-api"))
    "testImplementation"(stringyLibs.getLibrary("junit-jupiter-params"))
    "testImplementation"(platform(stringyLibs.getLibrary("mockito-bom")))
    "testImplementation"(stringyLibs.getLibrary("mockito-core"))
    "testImplementation"(stringyLibs.getLibrary("mockito-junit-jupiter"))
    "testRuntimeOnly"(stringyLibs.getLibrary("junit-jupiter-engine"))
    "testRuntimeOnly"(stringyLibs.getLibrary("junit-platform-launcher"))
}

configure<LevelHeaderedExtension> {
    headerTemplate(rootProject.file("HEADER.txt"))
}

configure<CheckstyleExtension> {
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    toolVersion = "10.16.0"
}

configure<JavaPluginExtension> {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
    withJavadocJar()
    withSourcesJar()
}

configure<PublishingExtension> {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
        }
    }
}

configure<IdeaModel> {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    withType<Test>().configureEach {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
    }

    // Java 8 turns on doclint which we fail
    withType<Javadoc>().configureEach {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).apply {
            addBooleanOption("Werror", true)
            addBooleanOption("Xdoclint:all", true)
            addBooleanOption("Xdoclint:-missing", true)
            tags(
                "apiNote:a:API Note:",
                "implSpec:a:Implementation Requirements:",
                "implNote:a:Implementation Note:"
            )
        }
    }

    named<ProcessResources>("processTestResources") {
        from(rootProject.file("common-test-resources"))
    }

    named("check").configure {
        dependsOn("checkstyleMain", "checkstyleTest")
    }
}