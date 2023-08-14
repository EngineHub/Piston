plugins {
    id("net.researchgate.release") version "2.8.1"
    id("org.enginehub.codecov")
    jacoco
}

configureArtifactory()

release {
    tagTemplate = "v\${version}"
    buildTasks = listOf<String>()
}

val totalReport = tasks.register<JacocoReport>("jacocoTotalReport") {
    for (proj in subprojects) {
        proj.apply(plugin = "jacoco")
        proj.plugins.withId("java") {
            executionData(
                fileTree(proj.buildDir.absolutePath).include("**/jacoco/*.exec")
            )
            sourceSets(proj.the<JavaPluginConvention>().sourceSets["main"])
            reports {
                xml.required.set(true)
                xml.outputLocation.set(rootProject.buildDir.resolve("reports/jacoco/report.xml"))
                html.required.set(true)
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
