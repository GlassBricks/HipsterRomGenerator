package schem

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.knbt.Nbt
import java.util.zip.GZIPInputStream

class ReadTemplateSchemFiles : StringSpec({
    "read template 1" {
        readFile("rom-template.schem")
    }
    "read template 2" {
        readFile("rom-template-2.schem")
    }
    "read carts" {
        readFile("carts.schem")
    }
    "read 6x6 schem" {
        readFile("6x6.schem")
    }
})

fun readFile(name: String) {
    val nbt = Nbt { ignoreUnknownKeys = true }
    val file = Thread.currentThread().contextClassLoader.getResourceAsStream(name) ?: error("File not found")
    val stream = GZIPInputStream(file.buffered())
    val tag = nbt.decodeToTagFromStream(stream)
    println(tag)
}
