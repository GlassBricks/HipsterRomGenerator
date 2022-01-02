@file:Suppress("PropertyName", "unused")

package me.glassbricks.schem

import kotlinx.serialization.Serializable
import me.glassbricks.knbt.CompoundTag

@Serializable
class SchemFile(
    val Width: Short,
    val Height: Short,
    val Length: Short,

    val Palette: Map<String, Int>,
    val PaletteMax: Int,
    val BlockData: ByteArray,
    val BlockEntities: List<ChestBlockEntity>,
    val Metadata: CompoundTag,
) {
    val Version = 2
    val DataVersion = 2584 // 1.16.4
}

@Serializable
class ChestBlockEntity(
    val Items: List<ChestItem>,
    val Pos: IntArray,
) {
    val Id = "minecraft:chest"
}

@Serializable
class ChestItem(
    val Slot: Byte,
    val id: String,
    val Count: Byte,
    val tag: CompoundTag = CompoundTag.empty,
)

