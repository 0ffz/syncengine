rootProject.name = "syncengine"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://packages.jetbrains.team/maven/p/amper/amper")
        maven("https://www.jetbrains.com/intellij-repository/releases")
        maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        mavenLocal()
    }
    includeBuild("../sqlite-kt")
}

includeBuild("../sqlite-kt")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":core", ":client", ":server", ":json-actions", ":tests")

//includeBuild("../sqlite-kt")