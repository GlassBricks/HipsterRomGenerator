package mini5x5tnt

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.infinirom.encode
import me.glassbricks.infinirom.ordinalEncoding
import me.glassbricks.infinirom.toInifinirom1
import me.glassbricks.schem.writeTo
import ogMegafoldHipster.SequenceBuilder
import java.io.File

@Suppress("EnumEntryName")
enum class Move {
    none,
    worm,
    a,
    @Suppress("unused")
    wait,
    sto,
    r,
    b,
    c,
    l
}

class B : SequenceBuilder<Move>() {


    val none get() = add(Move.none)
    val worm get() = add(Move.worm)
    val a get() = add(Move.a)

    //    val wait get() = add(Move.wait)
    val sto get() = add(Move.sto)
    val r get() = add(Move.r)
    val b get() = add(Move.b)
    val c get() = add(Move.c)
    val l get() = add(Move.l)


    fun seq() {
        closing()
        none
        opening()
        none
    }

    fun closing() {
        repeat(4) { a; worm }
        b; b
        r; r
    }

    fun opening() {
        val seq = """
        F
        
        bab r
        aa r
        bab
        F
        
        bb r c aa r a c
        bb r c r ac bb r aa r
        bab 
        F
        
        bb r b l bbbb bb l 
        c aa r a c bb r c aa r baba c bb r 
        baaba r b l bb l cac bb r aa r bab 
        F
        
        bb r b l c aaaa aaaa c bb l 
        c bab c b l bb l caa r a c bb r 
        c b aa b a r b c bb r r c aa bbb c 
        a bb r c bb c b l bbbb l c aa c bb a r 
        c bb r a a r baba c bb r baa b a r c bb 
        rr a c bb r aa r b a b rr a
        """

        for (char in seq) when (char) {
            'a' -> a
            'b' -> b
            'c' -> c
            'r' -> r
            'l' -> l
            'F' -> {
                r; r; a; a; sto; sto
            }

            ' ', '\n' -> {}
            else -> error("invalid char '$char'")
        }
    }
}

class Gen : StringSpec({
    "gen" {
        val theSeq = B().apply(B::seq).build()
        val ssSeq = ordinalEncoding<Move>().encode(theSeq)
        val schem = ssSeq.toInifinirom1()
        schem.writeTo(File("mini5x5tnt.schem"))
    }
})
