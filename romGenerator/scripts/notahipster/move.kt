package notahipster

import me.glassbricks.CHEST_MAX
import me.glassbricks.infinirom.RecordInfinirom
import me.glassbricks.infinirom.SSBox
import me.glassbricks.infinirom.SSEncoding
import me.glassbricks.infinirom.fullSSBox


@Suppress("EnumEntryName")
enum class Move {
    e,
    d,
    dpe,
    tpe,
    g,
    lb1t,
    lb4t,
    f,
    worm,
    wait,
    purple,
    pink,
}

fun dispersePinks(seq: List<Move>): List<Move> {
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

// Boundary between carts adds 2 free 2 waiting moves.
// But waits only happen inside purple tapes.
// So, if purples span a chest, can remove 2 waiting moves ANYWHERE in between purples
fun purpleWaitOptimizedRecordInfinirom(
    moves: List<Move>,
    encoding: SSEncoding<Move>,
    numCanOptPerCart: Int,
    removeEndingPurple: Boolean,
): RecordInfinirom {
    val result = mutableListOf<SSBox>()
    val curBox = mutableListOf<Move>()
    var inPurple = false
    var curPurpWaitBegin: Int? = null
    var curBoxCanOpt = numCanOptPerCart
    var i = 0
    var nRemoved = 0
    while (i < moves.size) {
        val move = moves[i++]
        curBox.add(move)
        if (move == Move.purple) {
            inPurple = !inPurple
            if (inPurple) {
                curPurpWaitBegin = null
            }
            if (!inPurple && removeEndingPurple) {
                curBox.removeLast()
            }
        } else if (move == Move.wait) {
            curPurpWaitBegin = curPurpWaitBegin ?: curBox.lastIndex
        }
        if (curBox.size == CHEST_MAX) {
            if (inPurple) {
                if (curBoxCanOpt > 0 && curPurpWaitBegin != null && curBox[curPurpWaitBegin] == Move.wait &&
                    moves.getOrNull(i) != Move.purple
                ) {
                    curBox.removeAt(curPurpWaitBegin)
                    curBoxCanOpt--
                    nRemoved++
                    continue
                }
                while (curBoxCanOpt > 0 && moves.getOrNull(i) == Move.wait) {
                    i++
                    curBoxCanOpt--
                    nRemoved++
                }
            }
            result.add(encoding.encode(curBox).let(::SSBox))
            curBox.clear()
            curPurpWaitBegin = null
            curBoxCanOpt = numCanOptPerCart
        }
    }

    result.add(fullSSBox(encoding.encode(curBox), encoding[Move.wait]))

    println("removed $nRemoved waits")

    return RecordInfinirom(result)
}
