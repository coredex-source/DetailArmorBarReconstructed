pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()

        maven("https://maven.isxander.dev/releases/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "dev.isxander.modstitch.base" && requested.version == null) {
                useVersion(settings.extra["modstitch.version"] as String)
            }
        }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version settings.extra["stonecutter.version"] as String
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        fun mc(mcVersion: String, loaders: Iterable<String>) {
            loaders.forEach { loader -> version("$mcVersion-$loader", mcVersion) }
        }

        mc("26.2", listOf("fabric", "neoforge"))

        vcsVersion = "26.2-fabric"
    }
}

rootProject.name = "DetailArmorBarReconstructed"
