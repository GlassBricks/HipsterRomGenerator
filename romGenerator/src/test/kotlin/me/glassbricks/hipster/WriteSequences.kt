package me.glassbricks.hipster

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.sequence.printGroups
import me.glassbricks.sequence.printLines
import me.glassbricks.sequence.printTree
import java.io.File
import java.io.PrintStream

class WriteSequences : StringSpec({
    "11x11 glass" {
        val sequence = HipsterSequences.glassSequence[11]
        sequence.writeTo(File("build/run-output/sequence/flat-11.txt")) { it.printLines(flattened) }
        sequence.writeTo(File("build/run-output/sequence/groups-11.txt")) { printGroups(it) }
        sequence.writeTo(File("build/run-output/sequence/tree-11.txt")) { printTree(it) }
    }
    "6x6 special" {
        val sequence = special6x6Sequence
        sequence.writeTo(File("build/run-output/sequence/special-6.txt")) { it.printLines(this) }
    }
})

private fun <T> T.writeTo(file: File, action: T.(Appendable) -> Unit) {
    file.also { it.parentFile.mkdirs() }
        .outputStream().buffered()
        .let(::PrintStream)
        .use { action(it) }
}
