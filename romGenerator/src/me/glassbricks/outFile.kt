package me.glassbricks

import java.io.File


private val outDir by lazy {
    var cur = File(".").absoluteFile
    while (true) {
        if (cur.resolve(".git").exists() && cur.resolve("romGenerator").isDirectory) {
            return@lazy cur.resolve("out")
        }
        cur = cur.parentFile ?: break
    }
    error("Could not find out dir")
}

fun File.resolveOut(): File =
    if (isAbsolute) this else outDir.resolve(this).absoluteFile
