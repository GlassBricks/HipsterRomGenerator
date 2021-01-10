package me.glassbricks.sequence

import me.glassbricks.sequence.Move.*
import kotlin.math.abs

typealias B = PistonSequenceBuilder

val B.morePistons: Unit get() = add(MorePistons)
val B.clearPistons get() = add(ClearPistons)
val B.moreObs get() = add(MoreObs)
val B.clearObs get() = add(ClearObs)
val B.store get() = add(Store)
val B.spe get() = add(Spe)
val B.dpe get() = add(Dpe)
//val B.tpe get() = add(Tpe)

/**
 * The actual sequence(s) for the glass hipster door.
 *
 * Row 0 = floor, 1 = first row, etc.
 */
object HipsterSequences : SequenceGroupHolder() {

    /** Does the entire sequence for a door of height N. */
    val entireSequence by group { n ->
        //dpe already happened
        if (n % 2 == 1) { // fix floor block
            dpe
            store
            dpe
        }

        for (i in 1..n) {
            row(i)
        }
    }


    /** Does the ENTIRE sequence for row n, including store */
    val row by group { n ->
        if (n != 1) //dpe already happened
            full(n)
        store

        inline = n <= 3
    }

    /** Pulls row n all the way down. */
    val full by func { n ->
        require(n in 1..11)
        extend(n)
        retract(n)
    }

    /**
     * [full], but for n>=3 and a layer of pistons already are deployed.
     * Also used for special case n=6,7.
     */
    val fullWithMorePistons by func { n ->
        require(n in 3..11)
        if (n in 6..7) {
            extendWithMorePistons(-n)
        } else {
            extendWithMorePistons(n)
        }
        retract(n)
    }

    /** Grabs or spits a row at height n, leaves in piston-observer-stack state */
    val extend by func { n ->
        require(n in 0..11)
        if (n in 0..2) {
            (n + 1).pe
        } else {
            morePistons
            extendWithMorePistons(n)
        }
    }

    /**
     * [extend] but for n>=3 when pistons already have been added.
     * n = -6 or -7 are special cases.
     */
    val extendWithMorePistons: PistonSequenceGroup by group("extend") { rn ->
        require(rn in 3..11 || rn == -6 || rn == -7)
        val n = abs(rn)

        moreObs
        // kick the obs out
        if (n - 3 in 0..2) {
            (n - 3 + 1).pe
        } else {
            // another layer of pistons
            dpe
            morePistons
            extendWithMorePistons(n - 3)
        }

        // Additional pulses so that top piston is actually powered
        // 1pe is actually 2 1pe's, since 2pe and 3pe spit but 1pe doesn't
        when (rn) {
            3 -> Unit // already down in [extend]
            4, 5 -> (n - 2).pe
            // special case n=6: FOUR 1pes because of floor powering
            6 -> repeat(2) { spe }
            // specialer cases for pistons already in air; results in weirdness
            -6 -> repeat(3) { spe }
            -7 -> repeat(6) { dpe }
            7, 8 -> repeat(2) { (n - 5).pe }
            9 -> repeat(6) { spe }
            10, 11 -> repeat(12) { (n - 8).pe }
        }
    }

    /** After [extend] (at the piston-observer-stack state), retracts everything, including the top grabbed block */
    val retract: PistonSequenceGroup by group { n ->
        require(n in 0..11)
        if (n in 0..2) return@group // base case 1: block already down, no stack

        // remove obs
        retract(n - 3)
        clearObs

        // pull pistons down
        pull(n - 2)
        if (n == 3) { // base case 2: remove layer of pistons
            clearPistons
            // would have been tpe, but special case due to floor powering
            dpe
        } else {
            // row n is now 1 block down, and pistons have been pulled
            // do sequence for previous row
            fullWithMorePistons(n - 1)
        }
    }

    /** From empty, pulls pistons at row n at least 1 block down */
    val pull by group { n ->
        require(n in 1..11)
        if (n in 0..2) {
            // simply grab pistons
            (n + 1).pe
            return@group
        }

        // another layer of pistons, pulls top pistons
        morePistons
        extendWithMorePistons(n)

        // remove obs
        retract(n - 3)
        clearObs

        // remove below pistons
        full(n - 2)
        clearPistons
    }
}