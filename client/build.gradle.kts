plugins {
    alias(miaLibs.plugins.kotlin.multiplatform)
    alias(miaLibs.plugins.kotlinx.serialization)
    alias(miaLibs.plugins.mia.publication)
    id("me.dvyy.sqlite.codegen")
}

kotlin {
    jvm()
    jvmToolchain(21)

    compilerOptions {
//        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }
    sourceSets {
        commonMain {
            dependencies {
                dependencies {
                    implementation(projects.core)
                    implementation(miaLibs.kotlinx.serialization.json)
                    implementation(miaLibs.kotlinx.serialization.protobuf)
                    implementation(miaLibs.kotlinx.coroutines)
                    implementation(miaLibs.kermit)
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
