package hipster.jank

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.schem.toCartSchem
import me.glassbricks.sequence.getSequence
import schem.writeSchematic
import java.io.File


class MakeSchems : StringSpec({
//    "print 6x6" {
//        println(printTree { seq6() })
//    }
    "print seq len" {
        println(getSequence { seq7() }.size)
    }

    "test schem" {
//        val seq = getSequence {
//            fullRow1()
//            fullRow2()
//            fullRow3()
//        }
        val seq = List(20) { Move.t4 }
        val schem = toCartSchem(
            seq,
            encoding1
        )
        writeSchematic(schem, File("4t.schem"))
    }
    "6x6 schem" {
        val seq = getSequence { seq6() }
        val schem = toCartSchem(seq, encoding1)
        writeSchematic(schem, File("6x6.schem"))
    }

    "7x7 schem" {
        val seq = getSequence { seq7() }
        val schem = toCartSchem(seq, encoding1)
        writeSchematic(schem, File("7x7.schem"))
    }

    "7x7 new" {
        val seq = HipSequences().apply {
            fullDoor(7)
        }.build()

        val schem = toCartSchem(seq, encoding1)
        writeSchematic(schem, File("7x7new.schem"))
    }

    "row7 only schem" {
        val seq = getSequence { row7() }
        val schem = toCartSchem(seq, encoding1)
        writeSchematic(schem, File("row7.schem"))
    }

    "print row7 only" {
        val seq = getSequence { row7() }
        println(seq.joinToString("\n"))
    }

    "row7 new" {
        val seq = HipSequences().apply { row(7) }.build()

        val schem = toCartSchem(seq, encoding1)
        writeSchematic(schem, File("row7new.schem"))
    }

    "print row5 new" {
        val seq = HipSequences().apply { row(5) }.build()
        println(seq.joinToString("\n"))
    }

    "row8 new only" {
        val seq = HipSequences(nObs = 3, nFolds = 2).apply { row(8) }.build()

        val schem = toCartSchem(seq, encoding2)
        writeSchematic(schem, File("row8new.schem"))
    }

    "8x8 new" {
        val seq = HipSequences(nObs = 3, nFolds = 2).apply { +Move.t4; fullDoor(8) }.build()
        val schem = toCartSchem(seq, encoding2)
        writeSchematic(schem, File("8x8new.schem"))
    }

    "print 8 length" {
        val seq = HipSequences(nObs = 3, nFolds = 2).apply { fullDoor(8) }.build()
        println(seq.size)
    }

    "print 8" {
        val seq = HipSequences(nObs = 3, nFolds = 2).apply { row(8) }.build()
        println(seq.joinToString("\n"))
    }

    "row9 new only" {
        val seq = HipSequences(nObs = 3, nFolds = 2).apply { row(9) }.build()

        val schem = toCartSchem(seq, encoding2)
        writeSchematic(schem, File("row9new.schem"))
    }

    "9x9 new" {
        val seq = HipSequences(nObs = 3, nFolds = 2).apply { +Move.t4; fullDoor(9) }.build()
        val schem = toCartSchem(seq, encoding2)
        writeSchematic(schem, File("9x9new.schem"))
    }

    "print 9 length" {
        val seq = HipSequences(nObs = 3, nFolds = 2).apply { fullDoor(9) }.build()
        println(seq.size)
    }


    "print 4 through 11 length" {
        for(i in 4..11) {
            val seq = HipSequences(nObs = 3, nFolds = 2).apply { fullDoor(i) }.build()
            println("$i: ${seq.size}")
        }
    }
})