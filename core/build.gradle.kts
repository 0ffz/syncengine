plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    `maven-publish`
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                dependencies {
                    api(libs.sqlite.kt)
                    implementation(libs.kotlinx.serialization.json)
                    implementation(libs.kotlinx.serialization.cbor)
                    implementation(libs.kotlinx.coroutines.core)
                    implementation(libs.kermit)
                }
            }
        }
    }
}
