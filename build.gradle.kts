plugins {
    id("net.researchgate.release") version "2.8.1"
    id("org.enginehub.codecov")
    jacoco
}

configureArtifactory()

repositories {
    jcenter()
}

release {
    tagTemplate = "v\${version}"
    buildTasks = listOf<String>()
}

val totalReport = tasks.register<JacocoReport>("jacocoTotalReport") {
    subprojects.forEach { proj ->
        proj.plugins.withId("java") {
            executionData(
                fileTree(proj.buildDir.absolutePath).include("**/jacoco/*.exec")
            )
            sourceSets(proj.the<JavaPluginConvention>().sourceSets["main"])
            reports {
                xml.isEnabled = true
                xml.destination = rootProject.buildDir.resolve("reports/jacoco/report.xml")
                html.isEnabled = true
            }
            dependsOn(proj.tasks.named("test"))
        }
    }
}
afterEvaluate {
    totalReport.configure {
        classDirectories.setFrom(classDirectories.files.map {
            fileTree(it).apply {
                exclude("**/*AutoValue_*")
            }
        })
    }
}

codecov {
    reportTask.set(totalReport)
}
