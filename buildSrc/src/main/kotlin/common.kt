import org.cadixdev.gradle.licenser.LicenseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.CoreJavadocOptions
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.delegateClosureOf
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask

fun Project.applyCommonConfig(
    group: String = rootProject.group.toString()
) {
    apply(plugin = "java-library")
    apply(plugin = "java")
    apply(plugin = "org.cadixdev.licenser")
    apply(plugin = "maven-publish")
    apply(plugin = "com.jfrog.artifactory")
    apply(plugin = "jacoco")

    project.group = group

    configure<LicenseExtension> {
        setHeader(rootProject.file("HEADER.txt"))
        exclude("**/META-INF/**")
        exclude("**/*.properties")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        constraints {
            for (configuration in listOf("implementation", "api")) {
                configuration(Libs.guava) {
                    version {
                        // This isn't perfect -- I need some way to communicate that we should only build
                        // Against 21.0, but also that the tests NEED to use 27.1 to be compatible
                        // with google's truth library
                        require("21.0")
                        because("Minecraft uses Guava 21.0")
                    }
                }
            }
        }
    }

    tasks.named<Copy>("processTestResources") {
        from(rootProject.file("common-test-resources"))
    }

    configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }
    tasks.named<JavaCompile>("compileJava") {
        options.encoding = "UTF-8"
        options.release.set(8)
    }
    tasks.named<JavaCompile>("compileTestJava") {
        options.encoding = "UTF-8"
        options.release.set(8)
    }
    tasks.withType<Javadoc>().configureEach {
        (options as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    dependencies {
        "testImplementation"(Libs.junitApi)
        "testImplementation"(Libs.junitEngine)
    }

    addExtraArchiveArtifacts()

    configureMavenPublish()

    val build = tasks.named("build")
    rootProject.tasks.named("afterReleaseBuild").configure {
        dependsOn(build)
    }
}

private fun Project.addExtraArchiveArtifacts() {
    configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()
    }
}

private fun Project.configureMavenPublish() {
    configure<PublishingExtension> {
        publications {
            register<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()

                from(components["java"])
            }
        }
    }
}

fun Project.configureArtifactory() {
    apply(plugin = "maven-publish")
    apply(plugin = "com.jfrog.artifactory")
    val ext = extensions.extraProperties
    if (!project.hasProperty("artifactory_contextUrl"))
        ext["artifactory_contextUrl"] = "http://localhost"
    if (!project.hasProperty("artifactory_user"))
        ext["artifactory_user"] = "guest"
    if (!project.hasProperty("artifactory_password"))
        ext["artifactory_password"] = ""
    configure<ArtifactoryPluginConvention> {
        publish {
            contextUrl = project.property("artifactory_contextUrl").toString()
            repository {
                repoKey = when {
                    "SNAPSHOT" in project.version.toString() -> "libs-snapshot-local"
                    else -> "libs-release-local"
                }
                username = project.property("artifactory_user").toString()
                password = project.property("artifactory_password").toString()
            }
            defaults {
                publications("maven")
                setPublishArtifacts(true)
            }
        }
    }
    tasks.named<ArtifactoryTask>("artifactoryPublish") {
        skip = true
    }
}

fun Project.applyCoreApConfig() {
    applyCommonConfig(group = rootProject.group.toString() + ".core-ap")
}
