package hipster.chungus

import hipster.chungus.Move.*
import me.glassbricks.sequence.SimpleSequenceVisitor
import me.glassbricks.splitCamelCase


@Suppress("EnumEntryName")
enum class Move(strName: String? = null) {
    bot,
    mid,
    fold1, // sticky
    fold2, // normal
    fold3, //
    t1("1t"),
    t3("3t"),
    t4("4t"),
    jank("J"),
    o,
    b,
    back,
    storage,
    wait
    ;

    private val strName = strName ?: name.splitCamelCase()

    override fun toString(): String = strName
}


class ChungusSeqBuilder : SimpleSequenceVisitor<Move>() {

    private var obsOut = false

    /**
     * Adds another row of pistons to the top, and spits them.
     *
     * Bigger row number allows smaller row numbers in the future
     *
     * ______ -> __P___
     */
    private fun morePistonsAndSpit(row: Int) {
        require(row in 1..5)

        when (row) {
            1 -> add(mid)
            2 -> add(bot)
            3 -> add(bot, fold1)
            4 -> add(bot, fold2)
            5 -> add(bot, fold2, fold3)
        }
        add(t4)
    }


    /**
     *  Sequence for row n
     *
     *  _____B -> B_____
     */
    fun row(n: Int) {
        require(n >= 1)
        if (n == 1) {
            +t4
            return
        }

        // add a row of pistons
        morePistonsAndSpit(n / 2)
        rowWithPistonsUp(n, false)
    }

    /**
     * Sequence for row n, but with a row of pistons already added
     *
     * __P__B -> B_____
     */
    private fun rowWithPistonsUp(n: Int, pistonsHigh: Boolean) {
        require(n >= 2)

        // __P__B
        extendAndRetractObs(n, pistonsHigh)
        // ____PB*
        retract(n)
        // B_____
    }

    /**
     * With the first row of pistons already added, extends so that row n is grabbed, then clears everything below.
     *
     * __P__B -> ____PB_
     */
    private fun extendAndRetractObs(n: Int, pistonsHigh: Boolean) {
        // first, extend, ending in "piston-obs stack" state,
        // ends in __PO_PO_PB*
        when (n) {
            2 -> {
                add(t1, t1)
                if (obsOut) {
                    add(o, o, b, b)
                } else {
                    add(o, b, b, o)
                }
            }

            3 -> add(jank)
            4 -> add(o, t4, t4, o)
            5 -> {
                add(o, mid)
                t1 * 2
                if (obsOut) +back
                t1 * 2
                +b; o * 6; +b
            }

            6 -> {
                +o
                morePistonsAndSpit(1)
                add(b, jank, jank)
                obsOut = true
            }

            7 -> {
                +o
                morePistonsAndSpit(2)
                add(b, o, t4, t4, t4, t4, o)
                obsOut = true
            }

            8 -> {
                +o
                morePistonsAndSpit(2)
                add(
                    b, o,
                    mid,
                    back, b,
                    t1, t1,
                )
                if (pistonsHigh) {
                    add(t1, t1)
                    o * 10
                    b * 6
                } else {
                    o * 16
                }
                obsOut = true
            }

            9 -> {
                if (pistonsHigh) {
                    TODO("pistonsHigh for 9")
                }
                +o
                morePistonsAndSpit(3)
                add(
                    b, o,
                    mid, back, b,
                    t1, t1,
                    o,
                    t1, t1, t1, t1, t3, o,
                    jank
                )
                obsOut = true
            }

            else -> TODO("pow($n)")
        }

        // now, retract the bottommost obs(es)
        // ends in: ____PB*
        // this could be a separate function, but our sizes are small enough for now

        // __PO_PB_
        // (retract obs row)
        // O___PB_
        // (clear obs)
        // ____PB_

        // the following could maybe be made more recursive, but whatever
        when (val obsRow = n - 3) {
            -1, 0, 1 -> {}
            2 -> {
                retract(obsRow)
                if (obsOut) {
                    +back
                } else {
                    +o
                }
            }

            3, 4 -> {
                retract(obsRow)
                add(b, o)
                obsOut = false
            }

            5, 6 -> {
                // 2 layers of obs!
                retract(obsRow - 3)
                add(b, back)
                retract(obsRow)
                add(b, o)
                obsOut = false
            }

            else -> TODO("obs retract for $obsRow")
        }
    }


    /**
     * With piston at row n-2 and block at row n-1, finishes retraction of row n
     *
     * ____PB_ -> B_____
     */
    private fun retract(n: Int) {
        require(n >= 2)

        if (n == 2) {
            // base case: no more pistons, manual sequence
            return add(mid, t4, t1, t4)
        }
        // ____PB_
        pull(n - 2)
        // ___P_B_
        rowWithPistonsUp(n - 1, true)
        // B______
    }

    /**
     * Pulls pistons at row n 1 down.
     *
     * _____P -> ____P_
     */
    private fun pull(n: Int) {
        require(n >= 1)
        // special case: row 2 doesn't need to pull
        if (n == 1) return

        // ______P
        morePistonsAndSpit(n / 2)
        // __P___P
        extendAndRetractObs(n, true)
        // ____PP_


        // now, remove the below pistons

        // base case: manual sequence
        if (n == 2) {
            add(mid, t4, t1, t1, bot, mid)
            return
        }

        // now, retract the pistons that pulled row n
        val pistonRow = n - 2
        row(pistonRow)
        // P___P_

        // retract the extra pistons, so ready for the next row
        when (pistonRow) {
            1 -> add(mid, t1, t1)
            2 -> add(bot, mid, mid, fold1, t1, t1)
            3 -> add(bot, mid, mid, t1, t1)
            4, 5 -> add(fold1, bot, mid, mid, t1, t1)
            else -> TODO("pow retract($n)")
        }
        // ____P_
    }


    fun fullDoor(n: Int) {
        for (r in 1 until n) {
            row(r)
            if (r == 9) +storage
            add(o, o)
        }
        row(n)

        // floor block
        check(elements.removeLast() == t4)
        t1 * 3
    }

}

fun getChungusSequence(
    fn: ChungusSeqBuilder.() -> Unit
): List<Move> {
    val rawMoves = ChungusSeqBuilder().apply(fn).build()

    return buildList {
        rawMoves.forEach {
            add(it)
            if (it == jank)
                add(wait)
        }
    }
}

val ChungusEncoding = mapOf(
    1 to t1,
    2 to t3,
    3 to storage,
    4 to t4,
    5 to mid,
    6 to fold1,
    7 to bot,
    8 to fold3,
    9 to b,
    10 to fold2,
    11 to back,
    12 to o,
    13 to jank,
    14 to wait
).entries.associate { (k, v) -> v to k }