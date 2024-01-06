@file:Suppress("SpellCheckingInspection")

package doritodoor

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.infinirom.RomRestrictions
import me.glassbricks.infinirom.encodeSimpleChungusRom
import me.glassbricks.infinirom.ordinalEncoding
import me.glassbricks.infinirom.toSchem
import me.glassbricks.knbt.compoundTag
import me.glassbricks.schem.SchemFile
import me.glassbricks.schem.tryTransfer
import me.glassbricks.schem.writeTo
import ogMegafoldHipster.SequenceBuilder


@Suppress("EnumEntryName")
enum class TopMove {
    wormb,
    tobs,
    bfold, // bottom fold
    obs,
    g,
    e,
    a,
    c,
    caps_lock,
    f,
    sto,
    unsto,
    tsto1,
    tsto2,
    d
}

private val encoding = ordinalEncoding<TopMove>(offest = 1)

private val topRomRestrictions = RomRestrictions(
    minCarts = 3,
    minInSomeBox = 10,
    minBoxSize = 3,
    minBoxesPerCart = 1,
)

@Suppress("PrivatePropertyName")
class TopSeq : SequenceBuilder<TopMove>() {

    private val bfold get() = add(TopMove.bfold)
    private val sto get() = add(TopMove.sto)
    private val unsto get() = add(TopMove.unsto)
    private val tsto1 get() = add(TopMove.tsto1)
    private val tsto2 get() = add(TopMove.tsto2)
    private val obs get() = add(TopMove.obs)
    private val tobs get() = add(TopMove.tobs)

    private val wait get() = capsLock()

    private val a get() = lower(TopMove.a)
    private val c get() = lower(TopMove.c)
    private val e get() = lower(TopMove.e)
    private val f get() = lower(TopMove.f)

    private val b get() = add(TopMove.wormb)
    private val d get() = add(TopMove.d)
    private val g get() = add(TopMove.g)

    @get:JvmName("getUpperA")
    private val A get() = upper(TopMove.a)

    @get:JvmName("getUpperC")
    private val C get() = upper(TopMove.c)

    @get:JvmName("getUpperE")
    private val E get() = upper(TopMove.e)

    @get:JvmName("getUpperF")
    private val F get() = upper(TopMove.f)


    private var currentCaps = false
    private fun capsLock() {
        add(TopMove.caps_lock)
        currentCaps = !currentCaps
    }

    private fun ensureState(state: Boolean) {
        if (currentCaps != state) capsLock()
    }

    private fun lower(move: TopMove) {
        ensureState(false)
        add(move)
    }

    private fun upper(move: TopMove) {
        ensureState(true)
        add(move)
    }

    override fun build(): List<TopMove> {
        while (elements.size < topRomRestrictions.minRecordsPerRom) wait
        ensureState(false)
        return super.build()
    }


    operator fun String.unaryPlus() = iterator().let {
        while (it.hasNext()) when (val ch = it.nextChar()) {
            'a' -> a
            'b' -> b
            'c' -> c
            'd' -> d
            'e' -> e
            'f' -> f
            'g' -> g
            'A' -> A
            'C' -> C
            'E' -> E
            'F' -> F
            'o' -> obs
            'O' -> tobs
            'z' -> bfold
            '-' -> wait
            ' ', '\n' -> {}
            else -> error("bad char $ch")
        }

    }

    fun opening(maxRow: Int = 11) {
        // manually do row 1
        +"FEfoo"
        for (i in 2..maxRow) {
            row(i)
            store(i)
            check(pistonOut.none { it })
        }
    }

    private fun stoGrab() {
        wait
        ensureState(false)
        sto
    }

    private fun store(n: Int) {
        when (n) {
            2 -> {
                tsto2
                stoGrab()
                +"oo"
            }
            3 -> {
                tsto1
                tsto2
                stoGrab()
                +"oo"
            }
            4, 5, 6, 7 -> {
                unsto
                tsto1
                tsto2
                stoGrab()
                +"oo"
            }
            else -> TODO("store($n)")
        }
    }

    private val maxPistonsOut = 4
    val pistonOut = BooleanArray(maxPistonsOut)

    private var needForcedPiston2Out = false

    private fun addPistons(layer: Int) {
        // verification
        require(layer in pistonOut.indices)
        val firstOut = pistonOut.indexOfFirst { it }.let { if (it == -1) maxPistonsOut else it }
        if (layer >= firstOut) {
            error("Pistons out bad order. Tried $layer, state ${pistonOut.contentToString()}")
        }
        if (needForcedPiston2Out && layer <= 2) {
            needForcedPiston2Out = false
            return addPistons(2)
        }
        when (layer) {
            0 -> e
            1 -> c
            2 -> {
                // because of how retraction works, first "a" pulse is always layer 3, not 2
                if (!pistonOut[3]) {
                    needForcedPiston2Out = true
                    return addPistons(3)
                }
                a
            }
            3 -> a
            else -> TODO("addPistons($layer)")
        }
        pistonOut[layer] = true

    }

    private fun removePistons() {
        val toRetract = pistonOut.indexOfFirst { it }
        if (toRetract == -1) error("No pistons to retract")
        for (i in toRetract downTo 0) when (i) {
            0 -> E
            1 -> +"Cd"
            2 -> {
                check(!needForcedPiston2Out)
                A
            }
            3 -> {}
            else -> TODO("removePistons($i)")
        }
        pistonOut[toRetract] = false
    }

    private var obsDown = false
    private fun ensureObsDown(state: Boolean) {
        check(nObsOut > 0); if (obsDown != state) {
            tobs
        }
        obsDown = state
    }

    private var nObsOut = 0

    private fun addObs() {
        when (nObsOut) {
            0 -> obs
            1 -> {
                ensureObsDown(true)
                obs
            }
            else -> error("can't add obs")
        }
        nObsOut++
    }

    private fun removeObs() {
        when (nObsOut) {
            0 -> error("no obs to remove")
            1 -> {
                ensureObsDown(false)
                obs
            }
            2 -> obs
        }
        nObsOut--
    }


    fun row(n: Int) {
        require(n >= 0)
        if (n == 0) {
            // base case, spe
            return f
        }
        extend(n)
        retract(n, isPiston = false)
    }

    private fun extend(n: Int, origHeight: Int = n) {
        if (n == 0) {
            // base case, spe
            return f
        }
        // more pistons needed
        addPistons((n - 1) / 2)
        extendWithPistonsUp(n, false, origHeight)
    }

    private fun extendWithPistonsUp(n: Int, pistonsHigh: Boolean, origHeight: Int = n) {
        when (n) {
            1 -> {
                if (!pistonsHigh) f
                F
            }
            2 -> {
                if (!pistonsHigh) f
                // already double piston out
            }
            3 -> {
                if (!pistonsHigh) {
                    // double piston out
                    addPistons(0)
                    +"ff"
                } else {
                    // already double piston out, see pull(2)
                    f
                }
            }
            4 -> {
                // spit pistons instead of obs first; less moves/parity issues
                if (!pistonsHigh || n != origHeight) spit(2)
                addObs()
            }
            else -> {
                if (!pistonsHigh) f
                addObs()
                extend(n - 3, origHeight = n)
            }
        }
        if (n == origHeight) when (n) {
            1 -> {}
            2, 3 -> g
            4 -> F
            5, 6 -> repeat(if (!pistonsHigh) 4 else 2) { g }
            7 -> {
                if (!pistonsHigh) {
                    repeat(4) { F }
                } else {
                    TODO("pow($n, true)")
                }
            }
            else -> TODO("pow($n)")
        }
    }

    private fun maybeRetractObs(n: Int) {
        if (n == 4) {
            // obs already up
            removeObs()
        } else if (n in 5..7) {
            retract(n - 3, isPiston = false)
            removeObs()
        } else if (n > 6) {
            TODO("retractObs($n)")
        }
    }

    private fun retract(n: Int, isPiston: Boolean) {
        require(n >= 0)
        if (n == 0) {
            // base case (spe), do nothing
            return
        }
        if (n == 1) {
            // base case: remove pistons, grab block
            removePistons()
            // optimization depending on if we're retracting pistons
            if (isPiston) F else f
            return
        }

        maybeRetractObs(n)
        // pull topmost piston down
        pull(n - 2, false)
        // do previous row, with pistons already up
        extendWithPistonsUp(n - 1, true)
        retract(n - 1, isPiston)
    }

    private fun spit(n: Int) {
        pull(n, true)
    }

    private fun pull(n: Int, isSpit: Boolean) {
        if (n == 0) {
            // base case, do nothing. entendWithPistonsUp(1) will handle rest
            return
        }
        if (n == 1) {
            // base case, undo extra piston for fake qc power
            extendWithPistonsUp(1, false)
            removePistons()
            return
        }
        extend(n)
        if (n == 2 && !isSpit) {
            // if not spitting, called from retract(4),
            // keep double piston out state
            return
        }

        maybeRetractObs(n)

        // modified row(n-2)
        val pistonRow = n - 2
        if (pistonRow == 1) {
            // already in double piston state, don't add more pistons
            extendWithPistonsUp(pistonRow, false)
        } else {
            extend(pistonRow)
        }
        retract(pistonRow, isPiston = true)

        removePistons()
    }
}


fun SchemFile.modifyTopRom() {
    this.Entities?.forEach {
        it.Pos = listOf(1982.5, 120.0625, 1980.5)
        it.Rotation = floatArrayOf(90f, 0f)
    }
    this.Metadata = compoundTag {
        "WEOffsetX" eq 0
        "WEOffsetY" eq -2
        "WEOffsetZ" eq 0
    }
    this.Palette = mapOf(
        "minecraft:powered_rail[powered=false,shape=north_south,waterlogged=false" to 0
    )
    this.Offset = intArrayOf(1982, 120, 1980)
}

class GenBottom : StringSpec({
    "gen opening" {
        val seq = TopSeq().apply { opening(7) }.build()
        println(seq.joinToString(" "))
        val rom = encodeSimpleChungusRom(seq, encoding, topRomRestrictions)
        val schem = rom.toSchem(cartRotation = 90F).apply { modifyTopRom() }
        schem.writeTo("top-opening")

        tryTransfer("20htriangle/roms")
    }

    "print row" {
        val seq = TopSeq().apply {
            row(7)
            check(pistonOut.none { it })
        }.build()
        println(seq.joinToString(" "))
        seq.forEach(::println)
    }
})
