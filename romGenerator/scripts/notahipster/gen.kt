package notahipster

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.schem.*
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


class GenSchem : StringSpec({
    beforeSpec {
        File("9x9fs out").mkdirs()
        // clear out dir
        File("9x9fs out").listFiles()?.forEach { it.delete() }
    }
    afterSpec {
        val version = getVersion()
        incVersion()
        // zip all files in out dir
        val files = File("9x9fs out/").listFiles() ?: error("no files")
        val zipFile = File("9x9fs out/9x9fs-all-v$version.zip")
        zipFile.delete()
        zipFile.createNewFile()
        ZipOutputStream(FileOutputStream(zipFile)).use {
            for (file in files) {
                if (!file.name.endsWith(".schem")) continue
                println("writing ${file.name}... ")
                it.putNextEntry(ZipEntry(file.name))
                it.write(file.readBytes())
                it.closeEntry()
            }
        }
        println("wrote to ${zipFile.absolutePath}")
    }
    val fns = listOf<SeqGen.() -> Unit>(
        SeqGen::row1234,
        SeqGen::row5,
        SeqGen::row6,
        SeqGen::row7,
        SeqGen::row8,
        SeqGen::row9,
    )
    val waitMoves = listOf(13, 14, 15, 17, 18, 19, 20)
    "generate cumulative" {
        val gen = SeqGen(
            pinkTapeLevel = -8 + 18,
            waitMoves = waitMoves,
        )
        for (i in 0..5) {
            val fn = fns[i]
            fn(gen)

            val newFileName = "upTo-row${i + 4}.schem"
            val schemFile = waitOptimizedRecordRomSchem2(gen.moves.let(::dispersePinks))
            writeSchematic(schemFile, "9x9fs out/$newFileName")
        }
    }
    "generate each" {
        for (i in 0..5) {
            val fn = fns[i]
            val gen = SeqGen(
                pinkTapeLevel = 0,
                waitMoves = waitMoves,
            )
            fn(gen)

            val newFileName = "row${i + 4}.schem"
            val schemFile = waitOptimizedRecordRomSchem2(gen.moves.let(::dispersePinks))
            writeSchematic(schemFile, "9x9fs out/$newFileName")
        }
    }
})

private val versionFile = File("9x9fs moves/cur_version")

private fun getVersion(): Int {
    if (!versionFile.exists()) {
        versionFile.writeText("0")
    }
    return versionFile.readText().trim().toInt()
}

private fun incVersion() {
    val version = getVersion()
    versionFile.writeText((version + 1).toString())
}

private const val shouldDispersePinks = false
fun dispersePinks(seq: List<Move>): List<Move> {
    if (!shouldDispersePinks) return seq
    var inPurple = false
    val lastSeg = mutableListOf<Move>()
    val result = mutableListOf<Move>()
    var lastSegNumPinks = 0
    for (move in seq) when (move) {
        Move.purple -> {
            inPurple = !inPurple
            if (inPurple) {
                // disperse pinks evenly in lastSeg
                val finalSegSize = lastSeg.size + lastSegNumPinks
                val numPinks = lastSegNumPinks

                if (numPinks > 0) {
                    val step = finalSegSize / numPinks
                    for (i in 0 until numPinks) {
                        val index = ((i + 0.5) * step).toInt()
                        lastSeg.add(index, Move.pink)
                    }
                }
            }
            lastSeg += move
            result += lastSeg
            lastSeg.clear()
            lastSegNumPinks = 0
        }

        Move.pink -> {
            lastSegNumPinks++
        }

        else -> {
            lastSeg += move
        }
    }
    result += lastSeg
    while (lastSegNumPinks-- > 0) result += Move.pink

    return result
}


// boundary of boxes count as 2 waiting moves
// BUT waits only happen in between purples
// if purples span a chest, can remove up to 2 wait moves ANYWHERE in between purples
fun toWaitOptimizedSSBoxes2(moves: List<Move>): List<List<Move>> = buildList {
    var curBox = mutableListOf<Move>()
    var inPurple = false
    var curPurpWaitBegin: Int? = null
    var curPurpCanOpt = 2
    var i = 0
    var nRemoved = 0
    while (i < moves.size) {
        val move = moves[i++]
        curBox.add(move)
        if (move == Move.purple) {
            inPurple = !inPurple
            if (inPurple) {
                curPurpCanOpt = 2
                curPurpWaitBegin = null
            }
        } else if (move == Move.wait) {
            curPurpWaitBegin = curPurpWaitBegin ?: curBox.lastIndex
        }
        if (curBox.size == CHEST_MAX) {
            if (inPurple) {
                if (curPurpCanOpt > 0 && curPurpWaitBegin != null && curBox[curPurpWaitBegin] == Move.wait &&
                    moves.getOrNull(i) != Move.purple
                ) {
                    curBox.removeAt(curPurpWaitBegin)
                    curPurpCanOpt--
                    nRemoved++
                    continue
                }
                while (curPurpCanOpt > 0 && moves.getOrNull(i) == Move.wait) {
                    i++
                    curPurpCanOpt--
                    nRemoved++
                }
            }
            add(curBox)
            curBox = mutableListOf()
            curPurpWaitBegin = null
        }
    }

    while (curBox.size < CHEST_MAX) {
        curBox.add(Move.wait)
    }
    add(curBox)

    println("removed $nRemoved wait moves")
}

fun waitOptimizedRecordRomSchem2(
    moves: List<Move>,
): SchemFile {
    val boxes = toWaitOptimizedSSBoxes2(moves)
    val rom = SSBoxes(
        boxes.map { SSBox(encodeToSignalStrengths(it, encoding)) }
    )
    return toRecordChestRomSchem(rom)
}
