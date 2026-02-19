plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    `maven-publish`
    id("me.dvyy.sqlite.codegen")
}

kotlin {
    jvm()

    compilerOptions {
//        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }
    sourceSets {
        commonMain {
            dependencies {
                dependencies {
                    implementation(projects.core)
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
    register("client") {
        packageName = "me.dvyy.syncengine.client.schema"
    }
}