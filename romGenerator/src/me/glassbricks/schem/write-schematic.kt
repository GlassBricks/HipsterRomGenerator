package me.glassbricks.schem

import java.io.File
import java.util.zip.GZIPOutputStream

fun writeSchematic(schematic: SchemFile, file: String) {
    writeSchematic(schematic, File(file))
}

fun writeSchematic(schematic: SchemFile, file: File) {
    file.absoluteFile.parentFile.mkdirs()
    val stream = file.outputStream().buffered().let(::GZIPOutputStream)
    stream.use {
        nbt.encodeToStream(it, SchemFile.serializer(), schematic)
    }
    println("wrote to ${file.absolutePath}")
}
