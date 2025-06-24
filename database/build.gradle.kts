plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    `maven-publish`
//    id("com.google.devtools.ksp") version "2.2.0-RC3-2.0.2"
}

group = "me.dvyy"
version = "0.0.1"

repositories {
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
    jvm()

    sourceSets {
        commonMain {
            dependencies {

                dependencies {
//    val room_version = "2.7.2"
//    implementation("androidx.room:room-runtime:$room_version")
                    api("androidx.sqlite:sqlite-bundled:2.5.2")
//    ksp("androidx.room:room-compiler:$room_version")

                    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
                    // See Add the KSP plugin to your project
//    implementation("app.cash.sqldelight:runtime:2.2.0-SNAPSHOT")
//    implementation("app.cash.sqldelight:sqlite-driver:2.2.0-SNAPSHOT")
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
//    implementation("androidx.sqlite:sqlite-ktx:2.5.2")
                }
            }
        }
    }
}
