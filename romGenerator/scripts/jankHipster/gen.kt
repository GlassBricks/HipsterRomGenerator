package jankHipster

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.infinirom.SSEncoding
import me.glassbricks.infinirom.encode
import me.glassbricks.infinirom.toInifinirom1
import me.glassbricks.schem.writeTo
import ogMegafoldHipster.getSequence
import java.io.File


fun List<Move>.writeInfRomSchem(
    name: String,
    encoding: SSEncoding<Move> = encoding1,
) {
    val schem = encoding.encode(this).toInifinirom1()
    schem.writeTo(File(name))
}

class MakeSchems : StringSpec({
    "print seq len" {
        println(getSequence { seq7() }.size)
    }

    "test schem" {
        val seq = List(20) { Move.t4 }
        seq.writeInfRomSchem("t4s.schem")
    }

    "6x6 schem" {
        val seq = getSequence { seq6() }
        seq.writeInfRomSchem("6x6.schem")
    }

    "7x7 schem" {
        val seq = getSequence { seq7() }
        seq.writeInfRomSchem("7x7.schem")
    }

    "row7 only schem" {
        val seq = getSequence { row7() }
        seq.writeInfRomSchem("row7.schem")
    }

    "print row7 only" {
        val seq = getSequence { row7() }
        println(seq.joinToString("\n"))
    }

    "row8 new" {
        val seq = HipSequences(nObs = 3, nFolds = 2).apply { row(8) }.build()

        seq.writeInfRomSchem("row8new.schem", encoding2)
    }

    "8x8 new" {
        val seq = HipSequences(nObs = 3, nFolds = 2).apply { +Move.t4; fullDoor(8) }.build()
        seq.writeInfRomSchem("8x8new.schem", encoding2)
    }

    "print 8 length" {
        val seq = HipSequences(nObs = 3, nFolds = 2).apply { fullDoor(8) }.build()
        println(seq.size)
    }

    "print row 8" {
        val seq = HipSequences(nObs = 3, nFolds = 2).apply { row(8) }.build()
        println(seq.joinToString("\n"))
    }

    "row9 new" {
        val seq = HipSequences(nObs = 3, nFolds = 2).apply { row(9) }.build()
        seq.writeInfRomSchem("row9new.schem", encoding2)
    }

    "9x9 new" {
        val seq = HipSequences(nObs = 3, nFolds = 2).apply { +Move.t4; fullDoor(9) }.build()
        seq.writeInfRomSchem("9x9new.schem", encoding2)
    }

    "print 9 length" {
        val seq = HipSequences(nObs = 3, nFolds = 2).apply { fullDoor(9) }.build()
        println(seq.size)
    }

    "print 4 through 11 length" {
        for (i in 4..11) {
            val seq = HipSequences(nObs = 3, nFolds = 2).apply { fullDoor(i) }.build()
            println("$i: ${seq.size}")
        }
    }
})
