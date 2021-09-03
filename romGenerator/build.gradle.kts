plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":knbt"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("script-runtime"))
}