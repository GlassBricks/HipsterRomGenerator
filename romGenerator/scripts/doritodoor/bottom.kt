import io.kotest.core.spec.style.FreeSpec
import me.glassbricks.CHEST_MAX
import me.glassbricks.infinirom.*
import me.glassbricks.schem.writeTo
import ogMegafoldHipster.SimpleSequenceVisitor
import java.io.File


enum class BottomMove {
    a,
    bworm,
    tworm,
    toes,
    soes,
    e,
    c,
    b,
    f,
    fold,
    bobs,
    unbubble,
    sobs,
    wait,
    d
}

val encoding = ordinalEncoding(
    offest = 1, waitingMove = BottomMove.wait
)

private val romRestrictions = RomRestrictions(
    minBoxSize = 1,
    minBoxesPerCart = CHEST_MAX,
    minCarts = 1,
)

class BotSeq : SimpleSequenceVisitor<BottomMove>(
) {

    val a get() = add(BottomMove.a)
    val bworm get() = add(BottomMove.bworm)
    val tworm: Unit get() = add(BottomMove.tworm)
    val toes get() = add(BottomMove.toes)
    val soes get() = add(BottomMove.soes)
    val e get() = add(BottomMove.e)
    val c get() = add(BottomMove.c)
    val b get() = add(BottomMove.b)
    val f get() = add(BottomMove.f)
    val fold get() = add(BottomMove.fold)
    val bobs get() = add(BottomMove.bobs)
    val sobs get() = add(BottomMove.sobs)
    val wait get() = add(BottomMove.wait)
    val d get() = add(BottomMove.d)


    override fun build(): List<BottomMove> {
        while (elements.size < romRestrictions.minRecordsPerRom - 1) add(BottomMove.wait)
        add(BottomMove.unbubble)
        return super.build()
    }

    fun closing() {
        soes; b
        bworm; wait; soes; b
        repeat(5) {
            tworm; wait; soes; b
        }
        toes; soes
        b; c; a; b; a
        tworm
    }

    fun tpeSpit() {

    }

}

class Gen : FreeSpec({
    "gen closing" {
        val seq = BotSeq().apply { closing() }.build()

        println(seq.joinToString(" ") { it.name })
        val rom = encodeSimpleChungusRom(seq, encoding, romRestrictions)
        val schem = rom.toSchem()
        schem.writeTo(File("botclosing.schem"))
    }
})
