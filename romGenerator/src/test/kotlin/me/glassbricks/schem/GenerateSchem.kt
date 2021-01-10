package me.glassbricks.schem

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.glassbricks.knbt.Nbt
import me.glassbricks.rom.toRom
import me.glassbricks.sequence.HipsterSequences
import me.glassbricks.sequence.PistonSequence
import java.io.File
import java.util.zip.GZIPOutputStream

suspend fun main() = coroutineScope {
    val dest = "~/.local/share/multimc/instances/1.16.4/.minecraft/config/worldedit/schematics"

    HipsterSequences.groups.forEach { (name, group) ->
        for (i in 6..11) {
            writeSchem(group[i], "$dest/$name-$i.schem")
        }
        if (name == "extend") {
            for (i in -7..-6) {
                writeSchem(group[i], "$dest/$name-$i.schem")
            }
        }
    }
}

private fun CoroutineScope.writeSchem(
    sequence: PistonSequence,
    fileName: String,
) = launch(Dispatchers.IO) {
    val nbt = Nbt { encodeDefaults = true }
    val file = File(fileName)
    file.parentFile.mkdirs()
    val model = sequence.toRom().toSchem2()
    val stream = file.outputStream().buffered().let(::GZIPOutputStream)
    nbt.encodeToStream(stream, SchemFile.serializer(), model)
}