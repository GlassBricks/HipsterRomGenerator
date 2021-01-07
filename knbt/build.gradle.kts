plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1")
    testImplementation("io.kotest:kotest-runner-junit5:4.3.2")
    testImplementation("io.kotest:kotest-assertions-core:4.3.2")
    testImplementation("io.kotest:kotest-property:4.3.2")
}
