package me.glassbricks.sequencegen

import java.io.File

val IntRange.size get() = endInclusive - first + 1

inline fun <T : Any> Array<T?>.getOrPut(index: Int, defaultValue: () -> T): T {
    return this[index] ?: defaultValue().also { this[index] = it }
}


fun File.mkPDirs(){
   parentFile.mkdirs()
}

