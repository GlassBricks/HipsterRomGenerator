package me.glassbricks.schem

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.knbt.Nbt
import java.util.zip.GZIPInputStream

class ReadTemplate : StringSpec({
    val nbt = Nbt { ignoreUnknownKeys = true }
    fun readSchemFile(name: String) {
        val file =
            Thread.currentThread().contextClassLoader.getResourceAsStream(name)
                ?: error("File not found")
        val stream = GZIPInputStream(file.buffered())
        val tag = nbt.decodeToTagFromStream(stream)
        println(tag)
    }

    "template 1" {
        readSchemFile("rom-template.schem")
    }
    "template 2" {
        readSchemFile("rom-template-2.schem")
    }

})

