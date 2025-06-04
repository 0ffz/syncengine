plugins {
    kotlin("jvm")
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":server"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
}
