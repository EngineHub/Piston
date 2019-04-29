import org.gradle.api.Project

fun everyProject(project: Project): Sequence<Project> {
    return sequence {
        yield(project)
        project.subprojects.forEach {
            yieldAll(everyProject(it))
        }
    }.distinct()
}

val Project.everyProject: Sequence<Project>
    get() = everyProject(this)
