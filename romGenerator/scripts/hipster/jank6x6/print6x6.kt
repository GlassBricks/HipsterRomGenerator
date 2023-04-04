package hipster.jank6x6

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
            encoding
        )
        writeSchematic(schem, File("4t.schem"))
    }
    "6x6 schem" {
        val seq = getSequence { seq6() }
        val schem = toCartSchem(seq, encoding)
        writeSchematic(schem, File("6x6.schem"))
    }

    "7x7 schem" {
        val seq = getSequence { seq7() }
        val schem = toCartSchem(seq, encoding)
        writeSchematic(schem, File("7x7.schem"))
    }

    "row7 only schem" {
        val seq = getSequence { row7() }
        val schem = toCartSchem(seq, encoding)
        writeSchematic(schem, File("row7.schem"))
    }

    "print row7 only" {
        val seq = getSequence { row7() }
        println(seq.joinToString("\n"))
    }
})
