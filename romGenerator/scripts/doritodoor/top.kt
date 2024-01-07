@file:Suppress("SpellCheckingInspection")

package doritodoor

import doritodoor.TopSeq.FoldState.*
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
    jworm, tobs, bfold, // bottom fold
    obs, g, e, a, c, caps_lock, f, sto, unsto, tsto1, tsto2, d
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
    private val unsto get() = add(TopMove.unsto)
    private val tsto1 get() = add(TopMove.tsto1)
    private val tsto2 get() = add(TopMove.tsto2)
    private val obs get() = add(TopMove.obs)
    private val tobs get() = add(TopMove.tobs)

    private val wait get() = capsLock()

    private val jworm get() = add(TopMove.jworm)

    private val a get() = lower(TopMove.a)
    private val c get() = lower(TopMove.c)
    private val e get() = lower(TopMove.e)
    private val f get() = lower(TopMove.f)

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


    private fun stoGrab() {
        wait
        lower(TopMove.sto)
    }

    private fun stoPush() {
        wait
        upper(TopMove.sto)
    }

    private fun store(n: Int) {
        when (n) {
            2 -> {
                tsto2
                stoGrab()
                +"oo"
            }
            3, 9 -> {
                tsto1
                tsto2
                stoGrab()
                +"oo"
            }
            in 4..8 -> {
                unsto
                tsto1
                tsto2
                stoGrab()
                +"oo"
            }
            10 -> {
                tsto2
                +"oo"
            }
            11 -> {
                check(elements.removeLast() == TopMove.f)
            }
            else -> error("bad n $n")
        }
    }

    fun closing() {
        stoPush()
        f

        tsto2
        stoGrab()
        f

        tsto1
        tsto2
        stoGrab()
        f

        repeat(5) {
            jworm
            tsto1
            tsto2
            stoGrab()
            f
        }
        tsto1
        tsto2
        stoPush()
        f

        stoGrab()
        wait
        tsto2
        jworm  // fix parity while waiting
        lower(TopMove.sto)
        +"fef"
    }

    private val maxPistonsOut = 6 // for layer 6 (index 5), we cheat a bit
    val pistonOut = BooleanArray(maxPistonsOut)

    // topmost folded pistons (layer 3-5 of pistonOut)
    // state machine!
    private enum class FoldState {
        None, Out1, Out2, FoldOut1, Out3
    }

    private var foldState = None
    private var needForcedFold = false

    private fun addFold() {
        foldState = when (foldState) {
            None -> {
                a
                Out1
            }
            Out1 -> {
                a
                Out2
            }
            FoldOut1 -> {
                +"aaa"
                Out2
            }
            Out2 -> {
                +"aaza"
                Out3
            }
            Out3 -> error("no more folds")
        }
    }

    private fun removeFold() {
        foldState = when (foldState) {
            None -> error("No fold to remove")
            Out1 -> error("Need forced fold")
            FoldOut1 -> {
                A
                None
            }
            Out2 -> {
                A
                FoldOut1
            }
            Out3 -> {
                +"Azaa"
                Out2
            }
        }
    }

    private fun addPistons(layer: Int) {
        // verification
        require(layer in pistonOut.indices)
        val firstOut = pistonOut.indexOfFirst { it }
        if (firstOut != -1 && layer >= firstOut) {
            error("Pistons out bad order. Tried $layer, state ${pistonOut.contentToString()}")
        }
        if (needForcedFold && layer <= 2) {
            needForcedFold = false
            return addPistons(2)
        }
        val isStretch = layer <= 2 && (layer + 1..5).all { pistonOut[it] }
        when (layer) {
            0 -> e // for both normal and stretch
            1 -> {
                if (!isStretch) {
                    // normal extension
                    c
                } else {
                    // stretch extension
                    d // the ONLY lowercase d!
                }
            }
            2 -> {
                if (!pistonOut[3]) {
                    // because of how retraction works, need to fold out 2 before can retract 1
                    // so, we pretend this is layer 3 and force a layer 2 later
                    needForcedFold = true
                    return addPistons(3)
                } else if (!isStretch) {
                    // normal extension
                    addFold()
                } else {
                    // stretch extension
                    c
                }
            }
            3, 4, 5 -> addFold()
            else -> error("")
        }
        pistonOut[layer] = true

    }

    private fun removePistons() {
        val toRetract = pistonOut.indexOfFirst { it }
        if (toRetract == -1) error("No pistons to retract")
        val wasStretch = toRetract <= 2 && (toRetract..5).all { pistonOut[it] }
        if (!wasStretch) {
            for (i in toRetract downTo 0) when (i) {
                0 -> E
                1 -> +"Cd"
                2 -> removeFold()
                3, 4, 5 -> {}
                else -> error("")
            }
        } else when (toRetract) {
            0 -> +"e"
            1 -> +"dE"
            2 -> +"CdE"
            else -> error("bad toRetract $toRetract")
        }

        pistonOut[toRetract] = false
    }


    private var obsDown = false
    private fun ensureObsDown(state: Boolean) {
        check(nObsOut == 1)
        if (obsDown != state) tobs
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
            else -> error("no obs to add")
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

    fun checkStableState() {
        check(pistonOut.none { it })
        check(nObsOut == 0)
        check(foldState == None)
    }

    fun opening(maxRow: Int = 11) {
        // manually do row 1
        +"FEfoo"
        for (i in 2..maxRow) {
            row(i)
            checkStableState()
            store(i)
        }
    }

    fun row(n: Int) {
        require(n >= 0)
        if (n == 0) {
            // base case, spe
            return f
        }
        extend(n)
        retract(n, isPiston = false, isQc = false)
    }

    private fun extend(n: Int, origHeight: Int = n) {
        if (n == 0) {
            // base case, spe
            return f
        }
        addPistons((n - 1) / 2)
        extendWithPistonsUp(n, false, origHeight)
    }

    private fun extendWithPistonsUp(n: Int, pistonsHigh: Boolean, origHeight: Int = n) {
        when {
            n == 1 -> {
                if (pistonOut.all { it }) {
                    // use jank input
                    check(!pistonsHigh && nObsOut == 0 && n == origHeight)
                    jworm
                } else {
                    if (!pistonsHigh) f
                    if (origHeight == 5) +"gg"
                    F
                }
            }
            n == 2 -> {
                if (!pistonsHigh) f
                // already double piston out
            }
            n == 3 -> {
                if (!pistonsHigh) {
                    // double piston out
                    addPistons(0)
                    if ((1..5).all { pistonOut[it] }) {
                        // use jank input instead of more pistons
                        check(nObsOut == 0 && n == origHeight)
                        jworm
                    } else {
                        +"ff"
                    }
                } else {
                    // already double piston out, see pull(2)
                    f
                }
            }
            n == 4 -> {
                // spit pistons instead of obs first; less moves/parity issues
                if (!pistonsHigh || n != origHeight) spit(2)
                addObs()
            }
            n in 5..9 && origHeight != 11 -> {
                if (!pistonsHigh) f
                addObs()
                // for pistonsHigh && n==5, we don't do qc power because of parity issues
                val obsHeight = if (pistonsHigh && n == origHeight && n > 5) n - 4 else n - 3
                extend(obsHeight, origHeight)
            }
            origHeight in 10..11 -> {
                // almost same as n==10..11, but also captures n==7 from 11 extension
                // note n==3 captures
                if (!pistonsHigh) {
                    // we don't have enough obs to do full extension, so spit first
                    spit(n - 2)
                } else check(n == 10)
                addObs()
                extend(n - 4, origHeight)
            }
            else -> TODO()
        }
        if (n == origHeight) when (n) {
            1 -> {}
            2, 3 -> g
            4 -> F
            5, 6 -> repeat(if (!pistonsHigh) 4 else 2) { g } // works also for 6, when obsHeight still 2
            7 -> {
                if (!pistonsHigh) {
                    // obsHeight = 4
                    repeat(4) { F }
                } else {
                    // obsHeight = 3
                    +"gg"
                }
            }
            8 -> {
                if (!pistonsHigh) {
                    // obsHeight = 5
                    +"gg"
                } else {
                    // obsHeight = 4
                    +"FF"
                }
            }
            9 -> {
                if (!pistonsHigh) {
                    repeat(6) { g }
                } else {
                    +"gg"
                }
            }
            10 -> {
                // pistons are high regardless
                +"gg"
            }
            11 -> {
                check(!pistonsHigh)
                repeat(4) { g }
            }
            else -> TODO("pow($n)")
        }
    }

    private fun maybeRetractObs(n: Int, isQc: Boolean) {
        when (n) {
            in 0..3 -> {}
            4 -> removeObs() // obs already up
            in 5..11 -> {
                // exception n==5, not actually qc
                val obsHeight = if (isQc && n > 5 || n >= 10) n - 4 else n - 3
                retract(obsHeight, isPiston = false, isQc = n == 11)
                removeObs()
            }
            else -> TODO("retractObs($n)")
        }
    }

    private fun retract(n: Int, isPiston: Boolean, isQc: Boolean) {
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

        maybeRetractObs(n, isQc)
        // pull topmost piston down
        pull(n - 2, false)
        if (n == 11) {
            // un-jank the pistonOut state
            check(pistonOut[5] && (0..4).none { pistonOut[it] })
            pistonOut[5] = false
            pistonOut[4] = true
        }
        // do previous row, with pistons already up
        extendWithPistonsUp(n - 1, true)
        retract(n - 1, isPiston, true)
    }

    private fun spit(n: Int) {
        pull(n, true)
    }

    fun pull(n: Int, isSpit: Boolean) {
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

        maybeRetractObs(n, false)

        // modified row(n-2)
        val pistonRow = n - 2
        if (pistonRow == 1) {
            // already in double piston state, don't add more pistons
            extendWithPistonsUp(pistonRow, false)
        } else {
            extend(pistonRow)
        }

        retract(pistonRow, isPiston = true, isQc = false)
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
        val seq = TopSeq().apply { opening() }.build()
//        println(seq.joinToString(" "))
        println(seq.size)
        val rom = encodeSimpleChungusRom(seq, encoding, topRomRestrictions)
        val schem = rom.toSchem(cartRotation = 90F).apply { modifyTopRom() }
        schem.writeTo("top-opening")

        tryTransfer("20htriangle/roms")
    }
    "gen closing" {
        val seq = TopSeq().apply { closing() }.build()
//        println(seq.joinToString(" "))
        println(seq.size)
        val rom = encodeSimpleChungusRom(seq, encoding, topRomRestrictions)
        val schem = rom.toSchem(cartRotation = 90F).apply { modifyTopRom() }
        schem.writeTo("top-closing")

        tryTransfer("20htriangle/roms")
    }

    "print row" {
        val seq = TopSeq().apply {
            row(11)
            checkStableState()
        }.build()
        println(seq.joinToString(" "))
        seq.forEach(::println)
    }

    "print pull 3 when jank" {
        val seq = TopSeq().apply {
            for (i in 2..5) pistonOut[i] = true
            pull(3, false)
        }.build()
        println(seq.joinToString(" "))
        seq.forEach(::println)
    }
})
