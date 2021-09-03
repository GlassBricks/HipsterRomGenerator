package me.glassbricks.schem

import me.glassbricks.knbt.Nbt
import java.io.File
import java.util.zip.GZIPOutputStream


fun SchemFile.writeTo(file: File) {
    val nbt = Nbt { encodeDefaults = true }
    file.parentFile.mkdirs()
    val stream = file.outputStream().buffered().let(::GZIPOutputStream)
    nbt.encodeToStream(stream, SchemFile.serializer(), this)
    stream.close()
    println("wrote to ${file.absolutePath}")
}
