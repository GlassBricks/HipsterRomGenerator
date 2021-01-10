package me.glassbricks.sequence

import io.kotest.core.spec.style.StringSpec

class HipsterSequencesInspection : StringSpec({
    "number of more pistons"  {
        HipsterSequences.entireSequence[11].flattened
            .count { it === Move.MorePistons }.let(::println)
    }
})