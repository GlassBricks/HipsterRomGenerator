@file:Suppress("SpellCheckingInspection")

package doritodoor

import doritodoor.BotSeq.ObsState.*
import doritodoor.BotSeq.ObsState.TuckState.*
import io.kotest.core.spec.style.FreeSpec
import me.glassbricks.CHEST_MAX
import me.glassbricks.infinirom.RomRestrictions
import me.glassbricks.infinirom.encodeSimpleChungusRom
import me.glassbricks.infinirom.ordinalEncoding
import me.glassbricks.infinirom.toSchem
import me.glassbricks.schem.tryTransfer
import me.glassbricks.schem.writeTo
import ogMegafoldHipster.SimpleSequenceVisitor


@Suppress("EnumEntryName")
enum class BottomMove {
    a,
    bworm,
    tworm,
    tsto,
    ssto,
    e,
    c,
    b,
    f,
    fold,
    bobs,
    unbubble,
    o,
    wait,
    d
}

private val encoding = ordinalEncoding(
    offest = 1, waitingMove = BottomMove.wait
)

private val botRomRestrictions = RomRestrictions(
    minBoxSize = 2,
    minBoxesPerCart = CHEST_MAX,
    minCarts = 1,
)

class BotSeq : SimpleSequenceVisitor<BottomMove>() {

    private val a get() = add(BottomMove.a)
    private val b get() = add(BottomMove.b)
    private val c get() = add(BottomMove.c)
    private val d get() = add(BottomMove.d)
    private val e get() = add(BottomMove.e)
    private val f get() = add(BottomMove.f)
    private val fold get() = add(BottomMove.fold)
    private val o get() = add(BottomMove.o)
    private val bobs get() = add(BottomMove.bobs)
    private val bworm get() = add(BottomMove.bworm)
    private val tworm get() = add(BottomMove.tworm)
    private val tsto get() = add(BottomMove.tsto)
    private val ssto get() = add(BottomMove.ssto)
    private val wait get() = add(BottomMove.wait)


    override fun build(): List<BottomMove> {
        while (elements.size < botRomRestrictions.minRecordsPerRom - 1) add(BottomMove.wait)
        add(BottomMove.unbubble)
        return super.build()
    }

    fun closing() {
        ssto; b
        bworm; wait; ssto; b
        repeat(5) {
            tworm; wait; ssto; b
        }
        tsto; ssto
        +"bcaba"
        tworm
    }

    operator fun String.unaryPlus() = this.iterator().let {
        while (it.hasNext()) when (val ch = it.next()) {
            'a' -> a
            'b' -> b
            'c' -> c
            'd' -> d
            'e' -> e
            'f' -> f
            'o' -> o
            's' -> ssto
            ' ', '\n' -> {}
            '#' ->
                @Suppress("ControlFlowWithEmptyBody")
                while (it.hasNext() && it.next() != '\n'); // comment
            else -> error("invalid char $ch")
        }
    }

    fun opening(maxRow: Int = 9) {
        require(maxRow in 1..9)
        +"abacbboo" // row 1, already extended by closing
        for (i in 2..maxRow) {
            row(i)
            if (i < 9) {
                tsto; o; o
            }
            if (i == 9) {
                // keep floor bock
                check(elements.takeLast(4) == listOf(BottomMove.a, BottomMove.c, BottomMove.b, BottomMove.b))
                repeat(4) { elements.removeLast() }
                c; b
            }
        }
    }

    private val maxPistonsOut = 5
    private val pistonOut = BooleanArray(maxPistonsOut)

    // spawns more pistons, don't sip
    private fun addPistons(layer: Int) {
        // verification
        require(layer in 0..2)
        val firstOut = pistonOut.indexOfFirst { it }.let { if (it == -1) maxPistonsOut else it }
        if (layer >= firstOut) {
            error("Pistons out bad order. Tried $layer, state ${pistonOut.contentToString()}")
        }
        pistonOut[layer] = true

        when (layer) {
            0 -> c
            1 -> d
            2 -> e
            else -> TODO("morePistons($layer)")
        }

    }

    private fun removePistons() {
        val toRetract = pistonOut.indexOfFirst { it }
        if (toRetract == -1) error("No pistons to retract")
        for (i in toRetract downTo 0) when (i) {
            0 -> +"cb"
            1 -> +"dc"
            2 -> +"ed"
            else -> TODO()
        }
        pistonOut[toRetract] = false
    }

    sealed class ObsState {
        data object Out0 : ObsState()

        // when tucked, a storage block is in topmost obs, to be exchanged with obs later
        enum class TuckState {
            None,
            BlockUp,
            Tucked,
        }

        sealed class Tuckable(var tuckState: TuckState = None) : ObsState()

        class Out1 : Tuckable()
        class Out2 : Tuckable()
    }

    private var currentObsState: ObsState = Out0

    private fun addObs() {
        val state = currentObsState
        if (state is Tuckable && state.tuckState != None) error("cannot add obs when tucked")
        currentObsState = when (state) {
            Out0 -> {
                o
                Out1()
            }

            is Out1 -> {
                bobs
                Out2()
            }

            is Out2 -> TODO()
        }
    }

    private fun removeObs() {
        val state = currentObsState
        if (state is Tuckable && state.tuckState == BlockUp) error("block needs to be tucked first")
        currentObsState = when (state) {
            Out0 -> error("No obs to retract")
            is Out1 -> {
                if (state.tuckState == None) o
                else +"obsaoboo" // shuffle obs and storage block back
                Out0
            }

            is Out2 -> error("Must tuck first")
        }
    }

    fun row(n: Int) {
        require(n in -2..9)
        if (n == -2) return // base case
        extend(n, false, origHeight = n)
        retract(n)
    }

    private fun extend(n: Int, pistonsHigh: Boolean, origHeight: Int = n) {
        require(n in -1..9)
        if (n == -1) {
            // single piston extender
            return b
        }
        // more pistons needed
        addPistons(n / 2)
        extendWithPistonsUp(n, pistonsHigh, origHeight)
    }

    private fun blockUp() {
        currentObsState.let {
            check(it is Tuckable && it.tuckState == None)
            it.tuckState = BlockUp
        }
        +"sb"
    }

    private fun tuckBlockUp() {
        currentObsState.let {
            check(it is Tuckable && it.tuckState == BlockUp)
            it.tuckState = Tucked
        }
        +"bo"
    }

    private fun extendWithPistonsUp(n: Int, pistonsHigh: Boolean, origHeight: Int = n) {
        require(n in 0..9)
        if (n > 0 && !pistonsHigh) b // move piston up for power or obs

        if (n in 0..1) return a // directly power top piston
        if (n == 2 && origHeight >= 5) {
            // use a block from storage instead of another observer
            blockUp()
        } else {
            // use more obs
            addObs()
            extend(n - 3, pistonsHigh, origHeight)
        }

        // extra pulses so top piston is powered
        when (n) {
            2 -> b
            3, 4 -> a
            5 -> +"aa" // powered through block
            else -> TODO("extraPulses($n)")
        }
    }

    private fun retract(n: Int) {
        require(n in -1..9)
        if (n == -1) {
            // base case (spe): do nothing
            return
        }
        if (n == 0) {
            // base case, remove pistons, grab block
            removePistons()
            b
            return
        }
        // if block up, tuck it in
        if (n == 2 && currentObsState.let { it is Tuckable && it.tuckState == BlockUp }) {
            check(n % 3 == 2)
            tuckBlockUp()
        } else if (n >= 2) {
            // remove obs
            retract(n - 3)
            removeObs()
        }

        // pull topmost piston one down
        pull(n - 2)

        // do previous row, with pistons already up
        extendWithPistonsUp(n - 1, true)
        retract(n - 1)
    }

    private fun pull(n: Int) {
        require(n in -1..7)
        // full extension, partial retraction
        extend(n, false)
        if (n == -1) {
            // base case (spe), no retraction: do nothing
            return
        }
        // remove obs if exists
        if (n >= 2) {
            retract(n - 3)
            removeObs()
        }
        // remove pistons
        row(n - 2)
        removePistons()
    }


}

class Gen : FreeSpec({
    "gen closing" {
        val seq = BotSeq().apply { closing() }.build()
        println(seq.joinToString(" "))
        val rom = encodeSimpleChungusRom(seq, encoding, botRomRestrictions)
        val schem = rom.toSchem()
        schem.writeTo("bot-closing.schem")
    }

    "gen opening" {
        val seq = BotSeq().apply { opening(5) }.build()
        println(seq.joinToString(" "))
        val rom = encodeSimpleChungusRom(seq, encoding, botRomRestrictions)
        val schem = rom.toSchem()
        schem.writeTo("bot-opening.schem")
    }

    "print row 5" {
        val seq = BotSeq().apply { row(5) }.build()
        println(seq.joinToString(" "))
    }

    "transfer" {
        tryTransfer("20htriangle/roms")
    }
})
