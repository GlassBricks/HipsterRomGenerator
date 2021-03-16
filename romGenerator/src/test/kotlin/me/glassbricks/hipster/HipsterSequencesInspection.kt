package me.glassbricks.hipster

import io.kotest.core.spec.style.StringSpec

class HipsterSequencesInspection : StringSpec({
    "number of more pistons"  {
        HipsterSequences.glassSequence[11].flattened
            .count { it === HipsterMove.MorePistons }.let(::println)
    }
})