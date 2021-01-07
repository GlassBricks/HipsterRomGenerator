package me.glassbricks.sequence

import me.glassbricks.sequence.Move.*

typealias G = PistonSequenceBuilder

val G.morePistons: Unit get() = add(MorePistons)
val G.clearPistons get() = add(ClearPistons)
val G.moreObs get() = add(MoreObs)
val G.clearObs get() = add(ClearObs)
val G.store get() = add(Store)
val G.spe get() = add(Spe)
val G.dpe get() = add(Dpe)
val G.tpe get() = add(Tpe)

// n: 0=floor block, 1=first block, etc.

object HipsterSequences {

    /** Does the entire sequence for a door of height N. */
    val entireSequence by group { n ->
        //dpe already happened
        if (n % 2 == 1) { // fix floor block
            dpe
            store
            dpe
        }


        for (i in 1..n) {
            entire(i)
        }
    }


    /** Does the ENTIRE sequence for row n, including store and initial block */
    val entire by group { n ->
        if (n != 1)
            full(n)
        store

        inline = n <= 3
    }

    /** Pulls row n all the way down. */
    val full by func { n ->
        require(n in 1..11)
        if (n in 0..2) {
            (n + 1).pe
        } else {
            // another layer of pistons
            morePistons
            fullWithMorePistons(n)
        }
    }

    /** [full], but for n>=3 and a layer of pistons have already been added */
    val fullWithMorePistons by func { n ->
        require(n in 3..11)
        extend(n)
        retract(n)
    }

    /** Grabs or spits a row at height n, leaves in piston-stack state. Only for n>=3 when pistons already have been added */
    val extend: PistonSequenceGroup by group("extend") { n ->
        require(n in 3..11)

        moreObs
        // kick the obs out
        if (n - 3 in 0..2) {
            (n - 3 + 1).pe
        } else {
            // another layer of pistons
            dpe
            morePistons
            extend(n - 3)
        }

        // Additional pulses so that top piston is actually powered
        // 1pe is actually 2 1pe's, since 2pe and 3pe spit but 1pe doesn't
        when (n) {
            3 -> Unit // already down in [extend]
            4, 5 -> (n - 2).pe
            // special case n=6: FOUR 1pes because of floor powering
            6 -> repeat(2) { spe }
            7, 8 -> repeat(2) { (n - 5).pe }
            9 -> repeat(6) { spe }
            10, 11 -> repeat(12) { (n - 8).pe }
        }
    }

    /** After [extend] (at the piston-observer-stack state), retracts everything, including the top grabbed block */
    val retract: PistonSequenceGroup by group { n ->
        require(n in 0..11)
        if (n in 0..2) return@group // base case 1: block already down

        // remove obs at n-3
        retract(n - 3)
        clearObs

        // pull pistons at n-2 down
        pull(n - 2)
        if (n == 3) {
            // base case 2: remove layer of pistons
            clearPistons
            // would have been tpe, but special case due to floor powering
            dpe
        } else {
            // row n is now 1 block down, and pistons have been pulled
            // do sequence for previous row
            fullWithMorePistons(n - 1)
        }
    }

    /** Pulls pistons at row n at least 1 block down */
    val pull by group { n ->
        require(n in 1..11)
        if (n in 0..2) {
            // simply grab pistons
            (n + 1).pe
            return@group
        }

        // another layer of pistons
        morePistons
        extend(n)

        // remove obs
        retract(n - 3)
        clearObs

        // remove below pistons
        full(n - 2)
        clearPistons
    }
}