package notahipster

import me.glassbricks.CHEST_MAX
import me.glassbricks.infinirom.*


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

@DslMarker
annotation class SeqGenDsl

@SeqGenDsl
interface MoveAdd {
    fun add(move: Move)
}

class MulToken(
    private val moveAdd: MoveAdd,
    private val move: Move,
) {
    operator fun times(amount: Int) {
        repeat(amount - 1) { moveAdd.add(move) }
    }
}

private fun MoveAdd.addAndGetToken(move: Move) = MulToken(this, move).also { add(move) }
val MoveAdd.e get() = addAndGetToken(Move.e)
val MoveAdd.d get() = addAndGetToken(Move.d)
val MoveAdd.dpe get() = addAndGetToken(Move.dpe)
val MoveAdd.tpe get() = addAndGetToken(Move.tpe)
val MoveAdd.g get() = addAndGetToken(Move.g)
val MoveAdd.lb1t get() = addAndGetToken(Move.lb1t)
val MoveAdd.lb4t get() = addAndGetToken(Move.lb4t)
val MoveAdd.f get() = addAndGetToken(Move.f)
val MoveAdd.worm get() = add(Move.worm)
private const val MaxTape = 6

class SimpleMoveCollector : MoveAdd {
    val moves = mutableListOf<Move>()
    override fun add(move: Move) {
        moves.add(move)
    }
}

class SeqGen(
    pinkTapeLevel: Int,
    val pinkSize: Int,
    val waitMoves: List<Int>,
    val addPurpleInnerMovesFirst: Boolean,
) : MoveAdd {
    init {
        require(waitMoves.size == MaxTape + 1)
    }

    var pinkTapeLevel = pinkTapeLevel
        private set
    val moves = mutableListOf<Move>()
    override fun add(move: Move) {
        moves.add(move)
        if (move == Move.pink) {
            pinkTapeLevel = (pinkTapeLevel + 1) % pinkSize
        }
    }


    fun tape(
        height: Int,
        block: MoveAdd.() -> Unit = {},
    ) {
        require(height in 0..MaxTape)
        while (pinkTapeLevel != height) add(Move.pink)
        add(Move.purple)

        val innerMoves = SimpleMoveCollector().apply(block).moves
        val numWaitMoves = waitMoves[height]

        if (addPurpleInnerMovesFirst) {
            moves.addAll(innerMoves)
        }
        repeat(numWaitMoves - innerMoves.size) { add(Move.wait) }
        if (!addPurpleInnerMovesFirst) {
            moves.addAll(innerMoves)
        }

        add(Move.purple)
    }
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

// Boundary between carts adds some number of free waiting moves.
// But waits only happen inside purple tapes.
// So, if purples span a cart, can remove that number waiting moves ANYWHERE in between purples.
// Also handle case when wait moves span multiple carts
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

