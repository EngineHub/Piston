plugins {
    id("net.researchgate.release") version "3.0.2"
    id("org.enginehub.codecov")
    jacoco
}

configureArtifactory()

repositories {
    mavenCentral()
}

release {
    tagTemplate = "v\${version}"
    buildTasks = listOf<String>()
}

val totalReport = tasks.register<JacocoReport>("jacocoTotalReport") {
    subprojects.forEach { proj ->
        proj.plugins.withId("java") {
            executionData(
                fileTree(proj.layout.buildDirectory.get().asFile.absolutePath).include("**/jacoco/*.exec")
            )
            sourceSets(proj.the<JavaPluginExtension>().sourceSets["main"])
            reports {
                xml.required = true
                xml.outputLocation = rootProject.layout.buildDirectory.file("reports/jacoco/report.xml")
                html.required = true
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
