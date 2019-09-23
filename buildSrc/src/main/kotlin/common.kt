import net.minecrell.gradle.licenser.LicenseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.CoreJavadocOptions
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
import org.jfrog.gradle.plugin.artifactory.dsl.DoubleDelegateWrapper
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask

fun Project.applyCommonConfig(
        group: String = rootProject.group.toString()
) {
    apply(plugin = "java-library")
    apply(plugin = "java")
    apply(plugin = "net.minecrell.licenser")
    apply(plugin = "net.ltgt.apt-idea")
    apply(plugin = "maven-publish")
    apply(plugin = "com.jfrog.artifactory")
    apply(plugin = "jacoco")

    project.group = group

    configure<LicenseExtension> {
        header = rootProject.file("HEADER.txt")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    repositories {
        jcenter()
    }

    configurations.all {
        resolutionStrategy {
            force(Libs.guava)
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }

    if (JavaVersion.current().isJava8Compatible) {
        tasks.withType<Javadoc>().configureEach {
            (options as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
        }
    }

    dependencies {
        "testImplementation"(Libs.junitApi)
        "testImplementation"(Libs.junitEngine)
    }

    addExtraArchiveArtifacts()

    configureMavenPublish()
}

private fun Project.addExtraArchiveArtifacts() {
    val sourcesJar = tasks.register<Jar>("sourcesJar") {
        dependsOn("classes")
        archiveClassifier.set("sources")
        from(project.the<SourceSetContainer>().getByName("main").allSource)
    }
    val javadocJar = tasks.register<Jar>("javadocJar") {
        dependsOn("javadoc")
        archiveClassifier.set("javadoc")
        from(tasks.getByName("javadoc"))
    }
    tasks.named("build") {
        dependsOn(sourcesJar, javadocJar)
    }
}

private fun Project.configureMavenPublish() {
    configure<PublishingExtension> {
        publications {
            register<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()

                artifact(tasks.getByName("sourcesJar"))
                artifact(tasks.getByName("javadocJar"))
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
        publish(delegateClosureOf<PublisherConfig> {
            setContextUrl(project.property("artifactory_contextUrl"))
            setPublishIvy(false)
            setPublishPom(true)
            repository(delegateClosureOf<DoubleDelegateWrapper> {
                invokeMethod("setRepoKey", when {
                    "SNAPSHOT" in project.version.toString() -> "libs-snapshot-local"
                    else -> "libs-release-local"
                })
                invokeMethod("setUsername", project.property("artifactory_user"))
                invokeMethod("setPassword", project.property("artifactory_password"))
            })
            defaults(delegateClosureOf<ArtifactoryTask> {
                publications("maven")
                setPublishArtifacts(true)
            })
        })
    }
    tasks.named<ArtifactoryTask>("artifactoryPublish") {
        skip = true
    }
}

fun Project.applyCoreApConfig() {
    applyCommonConfig(group = rootProject.group.toString() + ".core-ap")
}
