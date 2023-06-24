package notahipster

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
val MoveAdd.worm get() = addAndGetToken(Move.worm)

//val MoveAdd.wait get() = addAndGetToken(Move.wait)
//val MoveAdd.purple get() = addAndGetToken(Move.purple)
//val MoveAdd.pink get() = addAndGetToken(Move.pink)


private const val MaxTape = 6

class SimpleMoveCollector : MoveAdd {
    val moves = mutableListOf<Move>()
    override fun add(move: Move) {
        moves.add(move)
    }
}

typealias MoveBlock = MoveAdd.() -> Unit

class SeqGen(
    pinkTapeLevel: Int,
    val pinkSize: Int,
    val waitMoves: List<Int>,
    val addPurpleInnerMovesFirst: Boolean ,
    val addFinalPurple: Boolean ,
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
        block: MoveBlock = {},
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

        if (addFinalPurple) add(Move.purple)
    }
}
