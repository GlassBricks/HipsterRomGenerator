package notahipster

import me.glassbricks.schem.*
import java.io.File


@Suppress("EnumEntryName")
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

val encoding = Move.values().associateWith { it.ss }

data class MoveLine(val move: Move, var count: Int, var comment: String? = null) {
    init {
        require(count > 0)
    }

    override fun toString(): String = buildString {
        if (count > 1) append(count).append(' ')

        append(move.name)

        if (comment != null) {
            append(" ".repeat(15 - this.length))
            append("# ").append(comment)
        }
    }
}


fun parseLine(line: String): MoveLine {
    val comment = line.substringAfterLast('#', "").trim().takeIf { it.isNotBlank() }
    val value = line.substringBefore('#').trim()
    if (' ' in value) {
        val (a, b) = value.split(' ')
        return if (a.toIntOrNull() != null) {
            MoveLine(Move.valueOf(b), a.toInt(), comment)
        } else {
            MoveLine(Move.valueOf(a), b.toInt(), comment)
        }

    }
    return MoveLine(Move.valueOf(value), 1, comment)
}

fun parseFile(file: File): List<Move> =
    file.readLines()
        .filter { it.isNotBlank() }
        .map(::parseLine)
        .flatMap { (move, n) ->
            List(n) { move }
        }


private val shouldDispersePinks = true
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

                val step = finalSegSize / (numPinks + 1.0)
                for (i in 0 until numPinks) {
                    val index = (i * step).toInt()
                    lastSeg.add(index, Move.pink)
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
                if (curPurpCanOpt > 0 && curPurpWaitBegin != null && moves.getOrNull(i) != Move.purple) {
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
        boxes.map { SSBox(it.map { SignalStrength(it.ss) }) }
    )
    return toRecordChestRomSchem(rom)
}
