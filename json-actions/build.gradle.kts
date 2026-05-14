plugins {
    alias(miaLibs.plugins.kotlin.multiplatform)
    alias(miaLibs.plugins.kotlinx.serialization)
    alias(miaLibs.plugins.mia.publication)
    alias(miaLibs.plugins.sqlite.kt.codegen)
}

kotlin {
    jvm()
    jvmToolchain(21)

    sourceSets {
        commonMain {
            dependencies {
                dependencies {
                    implementation(kotlin("test"))
                    implementation(projects.core)
                    implementation(miaLibs.sqlite.kt)
                    implementation(miaLibs.kotlinx.serialization.json)
                    implementation(miaLibs.kotlinx.serialization.cbor)
                    implementation(miaLibs.kotlinx.coroutines)
                    implementation(miaLibs.kermit)
                }
            }
        }
    }
}
