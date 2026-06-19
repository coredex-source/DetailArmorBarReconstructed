plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "26.2-fabric"

tasks.register<Delete>("clean") {
    group = "build"
    delete(layout.projectDirectory.dir("bin"))
}

tasks.register("chiseledBuild") {
    group = "build"
    dependsOn(":26.2-fabric:build", ":26.2-neoforge:build")
}

tasks.register("binJar") {
    group = "build"
    dependsOn(":26.2-fabric:binJar", ":26.2-neoforge:binJar")
}
