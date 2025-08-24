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
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

//plugins {
//    id("org.jetbrains.amper.settings.plugin").version("0.6.0")
//}

//include(":android-app")
//include(":ios-app")
//include(":jvm-app")
//include(":shared")
//include(":common")
include(":server")
//include(":example:client")
//include(":database")
include(":core", ":client")
