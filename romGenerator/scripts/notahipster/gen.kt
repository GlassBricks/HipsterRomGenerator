package notahipster

import me.glassbricks.infinirom.SSEncoding
import me.glassbricks.infinirom.toRecordCartSchem
import me.glassbricks.schem.writeTo
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class Params(
    val tapeWaitMoves: List<Int>,
    val addPurpleInnerMovesFirst: Boolean,
    val addFinalPurple: Boolean,
    val pinkTapeSize: Int,
    val pinkTapeInitialPos: Int,
    val doPinkDisperse: Boolean,
    val doWaitOptimization: Int,
    val encoding: SSEncoding<Move>,
)

private val params = Params(
    tapeWaitMoves = listOf(28, 29, 31, 34, 36, 38, 40),
    addPurpleInnerMovesFirst = true,
    addFinalPurple = false,
    pinkTapeSize = 16,
    pinkTapeInitialPos = -6 + 16,
    doPinkDisperse = false,
    doWaitOptimization = 3,
    encoding = SSEncoding(
        Move.e to 1,
        Move.d to 2,
        Move.wait to 3,
        Move.dpe to 4,
        Move.tpe to 5,
        Move.purple to 6,
        // 7 is unusable wait move
        Move.worm to 8,
        Move.g to 9,
        Move.lb1t to 10,
        Move.lb4t to 11,
        Move.f to 12,
        Move.pink to 13,
    ),
)

fun main() {
    clearOutDir()
    genEachRow()
    genFullSeq()
    zipOutDir()
}

/*
old encoding
 = mapOf(
    Move.e to 2,
    Move.d to 3,
    Move.dpe to 4,
    Move.tpe to 5,
    Move.g to 6,
    Move.lb1t to 8,
    Move.lb4t to 9,
    Move.f to 10,
    Move.worm to 11,
    Move.wait to 12,
    Move.purple to 13,
    Move.pink to 15,
)
 */


private val outDir = File("romGenerator/9x9fs out")
private fun clearOutDir() {
    outDir.mkdirs()
    outDir.listFiles()?.forEach { it.deleteRecursively() }
}

private val rowFns = listOf(
    SeqGen::row1234,
    SeqGen::row5,
    SeqGen::row6,
    SeqGen::row7,
    SeqGen::row8,
    SeqGen::row9,
)

private fun genEachRow() {
    for (fn in rowFns) {
        val gen = SeqGen(
//        pinkTapeLevel = params.initialPinkPos,
            pinkTapeLevel = 0,
            pinkSize = params.pinkTapeSize,
            waitMoves = params.tapeWaitMoves,
            addPurpleInnerMovesFirst = params.addPurpleInnerMovesFirst,
        )
        fn(gen)
        writeSchem(gen, outDir.resolve("${fn.name}.schem"))
    }
}

private fun genFullSeq() {
    val gen = SeqGen(
        pinkTapeLevel = params.pinkTapeInitialPos,
        pinkSize = params.pinkTapeSize,
        waitMoves = params.tapeWaitMoves,
        addPurpleInnerMovesFirst = params.addPurpleInnerMovesFirst,
    )
    for (fn in rowFns) fn(gen)
    // final moves
    gen.apply {
        dpe // floor block
        while (pinkTapeLevel != params.pinkTapeInitialPos) add(Move.pink)
    }

//    println(gen.moves)
    writeSchem(gen, outDir.resolve("full.schem"))
}

private fun writeSchem(gen: SeqGen, file: File) {
    var moves: List<Move> = gen.moves
    if (params.doPinkDisperse) moves = dispersePinks(moves)

    val rom = purpleWaitOptimizedRecordInfinirom(moves, params.encoding, params.doWaitOptimization, !params.addFinalPurple)

    val schem = rom.toRecordCartSchem(rotation = 90f)
    schem.writeTo(file)
}


private val versionFile = File("romGenerator/9x9fs moves/cur_version")

private fun zipOutDir() {

    if (!versionFile.exists()) versionFile.writeText("0")
    val version = versionFile.readText().trim().toIntOrNull() ?: 0
    versionFile.writeText((version + 1).toString())
    val files = outDir.listFiles()

    val zipFile = outDir.resolve("9x9fs-$version.zip")
    ZipOutputStream(zipFile.outputStream()).use { zos ->
        files?.forEach { file ->
            zos.putNextEntry(ZipEntry(file.name))
            file.inputStream().use { it.copyTo(zos) }
            zos.closeEntry()
        }
    }
}
