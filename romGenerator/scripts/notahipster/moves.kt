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


private val shouldDispersePinks = false
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

// in between chests, there are 2 free waiting moves
// if on boundary there happens to be waiting moves, we can remove up to 2 of them
fun toWaitOptimizedSSBoxes(
    ss: List<SignalStrength>,
    waitingMove: SignalStrength
) = SSBoxes(buildList {
    var lastI = 0
    while (lastI < ss.size) {
        val remaining = ss.size - lastI
        if (remaining <= CHEST_MAX) {
            add(
                SSBox(ss.subList(lastI, ss.size).padToMinimum(CHEST_MAX, waitingMove))
            )
            break
        }
        add(SSBox(ss.subList(lastI, lastI + CHEST_MAX)))
        lastI += CHEST_MAX
        repeat(2) {
            if (ss.getOrNull(lastI) == waitingMove) lastI++
        }
    }
})

fun <T> waitOptimizedRecordRomSchem(
    moves: List<T>,
    encoding: Map<T, Int>,
    waitingMove: T
): SchemFile {
    val waitingSS = SignalStrength(encoding.getValue(waitingMove))
    val ss = encodeToSignalStrengths(moves, encoding)
    val rom = toWaitOptimizedSSBoxes(ss, waitingSS)
    return toRecordChestRomSchem(rom)
}
