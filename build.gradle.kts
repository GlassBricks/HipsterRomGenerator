import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0-M1"
    kotlin("plugin.serialization") version "1.5.0-M1" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        dependencies {
            testImplementation("io.kotest:kotest-runner-junit5:4.3.2")
            testImplementation("io.kotest:kotest-assertions-core:4.3.2")
            testImplementation("io.kotest:kotest-property:4.3.2")
        }
        tasks.test {
            useJUnitPlatform()
        }
        tasks.withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")

                apiVersion = "1.5"
                languageVersion = "1.5"
                useIR = true
            }
        }
    }
}