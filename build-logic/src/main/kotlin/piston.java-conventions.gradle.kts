plugins {
    id("org.enginehub.crankcase.checkstyle")
    id("org.enginehub.crankcase.java-library")
    id("org.enginehub.crankcase.licensing")
    id("org.enginehub.crankcase.publishing")
    jacoco
}

crankcaseJava {
    javaRelease = 25
    // AutoValue & JSpecify annotations are never claimed by a processor
    disabledLints.add("processing")
    // exceptions are Serializable via Throwable, but we never serialize them
    disabledLints.add("serial")
}

tasks.named<Copy>("processTestResources") {
    from(isolated.rootProject.projectDirectory.dir("common-test-resources"))
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

rootProject.tasks.named("afterReleaseBuild").configure {
    dependsOn(tasks.named("build"))
}
