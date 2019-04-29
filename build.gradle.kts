plugins {
    id("net.researchgate.release") version "2.8.0"
}
configureArtifactory()

release {
    tagTemplate = "v\${version}"
    buildTasks = project.everyProject
            .filter { it.tasks.names.contains("build") }
            .map { it.tasks.named("build") }
            .toList()
}
