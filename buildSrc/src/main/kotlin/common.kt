import net.minecrell.gradle.licenser.LicenseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.withType

fun Project.applyCommonConfig(
        group: String = rootProject.group.toString()
) {
    apply(plugin = "java-library")
    apply(plugin = "net.minecrell.licenser")
    apply(plugin = "net.ltgt.apt-idea")
    apply(plugin = "maven-publish")

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

    dependencies {
        "testImplementation"(Libs.junitApi)
        "testImplementation"(Libs.junitEngine)
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()

                from(components["java"])
            }
        }
    }
}

fun Project.applyCoreApConfig() {
    applyCommonConfig(group = rootProject.group.toString() + ".core-ap")
}
