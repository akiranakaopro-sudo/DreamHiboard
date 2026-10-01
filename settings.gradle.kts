val offlineMavenRepo = (providers.gradleProperty("offlineMavenRepo")
    .orElse(providers.environmentVariable("OFFLINE_MAVEN_REPO"))
    .orElse("D:/Android/maven_repo_hiboard"))
    .get()

// Prefer the file mirror only when explicitly requested. By default online repos
// come first so missing JARs (e.g. incomplete local POMs) download automatically.
val preferOfflineMaven = providers.gradleProperty("preferOfflineMaven")
    .orElse(providers.environmentVariable("PREFER_OFFLINE_MAVEN"))
    .map { it.equals("true", ignoreCase = true) }
    .orElse(false)
    .get()

pluginManagement {
    val offlineMavenRepo = (providers.gradleProperty("offlineMavenRepo")
        .orElse(providers.environmentVariable("OFFLINE_MAVEN_REPO"))
        .orElse("D:/Android/maven_repo_hiboard"))
        .get()
    val preferOfflineMaven = providers.gradleProperty("preferOfflineMaven")
        .orElse(providers.environmentVariable("PREFER_OFFLINE_MAVEN"))
        .map { it.equals("true", ignoreCase = true) }
        .orElse(false)
        .get()
    repositories {
        if (preferOfflineMaven) {
            maven { url = uri(offlineMavenRepo) }
        }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        if (!preferOfflineMaven) {
            maven { url = uri(offlineMavenRepo) }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        if (preferOfflineMaven) {
            maven { url = uri(offlineMavenRepo) }
        }
        google()
        mavenCentral()
        if (!preferOfflineMaven) {
            maven { url = uri(offlineMavenRepo) }
        }
    }
}

rootProject.name = "Hiboard"
include(":app")
