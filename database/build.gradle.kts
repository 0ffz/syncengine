plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    id("app.cash.sqldelight") version "2.2.0-SNAPSHOT"
//    id("com.google.devtools.ksp") version "2.2.0-RC3-2.0.2"
}

repositories {
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}
dependencies {
//    val room_version = "2.7.2"
//    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.sqlite:sqlite-bundled:2.5.2")
//    ksp("androidx.room:room-compiler:$room_version")

    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See Add the KSP plugin to your project
//    implementation("app.cash.sqldelight:runtime:2.2.0-SNAPSHOT")
//    implementation("app.cash.sqldelight:sqlite-driver:2.2.0-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
//    implementation("androidx.sqlite:sqlite-ktx:2.5.2")
    implementation("com.eygraber:sqldelight-androidx-driver:0.0.13")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}


sqldelight {
    databases {
        create("Database") {
            packageName.set("me.dvyy.syncengine.db")
        }
    }
}
