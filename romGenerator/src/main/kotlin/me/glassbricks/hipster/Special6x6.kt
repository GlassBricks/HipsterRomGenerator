package me.glassbricks.hipster

import me.glassbricks.hipster.HipsterMove.ClearPistons
import me.glassbricks.hipster.HipsterMove.Store
import me.glassbricks.sequence.MoveSequence

val normal6x6Sequence = MoveSequence("6x6 sequence") {
    HipsterSequences(isSpecial6x6 = true).apply {
        for (i in 1..5) {
            row(i)
        }
//        full(6)
//        moreObs
    }
}

val special6x6Sequence = normal6x6Sequence.flattened.map {
    when (it) {
        Store -> ClearPistons
        else -> it
    }
}
