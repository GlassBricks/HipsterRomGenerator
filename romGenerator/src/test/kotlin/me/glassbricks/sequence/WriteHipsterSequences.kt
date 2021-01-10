package me.glassbricks.sequence

import io.kotest.core.spec.style.StringSpec
import java.io.File
import java.io.PrintStream

class WriteHipsterSequences : StringSpec({
    val sequence = HipsterSequences.entireSequence[11]
    sequence.writeTo(File("build/run-output/sequence-flat.txt")) { printFlat(it) }
    sequence.writeTo(File("build/run-output/sequence-groups.txt")) { printGroups(it) }
    sequence.writeTo(File("build/run-output/sequence-tree.txt")) { printTree(it) }
})

private fun PistonSequence.writeTo(file: File, action: PistonSequence.(Appendable) -> Unit) {
    file.also { it.parentFile.mkdirs() }
        .outputStream().buffered()
        .let { PrintStream(it) }
        .use { action(it) }
}
