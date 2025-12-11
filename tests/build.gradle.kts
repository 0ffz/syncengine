plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    `maven-publish`
}

kotlin {
    jvm()

    sourceSets {
        jvmTest {
            dependencies {
                dependencies {
                    implementation(projects.core)
                    implementation(projects.server)
                    implementation(projects.client)
                    implementation(projects.jsonActions)
                    implementation(libs.kotlinx.serialization.json)
                    implementation(libs.kotlinx.serialization.cbor)
                    implementation(libs.kotlinx.coroutines.test)
                    implementation(libs.kermit)
//                    implementation(libs.kotest.assertions)
//                    implementation(libs.kotest.property)
                    implementation(libs.kotlinx.coroutines.test)
                    implementation(kotlin("test"))
                }
            }
        }
    }
}
