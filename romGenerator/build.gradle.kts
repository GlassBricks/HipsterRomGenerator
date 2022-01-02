plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":knbt"))
    testImplementation(kotlin("script-runtime"))
}
