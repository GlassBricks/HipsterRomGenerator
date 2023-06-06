import io.kotest.core.spec.style.StringSpec
import me.glassbricks.knbt.Nbt
import java.io.File
import java.util.zip.GZIPInputStream

class ReadTemplateSchemFiles : StringSpec({
    "read template 1" {
        readFile("templates/rom-template.schem")
    }
    "read template 2" {
        readFile("templates/rom-template-2.schem")
    }
    "read carts" {
        readFile("templates/carts.schem")
    }
    "read 6x6 schem" {
        readFile("templates/6x6.schem")
    }
    "read chungus" {
        readFile("templates/chungus-rom-template.schem", true)
    }
})

fun readFile(name: String, writeFile: Boolean = false) {
    val nbt = Nbt { ignoreUnknownKeys = true }
    val file = Thread.currentThread().contextClassLoader.getResourceAsStream(name) ?: error("File not found")
    val stream = GZIPInputStream(file.buffered())
    val tag = nbt.decodeToTagFromStream(stream)
    println(tag)
    if(writeFile) {
        val out = File("$name.txt")
        out.writeText(tag.toString())
    }
}
