plugins {
    alias(miaLibs.plugins.kotlin.multiplatform)
    alias(miaLibs.plugins.kotlinx.serialization)
    alias(miaLibs.plugins.mia.publication)
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
                    implementation(miaLibs.kermit)
                    implementation(miaLibs.kotlinx.serialization.json)
                    implementation(miaLibs.kotlinx.serialization.cbor)
                    implementation(miaLibs.kotlinx.serialization.protobuf)
                    implementation(miaLibs.kotlinx.coroutines.test)
                    implementation(miaLibs.kotest.assertions)
                    implementation(kotlin("test"))
                }
            }
        }
    }
}
