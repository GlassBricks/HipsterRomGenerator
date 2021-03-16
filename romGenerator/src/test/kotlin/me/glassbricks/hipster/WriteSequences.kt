package me.glassbricks.hipster

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.sequence.*
import java.io.File
import java.io.PrintStream

class WriteSequences : StringSpec({
    "11x11 glass" {
        val sequence = HipsterSequences.glassSequence[11]
        sequence.writeTo(File("build/run-output/sequence/flat-11.txt")) { printFlat(it) }
        sequence.writeTo(File("build/run-output/sequence/groups-11.txt")) { printGroups(it) }
        sequence.writeTo(File("build/run-output/sequence/tree-11.txt")) { printTree(it) }
    }
    "6x6 special" {
        val sequence = normal6x6Sequence
        sequence.writeTo(File("build/run-output/sequence/special-6.txt")) { printFlat(it) }
    }
})

fun <T : SequenceItem> MoveSequence<T>.writeTo(file: File, action: MoveSequence<T>.(Appendable) -> Unit) {
    file.also { it.parentFile.mkdirs() }
        .outputStream().buffered()
        .let { PrintStream(it) }
        .use { action(it) }
}
