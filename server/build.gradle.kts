plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    `maven-publish`
    id("me.dvyy.sqlite.codegen")
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
                    api(projects.core)
                    implementation(libs.kotlinx.serialization.json)
                    implementation(libs.kotlinx.serialization.protobuf)
                    implementation(libs.kotlinx.coroutines.core)
                    implementation(libs.kermit)
                }
            }
        }
    }
}

sqliteKt {
    register("server") {
        packageName = "me.dvyy.syncengine.server.schema"
    }
}