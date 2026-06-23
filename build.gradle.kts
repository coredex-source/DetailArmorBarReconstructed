plugins {
    id("dev.isxander.modstitch.base")
    id("maven-publish")
}

fun stringProp(name: String): String = property(name) as String
fun optionalProp(name: String, consumer: (String) -> Unit) {
    (findProperty(name) as? String)?.let(consumer)
}

val targetMinecraftVersion = stringProp("deps.minecraft")
val loader = project.name.substringAfterLast("-")

version = "${stringProp("mod.version")}-$targetMinecraftVersion-$loader"
base.archivesName = stringProp("archives.name")

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com/") {
        name = "Terraformers"
    }
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
    }
    maven("https://maven.neoforged.net/releases/") {
        name = "NeoForge"
    }
    maven("https://maven.fabricmc.net/") {
        name = "Fabric"
    }
}

modstitch {
    minecraftVersion = targetMinecraftVersion
    javaVersion = 25

    metadata {
        modId = stringProp("mod.id")
        modName = stringProp("mod.name")
        modVersion = project.version.toString()
        modGroup = stringProp("mod.group")
        modAuthor = stringProp("mod.author")
        modDescription = stringProp("mod.description")
        modLicense = stringProp("mod.license")

        replacementProperties.put("mod_sources", stringProp("mod.sources"))
        replacementProperties.put("mod_icon", stringProp("mod.icon"))
        replacementProperties.put("minecraft_dependency", stringProp("mod.minecraft_dependency"))
        replacementProperties.put("neoforge_minecraft_dependency", stringProp("mod.neoforge_minecraft_dependency"))
    }

    loom {
        fabricLoaderVersion = stringProp("deps.fabric_loader")
    }

    moddevgradle {
        optionalProp("deps.neoforge") {
            neoForgeVersion = it
        }

        defaultRuns(server = false)
    }

    mixin {
        addMixinsToModManifest = false
        configs.register("detailab")
        configs.register("detailab.overflowingbars")
    }
}

stonecutter {
    constants {
        put("fabric", loader.equals("fabric"))
        put("neoforge", loader.equals("neoforge"))
        put("minecraft_26_1", targetMinecraftVersion == "26.1.2")
    }
}

dependencies {
    modstitchModImplementation("maven.modrinth:eclipseui:${stringProp("deps.eclipseui")}")

    modstitch.loom {
        modstitchModImplementation("net.fabricmc.fabric-api:fabric-api:${stringProp("deps.fabric_api")}")
        modstitchModImplementation("com.terraformersmc:modmenu:${stringProp("deps.modmenu")}")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 25
}

java {
    withSourcesJar()
}

tasks.withType<ProcessResources>().configureEach {
    inputs.property("version", project.version)
}

tasks.jar {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

tasks.register<Copy>("binJar") {
    group = "build"
    dependsOn("build", "sourcesJar")
    from(modstitch.finalJarTask.map { it.archiveFile })
    from(tasks.named<Jar>("sourcesJar").map { it.archiveFile })
    into(rootProject.layout.projectDirectory.dir("bin"))
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    dependsOn("binJar")
    from(tasks.named<Copy>("binJar"))
    into(rootProject.layout.buildDirectory.dir("libs/${stringProp("mod.version")}"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.jar)
            artifact(tasks.named("sourcesJar"))
        }
    }
}
