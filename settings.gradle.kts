rootProject.name = "syncengine"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
//        maven("https://www.jetbrains.com/intellij-repository/releases")
//        maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
//        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
//        mavenLocal()
    }
//    includeBuild("../sqlite-kt")
}

//includeBuild("../sqlite-kt")


dependencyResolutionManagement {
    val miaLibs: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
    }

    versionCatalogs {
        create("miaLibs").from("com.mineinabyss:catalog:$miaLibs")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":core", ":client", ":server", ":json-actions", ":tests")

//includeBuild("../sqlite-kt")
