@file:Suppress("SpellCheckingInspection")

package doritodoor

import doritodoor.TopSeq2.FoldState.*
import io.kotest.core.spec.style.StringSpec
import me.glassbricks.SequenceBuilder
import me.glassbricks.infinirom.encodeSimpleChungusRom
import me.glassbricks.infinirom.toSchem
import me.glassbricks.schem.tryTransfer
import me.glassbricks.schem.writeTo

@Suppress("PrivatePropertyName")
class TopSeq2 : SequenceBuilder<TopMove>() {

    private val fold get() = add(TopMove.fold)
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
    private val f: Unit
        get() {
            check(fLow)
            lower(TopMove.f)
        }

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

    private fun ensureCaps(state: Boolean) {
        if (currentCaps != state) capsLock()
    }

    private fun lower(move: TopMove) {
        ensureCaps(false)
        add(move)
    }

    private fun upper(move: TopMove) {
        ensureCaps(true)
        add(move)
    }

    override fun build(): List<TopMove> {
        while (elements.size < topRomRestrictions.minRecordsPerRom) wait
        ensureCaps(false)
        return super.build()
    }


    operator fun String.unaryPlus() {
        for (ch in this) when (ch) {
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
//            'j' -> jworm
            'o' -> obs
            'O' -> tobs
            'z' -> fold
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

    // topmost folded pistons (layer 2..4 of pistonOut)
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


    private val maxPistonsOut = 6
    private val pistonOut = BooleanArray(maxPistonsOut)
    private val ePresent get() = !pistonOut[1]
    private var fLow = true

    private fun ensureFLow(state: Boolean) {
        if (fLow != state) {
            check(ePresent)
            e
            fLow = state
        }
    }


    private fun addPistons(layer: Int) {
        // verification
        require(layer in pistonOut.indices)
        val firstOut = pistonOut.indexOfFirst { it }
        if (firstOut != -1 && layer >= firstOut) {
            error("Pistons out bad order. Tried $layer, state ${pistonOut.contentToString()}")
        }
        if (needForcedFold && layer == 2) {
            needForcedFold = false
            return addPistons(3)
        }
        fLow = true
        when (layer) {
            0 -> {
                e
                if (!ePresent) fLow = false
            }
            1 -> d
            2 -> c
            3 -> {
                if (!pistonOut[4]) {
                    // because of how retraction works, need to fold out another time before
                    // can retract.
                    // so, we pretend this is layer 4 force a fold later
                    needForcedFold = true
                    return addPistons(4)
                } else {
                    addFold()
                }
            }
            4, 5 -> addFold()
        }
        pistonOut[layer] = true

    }

    private fun removePistons() {
        val toRetract = pistonOut.indexOfFirst { it }
        when (toRetract) {
            -1 -> error("No pistons to retract")
            0 -> {
                if (ePresent) {
                    ensureFLow(true)
                    E
                } else {
                    e
                }
            }
            1 -> +"dE"
            2 -> +"CdE"
            in 3..5 -> {
                removeFold()
                +"CdE"
            }
        }
        fLow = true

        pistonOut[toRetract] = false
    }

    private fun undoRemovePistons0() {
        check(!pistonOut[0])
        if (ePresent) {
            check(currentCaps && elements.removeLast() == TopMove.e)
            if (elements.last() == TopMove.caps_lock) {
                elements.removeLast()
                currentCaps = false
            }
        } else {
            check(!currentCaps && elements.removeLast() == TopMove.e)
        }
        pistonOut[0] = true
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

    private fun fQc() {
        if (ePresent || fLow) {
            ensureFLow(true)
            if (elements.last() == TopMove.f) {
                elements.removeLast()
                F
            } else {
                +"fF"
            }
            fLow = true
        } else {
            jworm
        }
    }

    fun checkStableState() {
        check(pistonOut.none { it })
        check(fLow)
        check(foldState == None)
        check(nObsOut == 0)
    }

    fun opening(maxRow: Int) {
        check(maxRow in 1..11)
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
        extend(n)
        retract(n, isPiston = false, isQc = false)
    }

    private fun extend(
        n: Int,
        pistonsAlreadyOut: Boolean = false,
    ) {
        if (n == 0) {
            // base case, spe
            return f
        }
        extendRecurse(n, pistonsAlreadyOut)
        pow(n, pistonsAlreadyOut)
    }

    private fun extendRecurse(
        n: Int,
        pistonsAlreadyOut: Boolean,
        forceSpit: Boolean = false,
    ) {
        if (n > 0 && !pistonsAlreadyOut) {
            val layer = if (n == 2) 0 else n / 2
            addPistons(layer)
        }
        when {
            n in 0..1 -> {}
            n == 2 -> {
                if (!pistonsAlreadyOut) f
            }
            n == 3 -> {
                // 3 needs one more piston to reach (breaking existing pattern)
                if (!pistonsAlreadyOut) {
                    addPistons(0)
                    fQc()
                } else {
                    // undo from pull(2)
                    undoRemovePistons0()
                }
            }
            n in 5..9 && !forceSpit -> {
                if (!pistonsAlreadyOut) f
                addObs()
                // for n==5, due to parity issues we don't do n-4 extension
                val obsHeight = if (pistonsAlreadyOut && n != 5) n - 4 else n - 3
                extendRecurse(obsHeight, false)
            }
            n in 10..11 || n == 4 || forceSpit -> {
                // do spit also on 4, less parity issues
                // forceSpit for n=7 from n=11 recursion
                if (!pistonsAlreadyOut) {
                    spit(n - 2)
                }
                addObs()
                extendRecurse(n - 4, false, forceSpit = n == 11)
            }
            else -> TODO()
        }
    }

    private fun pow(n: Int, pistonsAlreadyOut: Boolean) {
        // extra pulses so bottom is powered
        when (n) {
            1 -> fQc()
            2, 3 -> g
            4 -> F
            5, 6 -> repeat(if (!pistonsAlreadyOut) 4 else 2) { g }
            7 -> {
                if (!pistonsAlreadyOut) {
                    // obsHeight = 4
                    repeat(4) { F }
                } else {
                    // obsHeight = 3
                    +"gg"
                }
            }
            8 -> {
                if (!pistonsAlreadyOut) {
                    // obsHeight = 5
                    +"gg"
                } else {
                    // obsHeight = 4
                    +"FF"
                }
            }
            9 -> {
                if (!pistonsAlreadyOut) {
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
                check(!pistonsAlreadyOut)
                repeat(4) { g }
            }
            else -> TODO("pow($n)")
        }
    }

    private fun maybeRetractObs(n: Int, isQc: Boolean) {
        when (n) {
            in 0..3 -> {}
            in 4..11 -> {
                val obsHeight = if (
                    n == 4 || isQc && n > 5 || n >= 10) n - 4 else n - 3
                retract(obsHeight, isPiston = false, isQc = n == 11)
                removeObs()
            }
            else -> TODO("retractObs($n)")
        }
    }

    private fun retract(n: Int, isPiston: Boolean, isQc: Boolean) {
        require(n >= 0)
        when (n) {
            0 -> {} // base case (spe), do nothing
            1 -> {
                // base case: remove pistons, grab block
                removePistons()
                // optimization depending on if we're retracting pistons
                if (isPiston) F else f
            }
            else -> {
                maybeRetractObs(n, isQc)
                pull(n - 2)

                // do previous row, with pistons already up
                extend(n - 1, pistonsAlreadyOut = true)
                retract(n - 1, isPiston, isQc = true)
            }
        }
    }

    private fun spit(n: Int) {
        pull(n)
    }

    private fun pull(n: Int) {
        if (n == 0) {
            return f
        }
        extend(n, pistonsAlreadyOut = n == 1)
        maybeRetractObs(n, false)

        // retract the piston
        if (n - 2 >= 0) {
            extend(n - 2, pistonsAlreadyOut = n == 3)
            retract(n - 2, isPiston = true, isQc = false)
        }

        removePistons()
    }
}

private fun getSeq(block: TopSeq2.() -> Unit): List<TopMove> = TopSeq2().apply(block).build()
class GenTop2 : StringSpec({
    "gen opening" {
        val seq = getSeq { opening(11) }
        println(seq.size)
        println(seq.joinToString(" "))
        val rom = encodeSimpleChungusRom(seq, TopMove.encoding, topRomRestrictions)
        val schem = rom.toSchem(cartRotation = 90F).apply { modifyTopRom() }
        schem.writeTo("top-opening")

        tryTransfer("20htriangle/roms")
    }
    "print row" {
        val seq = getSeq {
            row(6)
            checkStableState()
        }
        println(seq.joinToString(" "))
        seq.forEach(::println)
    }
})
