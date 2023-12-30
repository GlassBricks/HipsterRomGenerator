package other

import me.glassbricks.analyze.countItems
import me.glassbricks.analyze.toChestsSchem
import me.glassbricks.knbt.Nbt
import me.glassbricks.schem.SchemFile
import me.glassbricks.schem.writeTo
import java.io.File
import java.util.zip.GZIPInputStream

fun main() {
    val srcFile = "input.schem"
    val stream = GZIPInputStream(File("romGenerator", srcFile).inputStream())
    val srcSchem = Nbt {
        ignoreUnknownKeys = true
    }.decodeFromStream<SchemFile>(stream)

    val itemCount = countItems(srcSchem)
    print(itemCount.items)
    val itemCountSchem = toChestsSchem(itemCount)

    itemCountSchem.writeTo(File("romGenerator/9x9fs out", "chests.schem"))
}
