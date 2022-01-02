import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        dependencies {
            testImplementation("io.kotest:kotest-runner-junit5:5.0.3")
            testImplementation("io.kotest:kotest-assertions-core:5.0.3")
            testImplementation("io.kotest:kotest-property:5.0.3")
        }
        tasks.test {
            useJUnitPlatform()
        }
        tasks.withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
            }
        }
    }
}
