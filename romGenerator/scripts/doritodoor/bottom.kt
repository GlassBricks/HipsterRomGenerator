@file:Suppress("SpellCheckingInspection")

package doritodoor

import doritodoor.BotSeq.StorageBlockState.*
import io.kotest.core.spec.style.FreeSpec
import me.glassbricks.CHEST_MAX
import me.glassbricks.infinirom.RomRestrictions
import me.glassbricks.infinirom.encodeSimpleChungusRom
import me.glassbricks.infinirom.ordinalEncoding
import me.glassbricks.infinirom.toSchem
import me.glassbricks.schem.tryTransfer
import me.glassbricks.schem.writeTo
import ogMegafoldHipster.SequenceBuilder


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
    obs,
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

class BotSeq : SequenceBuilder<BottomMove>() {

    private val a get() = add(BottomMove.a)
    private val b get() = add(BottomMove.b)
    private val c get() = add(BottomMove.c)
    private val d get() = add(BottomMove.d)
    private val e get() = add(BottomMove.e)
    private val f get() = add(BottomMove.f)
    private val fold get() = add(BottomMove.fold)
    private val o get() = add(BottomMove.obs)
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


    operator fun String.unaryPlus() = this.iterator().let {
        while (it.hasNext()) when (val ch = it.nextChar()) {
            'a' -> a
            'b' -> b
            'c' -> c
            'd' -> d
            'e' -> e
            'f' -> f
            'o' -> o
            'O' -> bobs
            's' -> {
                wait; ssto
            }
            '-' -> wait
            'F' -> fold
            ' ', '\n' -> {}
            '#' ->
                @Suppress("ControlFlowWithEmptyBody")
                while (it.hasNext() && it.next() != '\n'); // comment
            else -> error("invalid char $ch")
        }
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
    fun opening(maxRow: Int = 9) {
        require(maxRow in 1..9)
        +"abacbboo" // row 1, already extended by closing
        for (i in 2..maxRow) {
            row(i)
            if (i < 9) {
                tsto; o; o
            } else {
                // put floor block
                pull(0)
            }
        }
    }


    private val maxPistonsOut = 5
    private val pistonOut = BooleanArray(maxPistonsOut)

    private fun addPistons(layer: Int) {
        // verification
        require(layer in pistonOut.indices)
        val firstOut = pistonOut.indexOfFirst { it }.let { if (it == -1) maxPistonsOut else it }
        if (layer >= firstOut) {
            error("Pistons out bad order. Tried $layer, state ${pistonOut.contentToString()}")
        }
        pistonOut[layer] = true

        when (layer) {
            0 -> c
            1 -> d
            2 -> e
            3 -> f
            4 -> +"fF"
        }

    }

    private fun removePistons() {
        val toRetract = pistonOut.indexOfFirst { it }
        if (toRetract == -1) error("No pistons to retract")
        for (i in toRetract downTo 0) when (i) {
            0 -> +"cb"
            1 -> +"dc"
            2 -> +"ed"
            3 -> +"fe"
            4 -> +"F"
        }
        pistonOut[toRetract] = false
    }

    // when tucked, a storage block is in topmost obs, to be exchanged with obs later
    enum class StorageBlockState {
        None,
        BlockUp,
        Tucked,
    }

    private var sBlockState: StorageBlockState = None
    private var numObsOut = 0


    private fun addObs() {
        check(sBlockState != BlockUp)
        when (numObsOut++) {
            0 -> o
            1 -> if (sBlockState == None) {
                +"Oo"
            } else {
                +"oOo"
            }
            2 -> if (sBlockState == None) {
                +"sbabo"
            } else {
                TODO("addObs(2), tucked")
            }
            // only used during 9th block
            else -> {
                println(elements.takeLast(20))
                TODO("addObs($numObsOut)")
            }
        }
        sBlockState = None
    }

    private fun removeObs() {
        check(sBlockState != BlockUp)
        if (ignoreNextRemoveObs2) {
            check(sBlockState == Tucked)
            ignoreNextRemoveObs2 = false
            return
        }

        val oldState = sBlockState
        sBlockState = None
        when (numObsOut--) {
            0 -> error("No obs to retract")
            1 -> {
                if (oldState == None) o
                else +"obsaoboo"
                // shuffle obs and storage block back
                // a bunch of waiting moves because reasons
            }
            2 -> {
                if (oldState == None) {
                    +"sbao"
                } else {
                    +"obsaoao"
                }
                sBlockState = Tucked
            }
            3 -> {
                if (oldState == None) {
                    +"sbabo"
                } else {
                    error("Should not tuck with 3 obs out")
                }
                sBlockState = Tucked
            }
            else -> TODO("removeObs($numObsOut)")
        }
    }

    fun row(n: Int) {
        require(n in -2..9)
        if (n == -2) return // base case
        extend(n)
        retract(n)
    }

    private fun extend(n: Int, origHeight: Int = n) {
        require(n in -1..9)
        if (n == -1) {
            // single piston extender
            return b
        }
        // more pistons needed
        addPistons(n / 2)
        extendWithPistonsUp(n, false, origHeight)
    }

    private fun blockUp() {
        check(sBlockState == None)
        sBlockState = BlockUp
        +"sb"
    }

    private fun tuckBlockUp() {
        check(sBlockState == BlockUp)
        sBlockState = Tucked
        +"bo"
    }

    private var ignoreNextRemoveObs2 = false

    private fun extendWithPistonsUp(n: Int, pistonsHigh: Boolean, origHeight: Int = n) {
        require(n in 0..9)
        if (n > 0 && !pistonsHigh) b // move piston up for power or obs

        when {
            n in 0..1 -> {
                // directly power top piston
                if (n == origHeight) a
            }
            n == 2 && origHeight >= 5 -> blockUp() // use a block from storage instead of another observer
            n == 2 && origHeight == 2 && sBlockState == Tucked -> {
                // use tucked block to power
                when (numObsOut) {
                    1 -> +"oObao"
                    2 -> +"obaooabo"
                    else -> error("bad numObsOut")
                }
                ignoreNextRemoveObs2 = true
            }
            else -> {
                // use more obs
                addObs()
                extend(n - 3, origHeight) // extra pulses so top piston is powered
                if (n == origHeight) when (n) {
                    2 -> b // 1st power by extend(-1 )
                    3, 4, 5 -> +"aa" // powered through block
                    6 -> if (!pistonsHigh) +"abbaaa" else +"bba"
                    7, 8 -> repeat(if (pistonsHigh) 4 else 8) { a }
                    9 -> {
                        check(!pistonsHigh)
                        +"abbbbaaa"
                    }
                    else -> {
                        println(elements.takeLast(20))
                        TODO("extraPulses($n)")
                    }
                }
            }
        }
    }

    private fun maybeRetractObs(n: Int) {
        if (n == 2 && sBlockState == BlockUp) {
            // if block up, tuck it in
            check(n % 3 == 2)
            tuckBlockUp()
        } else if (n >= 2) {
            // remove obs
            retract(n - 3)
            removeObs()
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
        maybeRetractObs(n)

        // pull topmost piston one down
        pull(n - 2)

        // do previous row, with pistons already up
        extendWithPistonsUp(n - 1, true)
        retract(n - 1)
    }

    private fun pull(n: Int) {
        require(n in -1..7)
        // full extension, partial retraction
        extend(n)
        if (n == -1) {
            // base case (spe), no retraction: do nothing
            return
        }
        maybeRetractObs(n)

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
        val seq = BotSeq().apply { opening(9) }.build()
        println(seq.joinToString(" "))
        val rom = encodeSimpleChungusRom(seq, encoding, botRomRestrictions)
        val schem = rom.toSchem()
        schem.writeTo("bot-opening.schem")

        tryTransfer("20htriangle/roms")
    }

    "print row " {
        val seq = BotSeq().apply { row(7) }.build()
        println(seq.joinToString(" "))
        seq.forEach(::println)
    }

    "transfer" {
        tryTransfer("20htriangle/roms")
    }
})
