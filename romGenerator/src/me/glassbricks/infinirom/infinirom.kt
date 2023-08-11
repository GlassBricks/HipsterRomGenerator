package me.glassbricks.infinirom

import me.glassbricks.CHEST_MAX
import me.glassbricks.knbt.Nbt
import me.glassbricks.schem.Item

@JvmInline
value class SignalStrength(val ss: Int) {
    init {
        require(ss in 0..15)
    }
}

class SSEncoding<T>(
    private val map: Map<T, SignalStrength>,
) : Map<T, SignalStrength> by map {

    constructor(vararg values: Pair<T, Int>) : this(values.toMap().mapValues { SignalStrength(it.value) })

    override fun get(key: T): SignalStrength {
        return map[key] ?: error("No signal strength for $key")
    }


    fun encode(sequence: Iterable<T>): List<SignalStrength> = sequence.map(this::get)
}

inline fun <reified T : Enum<T>> enumEncoding(): SSEncoding<T> {
    return SSEncoding(enumValues<T>().associateWith { SignalStrength(it.ordinal) })
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

/**
 * A box (cart or shulker box) of signal strengths.
 */
class SSBox(sses: List<SignalStrength>) : List<SignalStrength> by sses.toList()

fun fullSSBox(
    sses: List<SignalStrength>,
    waitingMove: SignalStrength,
): SSBox {
    require(sses.size <= CHEST_MAX)

    return sses.toMutableList()
        .apply { while (size < CHEST_MAX) add(waitingMove) }
        .let(::SSBox)
}

fun List<SignalStrength>.chunkToFullBoxes(
    waitingMove: SignalStrength,
): List<SSBox> = chunked(CHEST_MAX) { fullSSBox(it, waitingMove) }


fun SSBox.toRecordItems(): List<Item> = mapIndexed { index, ss ->
    Item(
        Slot = index.toByte(),
        id = ss.asRecord(),
        Count = 1,
    )
}

val nbt = Nbt { encodeDefaults = true }
