package me.glassbricks.sequencegen

import me.glassbricks.sequencegen.Move.*

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

/** Does the entire sequence for a door of height N, including floor block and initial dpe */
val entireSequence by group { n ->
    //dpe already happened
    if (n % 2 == 1) fixFloorBlock()
    for (i in 1..n) {
        if (i != 1) full(i)
        store
    }
}

val fixFloorBlock by seq {
    dpe
    store
    dpe
}

/** The entire sequence for row n */
val full by group { n ->
    require(n in 1..11)
    extend(n)
    retract(n)
}

/** [full], but for n>=3 and a layer of pistons have already been added */
val fullWithMorePistons by func { n ->
    require(n in 3..11)
    extendWithMorePistons(n)
    retract(n)
}

/** Grabs or spits a row at height n, leaves in piston-stack state */
val extend by func { n ->
    require(n in 0..11)
    if (n in 0..2) {
        (n + 1).pe
    } else {
        morePistons
        extendWithMorePistons(n)
    }
}

/** [extend], but for n>=3 and a layer of pistons have already been added */
val extendWithMorePistons: PistonSequenceGroup by group { n ->
    require(n in 3..11)

    moreObs
    if (n >= 6) 2.pe // another layer of pistons coming
    extend(n - 3)

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
    if (n in 0..2) return@group // no pistons or obs out, block already down
    // remove obs at n-3 (if needed)
    retract(n - 3)
    clearObs
    // pull pistons at n-2 down
    pull(n - 2)

    if (n == 3) {
        // special case due to floor powering and base case clearing pistons
        clearPistons
        dpe
    } else {
        // row is now 1 block down, and pistons pulled
        // do sequence for previous row
        fullWithMorePistons(n - 1)
    }
}

/** Pulls pistons at row n at least 1 block down */
val pull by group { n ->
    // grab pistons
    extend(n)
    if (n in 0..2) return@group // no piston stack

    // remove obs
    retract(n - 3)
    clearObs

    // remove below pistons
    full(n - 2)
    clearPistons
}

