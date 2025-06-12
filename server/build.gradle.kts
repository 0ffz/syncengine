plugins {
    alias(libs.plugins.kotlin.jvm)
    kotlin("plugin.serialization") version "2.1.20"
}
tasks {
    test {
        useJUnit()
    }
}
dependencies {
    implementation(project(":common"))
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
    implementation(libs.ktor.server.netty)
    implementation("io.ktor:ktor-server-core:3.1.2")
    implementation("io.ktor:ktor-server-websockets:3.1.2")
    implementation("io.ktor:ktor-server-core:3.1.2")
    implementation("io.ktor:ktor-serialization-kotlinx-protobuf:3.1.2")

//    implementation(libs.exposed.dao)
//    implementation(libs.exposed.core)
//    implementation(libs.exposed.r2dbc)
//    implementation(libs.exposed.jdbc)
//    implementation(libs.exposed.json)
//    implementation("org.xerial:sqlite-jdbc:3.49.1.0")
    implementation(libs.logback)
    implementation("io.ktor:ktor-server-content-negotiation:3.1.2")
    implementation("io.ktor:ktor-serialization-jackson:3.1.2")
    implementation("io.ktor:ktor-server-content-negotiation:3.1.2")

    // tests
    testImplementation(kotlin("test"))
}
