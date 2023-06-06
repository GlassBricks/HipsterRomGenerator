package me.glassbricks.schem

import me.glassbricks.knbt.Nbt

@JvmInline
value class SignalStrength(val ss: Int) {
    init {
        require(ss in 1..15)
    }
}

private val ssToRecord = listOf(
    null,
    "music_disc_13",
    "music_disc_cat",
    "music_disc_blocks",
    "music_disc_chirp",
    "music_disc_far",
    "music_disc_mall",
    "music_disc_mellohi",
    "music_disc_stal",
    "music_disc_strad",
    "music_disc_ward",
    "music_disc_11",
    "music_disc_wait",
    "music_disc_pigstep",
    "music_disc_otherside",
    "music_disc_5",
).map { it?.let { "minecraft:$it" } }

private val ssToCartItems = intArrayOf(
    0,
    1,
    1 * 64 + 60,
    3 * 64 + 55,
    5 * 64 + 51,
    7 * 64 + 46,
    9 * 64 + 42,
    11 * 64 + 37,
    13 * 64 + 32,
    15 * 64 + 28,
    17 * 64 + 23,
    19 * 64 + 19,
    21 * 64 + 14,
    23 * 64 + 10,
    25 * 64 + 5,
    27 * 64,
)

fun SignalStrength.asRecord(): String = ssToRecord[ss]!!
fun SignalStrength.numCartItems(): Int = ssToCartItems[ss]
fun SignalStrength.numCartStacks() = (numCartItems() + 63) / 64

class SSBox(val sses: List<SignalStrength>)

class SSBoxes(val boxes: List<SSBox>)

val nbt = Nbt { encodeDefaults = true }

fun <T> encodeToSignalStrengths(
    moves: List<T>,
    encoding: Map<T, Int>,
): List<SignalStrength> {
    return moves.map { SignalStrength(encoding.getValue(it)) }
}

fun SSBox.toRecordItems(): List<Item> = sses.mapIndexed { index, signalStrength ->
    Item(
        Slot = index.toByte(),
        id = signalStrength.asRecord(),
        Count = 1,
    )
}

fun List<SignalStrength>.padToMinimum(
    minSize: Int,
    waitingMove: SignalStrength?,
): List<SignalStrength> {

    val numToPad = minSize - size
    if (numToPad > 0) {
        waitingMove ?: error("no waiting move provided")
        return this + List(numToPad) { waitingMove }
    }
    return this
}
