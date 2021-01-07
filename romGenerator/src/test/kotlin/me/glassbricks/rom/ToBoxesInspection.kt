package me.glassbricks.rom

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.sequence.HipsterSequences

class ToBoxesInspection : StringSpec({
    "first three" {
        val boxes = HipsterSequences.entireSequence[3].toRom()
        println(boxes)
    }

})
