plugins {
    alias(miaLibs.plugins.kotlin.multiplatform)
    alias(miaLibs.plugins.kotlinx.serialization)
    alias(miaLibs.plugins.mia.publication)
}

kotlin {
    jvm()
    jvmToolchain(21)

    sourceSets {
        commonMain {
            dependencies {
                dependencies {
                    api(miaLibs.sqlite.kt)
                    implementation(miaLibs.kotlinx.serialization.json)
                    implementation(miaLibs.kotlinx.serialization.cbor)
                    implementation(miaLibs.kotlinx.coroutines)
                    implementation(miaLibs.kermit)
                }
            }
        }
    }
}
