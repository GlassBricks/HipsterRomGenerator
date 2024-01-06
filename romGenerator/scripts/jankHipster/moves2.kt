package jankHipster

import io.kotest.matchers.shouldBe
import jankHipster.Move.*
import me.glassbricks.infinirom.SSEncoding
import ogMegafoldHipster.SequenceBuilder


class HipSequences(
    private val nFolds: Int = 1,
    private val nObs: Int = 2
) : SequenceBuilder<Move>() {
    var expect: List<Move>? = null

    override fun add(element: Move) {
        elements.add(element)
        when (element) {
            fold -> require(nFolds >= 1)
            fold2 -> require(nFolds >= 2)
            b -> require(nObs >= 2)
            back -> require(nObs >= 3)
            else -> {}
        }
        expect?.let {
            if (element != it[elements.lastIndex]) {
                element shouldBe it[elements.lastIndex]
            }
        }
    }


    private val J = arrayOf(jank, t1, t3, o)
    private var obsOut = false
        set(value) {
            if (value == field) {
                error("obsOut already $value")
            }
            field = value
        }

    /**
     * Adds another row of pistons to the top, and spits them.
     *
     * Bigger row number allows smaller row numbers in the future
     *
     * ______ -> __P___
     */
    private fun morePistons(row: Int) {
        when (row) {
            1 -> add(mid, t4)
            2 -> add(bot, t4)
            3 -> add(bot, fold, t4)
            4 -> add(bot, fold2, t4)
            else -> error("Invalid row: $row")
        }
    }

    fun store() {
        add(o, o)
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
        morePistons(n / 2)
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

            3 -> addAll(J)
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
                morePistons(1)
                add(b, *J, *J)
                obsOut = true
            }

            7 -> {
                +o
                morePistons(2)
                add(b, o, t4, t4, t4, t4, o)
                obsOut = true
            }

            8 -> {
                +o
                morePistons(2)
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
                morePistons(3)
                add(
                    b, o,
                    mid, back, b,
                    t1, t1,
                    o,
                    t1, t1, t1, t1, t3, o,
                    *J
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
        when (val obsRow = n - 3) {
            -1, 0, 1 -> {}
            2 -> {
                retract(obsRow)
                if (obsOut) {
                    add(back, fold2) // fold2 is waiting move
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
                add(b, back, fold2) // fold2 is waiting move
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
        morePistons(n / 2)
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
            2 -> add(bot, mid, mid, fold, t1, t1)
            3 -> add(bot, mid, mid, t1, t1)
            4, 5 -> add(fold, bot, mid, mid, t1, t1)
            else -> TODO("pow retract($n)")
        }
        // ____P_
    }


    fun fullDoor(n: Int) {
//        if (n % 2 == 1) +t4
        for (r in 1 until n) {
            row(r)
            store()
        }
        row(n)

        // floor block
        check(elements.removeLast() == t4)
        add(t1, t1, t1)
    }
}


val encoding2 = SSEncoding(
    fold to 1,
    mid to 2,
    bot to 3,
    back to 4,
    b to 7,
    fold2 to 8,
    o to 10,
    jank to 11,
    t1 to 12,
    t3 to 14,
    t4 to 15,
)
