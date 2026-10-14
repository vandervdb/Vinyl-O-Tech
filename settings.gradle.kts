import java.util.Properties

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal() // dev local
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "vinyl-otech"

include(":app")
include(":spotify-lib")
include(":core:domain")
include(":core:dto")
include(":core:logger")
include(":core:security")
include(":core:ui")
include(":fake")
include(":konsist")

// Identifiants Spotify, lus depuis local.properties (jamais commité).
run {
    val properties = Properties()
    val propertiesFile = file("local.properties")

    if (propertiesFile.exists()) {
        logger.lifecycle("@@ LOADING PROPERTIES FROM local.properties")
        properties.load(propertiesFile.inputStream())

        val clientId = properties["CLIENT_ID"] ?: error("CLIENT_ID missing")
        val clientSecret = properties["CLIENT_SECRET"] ?: error("CLIENT_SECRET missing")

        gradle.extra.also {
            it.set("spotifyClientId", clientId.toString())
            it.set("spotifyClientSecret", clientSecret.toString())
        }
    } else {
        logger.error("No local.properties file found! It must contain CLIENT_ID and CLIENT_SECRET.")
    }
}
