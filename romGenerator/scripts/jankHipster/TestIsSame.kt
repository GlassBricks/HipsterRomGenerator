package jankHipster

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import me.glassbricks.getSequence

class TestIsSame : StringSpec({
    System.setProperty("kotest.assertions.collection.print.size", "100")
    "row 2" {
        val seq1 = getSequence { row2(false); store() }
        val seq2 = HipSequences().apply {
            expect = seq1
            row(2); store()
        }.build()

        seq2 shouldBe seq1
    }
    "row 3" {
        val seq1 = getSequence { row3(); store() }
        val seq2 = HipSequences().apply { row(3); store() }.build()

        seq2 shouldBe seq1
    }

    "row 4" {
        val seq1 = getSequence { row4(); store() }
        val seq2 = HipSequences().apply {
            expect = seq1
            row(4); store()
        }.build()

        seq2 shouldBe seq1
    }

    "row 5" {
        val seq1 = getSequence { row5(); store() }
        val seq2 = HipSequences().apply {
            expect = seq1
            row(5); store()
        }.build()

        seq2 shouldBe seq1
    }

    "row 6" {
        val seq1 = getSequence { row6(); store() }
        val seq2 = HipSequences().apply {
            expect = seq1
            row(6); store()
        }.build()

        seq2 shouldBe seq1
    }

    "row 7" {
        val seq1 = getSequence { row7(); store() }
        val seq2 = HipSequences().apply {
            expect = seq1
            row(7); store()
        }.build()

        seq2 shouldBe seq1
    }
})
