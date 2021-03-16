package me.glassbricks.schem

import me.glassbricks.hipster.HipsterSequences
import me.glassbricks.knbt.Nbt
import me.glassbricks.rom.toRom
import me.glassbricks.sequence.MoveSequence
import me.glassbricks.sequence.SequenceItem
import java.io.File
import java.util.zip.GZIPOutputStream

fun main() {
    val home = System.getProperty("user.home")
    val dest = "$home/.local/share/multimc/instances/1.16.4/.minecraft/config/worldedit/schematics"

    HipsterSequences.groups.forEach { (name, group) ->
        for (i in 6..11) {
            writeSchem(group[i], "$dest/$name-$i.schem", HipsterSequences.encoding)
        }
        if (name == "extend") {
            for (i in -7..-6) {
                writeSchem(group[i], "$dest/$name-$i.schem", HipsterSequences.encoding)
            }
        }
    }
}


fun <T : SequenceItem> writeSchem(
    sequence: MoveSequence<T>,
    fileName: String,
    encoding: Map<T, Int>,
) {
    val nbt = Nbt { encodeDefaults = true }
    val file = File(fileName)
    file.parentFile.mkdirs()
    val model = sequence.toRom(encoding).toSchem2()
    val stream = file.outputStream().buffered().let(::GZIPOutputStream)
    nbt.encodeToStream(stream, SchemFile.serializer(), model)
    stream.close()
    println("wrote ${sequence.name} to ${file.absolutePath}")
}