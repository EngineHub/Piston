import net.minecrell.gradle.licenser.LicenseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.withType

fun Project.applyCommonConfig() {
    apply(plugin = "java-library")
    apply(plugin = "net.minecrell.licenser")
    apply(plugin = "net.ltgt.apt-idea")

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
}
