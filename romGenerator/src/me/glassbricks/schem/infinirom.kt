package me.glassbricks.schem

import me.glassbricks.knbt.Nbt

@JvmInline
value class SignalStrength(val ss: Int) {
    init {
        require(ss in 1..15)
    }
}

const val ChestSize = 27

class SSBox(val sses: List<SignalStrength>)
class SSBoxes(val boxes: List<SSBox>)

val nbt = Nbt { encodeDefaults = true }

val ssToRecord = listOf(
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

fun <T> toSignalStrengths(
    moves: List<T>,
    encoding: Map<T, Int>,
): List<SignalStrength> {
    return moves.map { SignalStrength(encoding.getValue(it)) }
}

fun SSBox.toRecordItems(): List<Item> = sses.mapIndexed { index, signalStrength ->
    Item(
        Slot = index.toByte(),
        id = ssToRecord[signalStrength.ss]!!,
        Count = 1,
    )
}
