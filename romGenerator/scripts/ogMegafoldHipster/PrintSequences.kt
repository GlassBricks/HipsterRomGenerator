package ogMegafoldHipster

import io.kotest.core.spec.style.StringSpec
import java.io.File


fun <T> Appendable.printLines(sequence: Sequence<T>) = sequence.forEach { appendLine(it.toString()) }
fun <T> Appendable.printLines(sequence: Iterable<T>) = sequence.forEach { appendLine(it.toString()) }

class WriteSequences : StringSpec({
    "11x11 glass" {
        val sequence = glassSequence[11]
        getSequence { sequence() }
        writeFile("run-output/sequence/flat-11.txt", getSequence { sequence() }.joinToString(separator = "\n"))
        writeFile("run-output/sequence/groups-11.txt", printAll { sequence() })
        writeFile("run-output/sequence/tree-11.txt", printTree { sequence() })
    }
    "6x6 special" {
        val sequence = special6x6Sequence
        writeFile("run-output/sequence/special-6.txt", sequence.joinToString(separator = "\n"))
    }
})

fun writeFile(file: String, content: String) {
    File(file).also { it.parentFile.mkdirs() }
        .writeText(content)
}
