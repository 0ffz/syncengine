plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    `maven-publish`
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
                    implementation(projects.core)
//                    implementation("io.ktor:ktor-client-websockets:3.1.2")
//                    implementation("io.ktor:ktor-client-content-negotiation:3.1.2")
//                    implementation("io.ktor:ktor-serialization-kotlinx-protobuf:3.1.2")
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.8.1")
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                }
            }
        }
    }
    sourceSets.commonMain.dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:null")
    }
}
