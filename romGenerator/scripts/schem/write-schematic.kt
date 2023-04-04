package schem

import me.glassbricks.knbt.Nbt
import me.glassbricks.schem.SchemFile
import java.io.File
import java.util.zip.GZIPOutputStream


fun writeSchematic(schematic: SchemFile, file: File) {
    val nbt = Nbt { encodeDefaults = true }
    file.absoluteFile.parentFile.mkdirs()
    val stream = file.outputStream().buffered().let(::GZIPOutputStream)
    nbt.encodeToStream(stream, SchemFile.serializer(), schematic)
    stream.close()
    println("wrote to ${file.absolutePath}")
}