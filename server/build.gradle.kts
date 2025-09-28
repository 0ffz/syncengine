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
                    api(projects.core)
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.8.1")
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
//                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.8.1")
//                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.8.1")

                }
            }
        }
    }
    sourceSets.commonMain.dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:null")
    }
}


//dependencies {
//    implementation(project(":common"))
//    implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-serialization-json:0.6.2")
//
//    // Client API
//    implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-client:0.6.2")
//    implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-ktor-client:0.6.2")
//    // Server API
//    implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-server:0.6.2")
//    implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-ktor-server:0.6.2")
//    implementation(libs.kotlinx.rpc.server)
//    implementation(libs.kotlinx.rpc.client)
//    implementation(libs.ktor.server.core)
//    implementation(libs.ktor.client.core)
//    implementation(libs.ktor.client.cio)
//    implementation(libs.exposed.datetime)
//    implementation(libs.exposed.dao)
//    implementation(libs.exposed.core)
//    implementation(libs.exposed.r2dbc)
//    implementation(libs.exposed.jdbc)
//    implementation(libs.exposed.json)
//    implementation("org.xerial:sqlite-jdbc:3.49.1.0")
//    implementation(libs.logback)
//    implementation("io.ktor:ktor-server-content-negotiation:3.1.2")
//    implementation("io.ktor:ktor-serialization-jackson:3.1.2")
//    implementation("io.ktor:ktor-server-content-negotiation:3.1.2")
//
//    // tests
//    testImplementation(kotlin("test"))
//}
