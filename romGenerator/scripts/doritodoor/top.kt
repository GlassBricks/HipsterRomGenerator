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
    val unsto get() = add(TopMove.unsto)
    val tsto1 get() = add(TopMove.tsto1)
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
            else -> {
                TODO("store($n)")
            }
        }
    }

    private val maxPistonsOut = 2
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
            0 -> e
            1 -> c
            else -> TODO("addPistons($layer)")
        }

    }

    private fun removePistons() {
        val toRetract = pistonOut.indexOfFirst { it }
        if (toRetract == -1) error("No pistons to retract")
        for (i in toRetract downTo 0) when (i) {
            0 -> E
            else -> TODO("removePistons($i)")
        }
        pistonOut[toRetract] = false
    }


    fun row(n: Int) {
        require(n >= 1)
        extend(n)
        retract(n)
    }

    private fun extend(n: Int) {
        require(n > 0)
        // more pistons needed
        var layer = (n - 1) / 2
        addPistons(layer)
        extendWithPistonsUp(n, false)
    }

    private fun extendWithPistonsUp(n: Int, pistonsHigh: Boolean) {
        when (n) {
            1 ->  // fake qc power
                if (pistonsHigh) {
                    // this is only called from retract(2),
                    // piston already at level 0
                    F
                } else {
                    +"fF"
                }
            2 -> +"fg" // tpe
            else -> TODO("extend($n)")
        }
    }

    private fun retract(n: Int) {
        require(n >= 1)
        if (n == 1) {
            // base case: remove pistons, grab block
            removePistons()
            f
            return
        }
        // pull topmost piston down
        pull(n - 2)
        // do previous row, with pistons already up
        extendWithPistonsUp(n - 1, true)
        retract(n - 1)
    }

    private fun pull(n: Int) {
        if (n == 0) {
            // base case, do nothing. entendWithPistonsUp(1) will handle rest
            return
        }
        extend(n)
        TODO("pull($n)")
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
        val seq = TopSeq().apply { opening(2) }.build()
        println(seq.joinToString(" "))
        val rom = encodeSimpleChungusRom(seq, encoding, topRomRestrictions)
        val schem = rom.toSchem(cartRotation = 90F).apply { modifyTopRom() }
        schem.writeTo("top-opening")

        tryTransfer("20htriangle/roms")
    }
})
