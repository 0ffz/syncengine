import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    id("me.dvyy.sqlite.codegen") version "0.0.3-alpha.2" apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
    }

    project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
        val kotlinExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        kotlinExtension.compilerOptions {
            freeCompilerArgs.add("-Xcontext-parameters")
            optIn.add("kotlin.uuid.ExperimentalUuidApi")
        }
    }
}
