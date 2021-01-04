package me.glassbricks.sequencegen

import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import java.io.File
import java.io.InputStream
import java.io.PrintStream

@OptIn(ExperimentalStdlibApi::class)
@Suppress("BlockingMethodInNonBlockingContext")
class HipsterSequencesTest : StringSpec({
    val sequence = entireSequence[11]
    "generate file" {
        sequence.writeTo(File("temp/sequence-flat.txt")) { printFlat(it) }
        sequence.writeTo(File("temp/sequence-groups.txt")) { printGroups(it) }
    }
    "compare to old" {
        val file = tempfile()
        sequence.writeTo(file) { printFlat(it) }

        val expected = Thread.currentThread().contextClassLoader.getResourceAsStream("old sequence.txt")!!
        val actual = file.inputStream()
        compareStreams(actual, expected) shouldBe true
    }
})

fun compareStreams(actual: InputStream, expected: InputStream): Boolean {
    val a = actual.buffered()
    val b = expected.buffered()
    do {
        val va = a.read()
        val vb = b.read()
        if (va != vb) return false
    } while (va != -1)
    return true
}

private fun PistonSequence.writeTo(file: File, action: PistonSequence.(Appendable) -> Unit) {
    file.also { it.parentFile.mkdirs() }
        .outputStream().buffered()
        .let { PrintStream(it) }
        .use { action(it) }
}
