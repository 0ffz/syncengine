import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    alias(miaLibs.plugins.kotlin.multiplatform) apply false
    alias(miaLibs.plugins.kotlin.jvm) apply false
    alias(miaLibs.plugins.sqlite.kt.codegen) apply false
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
