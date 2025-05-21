plugins {
    alias(libs.plugins.kotlin.jvm)
    kotlin("plugin.serialization") version "2.1.20"
//    id("org.jetbrains.kotlinx.rpc.plugin") version "0.6.2"
}
tasks {
    test {
        useJUnit()
    }
}
dependencies {
//    implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-serialization-json:0.6.2")
//    implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-serialization-cbor:0.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.8.1")

    // Client API
//    implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-client:0.6.2")
//    implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-ktor-client:0.6.2")
    // Server API
//    implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-server:0.6.2")
//    implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-ktor-server:0.6.2")
//    implementation(libs.kotlinx.rpc.server)
//    implementation(libs.kotlinx.rpc.client)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation("io.ktor:ktor-server-core:3.1.2")
    implementation("io.ktor:ktor-server-websockets:3.1.2")
    implementation("io.ktor:ktor-server-core:3.1.2")

    implementation(libs.exposed.dao)
    implementation(libs.exposed.core)
    implementation(libs.exposed.r2dbc)
    implementation(libs.exposed.datetime)
    implementation(libs.r2dbc.h2)
    implementation(libs.r2dbc.postgresql)
    implementation("com.h2database:h2:2.2.224")
//    implementation(libs.exposed.jdbc)
//    implementation(libs.exposed.json)
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")

    // tests
    testImplementation(kotlin("test"))
}
