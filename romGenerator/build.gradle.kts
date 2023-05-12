import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}


sourceSets {
    test {
        java.setSrcDirs(listOf("scripts"))
        resources.setSrcDirs(listOf("scripts"))
    }
    main {
        java.setSrcDirs(listOf("src"))
    }
}

dependencies {
    implementation(project(":knbt"))
    testImplementation(kotlin("script-runtime"))
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    languageVersion = "1.9"
}