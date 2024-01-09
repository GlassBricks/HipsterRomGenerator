package ogMegafoldHipster

import ogMegafoldHipster.Move.*
import me.glassbricks.BinaryEncoding
import me.glassbricks.splitCamelCase
import javax.sound.midi.MidiSystem.getSequence
import kotlin.math.abs

enum class Move {
    MorePistons, ClearPistons, MoreObs, ClearObs, Store, Spe, Dpe, Tpe;

    override fun toString(): String = name.splitCamelCase()
}

private typealias B = RsSequenceVisitor<Move>


private val B.morePistons: Unit get() = add(MorePistons)
private val B.clearPistons get() = add(ClearPistons)
val B.moreObs get() = add(MoreObs)
private val B.clearObs get() = add(ClearObs)
private val B.store get() = add(Store)
private val B.spe get() = add(Spe)
private val B.dpe get() = add(Dpe)
//private val B.tpe get() = add(Tpe)


private fun B.pe(n: Int): Unit = when (n) {
    1 -> add(Spe)
    2 -> add(Dpe)
    3 -> add(Tpe)
    else -> throw UnsupportedOperationException()
}


/** Does the entire sequence for a particular glass hipster door of height N. */
val glassSequence by fn { n ->
    //dpe already happened
    if (n % 2 == 1) { // fix floor block
        dpe
        store
        dpe
    }

    for (i in 1..n) {
        if (n == 1) {
            // dpe already happened
            store
        } else {
            row(i)
        }
    }
}

val normalSequence by fn { n ->
    for (i in 1..n) {
        row(i)
    }
}


/** Does the ENTIRE sequence for row n, including store */
val row by fn { n ->
    full(n)
    store
}

/** Pulls row n all the way down. */
fun B.full(n: Int) {
    require(n in 1..11)
    extend(n)
    retract(n)
}

/**
 * [full], but for n>=3 and a layer of pistons already are deployed.
 * Also used for special case n=6,7.
 */
fun B.fullWithMorePistons(n: Int) {
    require(n in 3..11)
    if (n in 6..7) {
        extendWithMorePistons(-n)
    } else {
        extendWithMorePistons(n)
    }
    retract(n)
}

/** Grabs or spits a row at height n, leaves in piston-observer-stack state */
fun B.extend(n: Int) {
    require(n in 0..11)
    if (n in 0..2) {
        pe(n + 1)
    } else {
        morePistons
        extendWithMorePistons(n)
    }
}

/**
 * [extend] but for n>=3 when pistons already have been added.
 * n = -6 or -7 are special cases.
 */
val extendWithMorePistons: RsSequenceFn<Move, Int> by fn { rn ->
    require(rn in 3..11 || rn == -6 || rn == -7)
    val n = abs(rn)

    moreObs
    // kick the obs out
    if (n - 3 in 0..2) {
        pe(n - 3 + 1)
    } else {
        // another layer of pistons
        dpe
        morePistons
        extendWithMorePistons(n - 3)
    }

    // Additional pulses so that top piston is actually powered
    // 1pe is actually 2 1pe's, since 2pe and 3pe spit but 1pe doesn't
    when (rn) {
        3 -> Unit // already down in [extend]
        4, 5 -> pe(n - 2) // special case n=6: FOUR 1pes because of floor powering
        6 -> repeat(2) { spe }
        // specialer cases for pistons already in air; results in weirdness
        -6 -> repeat(3) { spe }
        -7 -> repeat(6) { dpe }
        7, 8 -> repeat(2) { pe(n - 5) }
        9 -> repeat(6) { spe }
        10, 11 -> repeat(12) { pe(n - 8) }
    }
}

/** After [extend] (at the piston-observer-stack state), retracts everything, including the top grabbed block */
val retract: RsSequenceFn<Move, Int> by fn { n ->
    require(n in 0..11)
    if (n in 0..2) return@fn

    // remove obs
    retract(n - 3)

    // MAYBE MAYBE NOT OPTIMIZATION
    if (n < 5)
        clearObs

    // pull pistons down
    pull(n - 2)
    if (n == 3) { // base case 2: remove layer of pistons
        clearPistons
//        if (isSpecial6x6) {
//            tpe
//        } else {
//            // would have been tpe, but special case due to floor powering
//        }
        dpe
    } else {
        // row n is now 1 block down, and pistons have been pulled
        // do sequence for previous row
        fullWithMorePistons(n - 1)
    }
}

/** From empty, pulls pistons at row n at least 1 block down */
val pull by fn { n ->
    require(n in 1..11)
    if (n in 0..2) {
        // simply grab pistons
        pe(n + 1)
        return@fn
    }

    // another layer of pistons, pulls top pistons
    morePistons
    extendWithMorePistons(n)

    // remove obs
    retract(n - 3)
    clearObs

    // remove below pistons
    full(n - 2)
    clearPistons
}

fun getSequence(block: B.() -> Unit): List<Move> = SimpleSequenceVisitor<Move>().apply(block).build()

val normal6x6Sequence = getSequence {
    for (i in 1..5) {
        row(i)
    }
    full(6)
    moreObs
}

val special6x6Sequence = normal6x6Sequence.map {
    when (it) {
        Store -> ClearPistons
        else -> it
    }
}

val glassHipster11Encoding = BinaryEncoding(
    mapOf(
        MorePistons to 0b011,
        ClearPistons to 0b010,
        MoreObs to 0b100,
        ClearObs to 0b110,
        Store to 0b111,
        Spe to 0b101,
        Dpe to 0b000,
        Tpe to 0b001
    ), 3
)
