package notahipster

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.schem.waitOptimizedRecordRomSchem
import schem.writeSchematic
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


enum class Move(val ss: Int) {
    e(2),
    d(3),
    dpe(4),
    tpe(5),
    g(6),
    lb1t(8),
    lb4t(9),
    f(10),
    worm(11),
    wait(12),
    purple(13),
    pink(15),
}


fun parseLine(line: String): Pair<Move, Int> {
    val m = line.substringBefore('#').trim()
    if (' ' in m) {
        val (a, b) = m.split(' ')
        // either a or b can be number
        return if (a.toIntOrNull() != null) {
            Move.valueOf(b) to a.toInt()
        } else {
            Move.valueOf(a) to b.toInt()
        }
    }
    return Move.valueOf(m) to 1
}

fun parseFile(file: File): List<Move> =
    file.readLines()
        .filter { it.isNotBlank() }
        .map(::parseLine)
        .flatMap { (move, n) ->
            List(n) { move }
        }

val fileNames = listOf(
    "row1-3.txt",
    "row4.txt",
    "row5.txt",
    "row6.txt",
    "row7.txt",
    "row8.txt",
    "row9.txt",
)
val moves = fileNames.map {
    lazy {
        File("9x9fs moves/$it")
            .takeIf { it.exists() }
            ?.let(::parseFile)
    }
}

val encoding = Move.values().associateWith { it.ss }

val version = "8"

class GenSchem : StringSpec({
    beforeSpec {
        File("9x9fs out").mkdirs()
        // clear out dir
        File("9x9fs out").listFiles()?.forEach { it.delete() }
    }
    afterSpec {
        // zip all files in out dir
        val files = File("9x9fs out/").listFiles() ?: error("no files")
        println(files.map { it.name })
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
    "generate cumulative" {
        val movesCumulative = mutableListOf<Move>()
        for ((i, fileName) in fileNames.withIndex()) {
            val elements = moves[i].value ?: break
            movesCumulative.addAll(elements)

            if (i == 0) continue

            val newFileName =
                "upTo-${fileName.substringBefore('.')}.schem"


            val schemFile = waitOptimizedRecordRomSchem(movesCumulative, encoding, Move.wait)
            writeSchematic(schemFile, "9x9fs out/$newFileName")
        }
    }
    "generate each" {
        for ((i, fileName) in fileNames.withIndex()) {
            val elements = moves[i].value ?: continue
            val newFileName = fileName.substringBefore('.') + ".schem"

            val schemFile = waitOptimizedRecordRomSchem(elements, encoding, Move.wait)
            writeSchematic(schemFile, "9x9fs out/$newFileName")
        }
    }
})


class AnalyzeSeq : StringSpec({
    "analyze" {
        val fullSeq = moves.flatMap { it.value ?: emptyList() }

        val PinkModSize = 18
        var pinkPos = -8 + 18

        val heightToWaits = List(PinkModSize) { sortedSetOf<Int>() }

        var lastPurple: Int? = null

        for ((i, move) in fullSeq.withIndex()) when (move) {
            Move.pink -> {
                pinkPos = (pinkPos + 1) % PinkModSize
            }

            Move.purple -> lastPurple = if (lastPurple == null) {
                i
            } else {
                val value = i - lastPurple!! - 1
                heightToWaits[pinkPos] += value
                null
            }

            else -> {}
        }

        for (height in 0 until PinkModSize) {
            println("$height: ${heightToWaits[height].joinToString()}")
        }
    }

    "gen wait rom" {
        val waits = intArrayOf(17, 19, 20, 22, 23, 25)

        val seq = waits.flatMap { n ->
            buildList {
                add(Move.purple)
                repeat(n) { add(Move.wait) }
                add(Move.purple)
                add(Move.pink)
                while (size < 27) add(Move.wait)
            }
        }

        val schemFile = waitOptimizedRecordRomSchem(seq, encoding, Move.wait)
        writeSchematic(schemFile, "9x9fs out/wait-rom.schem")
    }
    "reencode waits" {
        val waits = intArrayOf(17, 19, 20, 22, 23, 25, 26)
        var curPinkPos = -8 + 18

        data class MoveCount(val move: Move, var count: Int)

        for (filename in fileNames) {
            val f = File("9x9fs moves/$filename")
            if (!f.exists()) continue
            val file = f
                .readLines()
                .filter { it.isNotBlank() }
                .map(::parseLine)


            var lastWait: MoveCount? = null
            var lastPurpleIndex = -1

            val newList = mutableListOf<MoveCount>()

            var i = 0

            for ((move, n) in file) {
                val cur = MoveCount(move, n)
                when (move) {
                    Move.pink -> {
                        curPinkPos = (curPinkPos + n) % 18
                    }
                    Move.purple -> {
                        if (lastPurpleIndex == -1) {
                            lastPurpleIndex = i
                            lastWait = null
                        } else {
                            val nWaits = i - lastPurpleIndex - 1
                            val expected = waits[curPinkPos]

                            val diff = nWaits - expected
                            lastWait!!.count -= diff

                            lastPurpleIndex = -1
                        }
                    }

                    Move.wait -> {
                        lastWait = cur
                    }

                    else -> {}
                }
                newList.add(cur)
                i+=n
            }

            // write new file
            val fileNoPath = filename.substringBefore('.')
            val newFile = File("9x9fs moves/$fileNoPath-2.txt")

            newFile.writeText(newList.joinToString("\n") { (move, n) ->
                if (n == 1) {
                    move.toString()
                } else {
                    "$move $n"
                }
            })
        }
    }
})
