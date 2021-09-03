package me.glassbricks.rom

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.hipster.HipsterSequences

class ToBoxesInspection : StringSpec({
    "first three" {
        val boxes = HipsterSequences.glassSequence[3].flattened
            .toRom(HipsterSequences.glassHipsterEncoding)
        println(boxes)
    }
})
