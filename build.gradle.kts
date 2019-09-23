plugins {
    id("net.researchgate.release") version "2.8.0"
    jacoco
}

configureArtifactory()

repositories {
    jcenter()
}

release {
    tagTemplate = "v\${version}"
    buildTasks = project.everyProject
            .filter { it.tasks.names.contains("build") }
            .map { it.tasks.named("build") }
            .toList()
}

val totalReport = tasks.register<JacocoReport>("jacocoTotalReport") {
    subprojects.forEach { proj ->
        proj.plugins.withId("java") {
            executionData(
                fileTree(proj.buildDir.absolutePath).include("**/jacoco/*.exec")
            )
            println(proj)
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
