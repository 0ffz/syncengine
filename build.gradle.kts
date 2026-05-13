import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
//    alias(libs.plugins.androidApplication) apply false
    alias(miaLibs.plugins.kotlin.multiplatform) apply false
    alias(miaLibs.plugins.kotlin.jvm) apply false
    //TODO add sqlite codegen plugin to catalog
//    alias(miaLibs.plugins.sqlite.codegen) apply false
    id("me.dvyy.sqlite.codegen") version "0.1.0-dev.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
    }

    project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
        val kotlinExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        kotlinExtension.compilerOptions {
            freeCompilerArgs.add("-Xcontext-parameters")
            optIn.add("kotlin.uuid.ExperimentalUuidApi")
        }
    }
}
