import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        dependencies {
            testImplementation("io.kotest:kotest-runner-junit5:5.5.5")
            testImplementation("io.kotest:kotest-assertions-core:5.5.5")
            testImplementation("io.kotest:kotest-property:5.5.5")
        }
        tasks.test {
            useJUnitPlatform()
        }
        tasks.withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
            }
        }
    }
}
