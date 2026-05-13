plugins {
    alias(miaLibs.plugins.kotlin.multiplatform)
    alias(miaLibs.plugins.kotlinx.serialization)
    alias(miaLibs.plugins.mia.publication)
    id("me.dvyy.sqlite.codegen")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
    jvm()
    jvmToolchain(21)

    sourceSets {
        commonMain {
            dependencies {
                dependencies {
                    api(projects.core)
                    implementation(projects.jsonActions)
                    implementation(miaLibs.kotlinx.serialization.json)
                    implementation(miaLibs.kotlinx.serialization.protobuf)
                    implementation(miaLibs.kotlinx.coroutines)
                    implementation(miaLibs.kermit)
                    implementation(libs.caffeine)
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
