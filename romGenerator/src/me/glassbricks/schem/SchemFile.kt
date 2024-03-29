@file:Suppress("PropertyName", "unused")

package me.glassbricks.schem

import kotlinx.serialization.Serializable
import me.glassbricks.knbt.CompoundTag

@Serializable
class SchemFile(
    val Width: Short,
    val Height: Short,
    val Length: Short,

    var Palette: Map<String, Int>,
    val PaletteMax: Int,
    val BlockData: ByteArray,
    val BlockEntities: List<ChestBlockEntity>,
    var Metadata: CompoundTag? = null,
    val DataVersion: Int = DataVersions.v1_16_4,
    val Entities: List<Entity>? = null,
    var Offset: IntArray? = null,
) {
    val Version = 2
}

object DataVersions {
    const val v1_16_4 = 2584
    const val v1_19_4 = 3337
}

@Serializable
class ChestBlockEntity(
    val Items: List<Item> = emptyList(),
    val Pos: IntArray,
) {
    val Id = "minecraft:chest"
}

@Serializable
class Item(
    val Slot: Byte,
    val id: String,
    val Count: Byte,
    val tag: CompoundTag? = null,
)

@Serializable
class Entity(
    val Id: String,
    var Pos: List<Double>,
    val Air: Short = 300,
    val FallDistance: Float = 0.0f,
    val Fire: Short = -1,
    val Invulnerable: Boolean = false,
    val Items: List<Item>? = null,
    val Motion: DoubleArray = doubleArrayOf(0.0, 0.0, 0.0),
    val OnGround: Boolean = false,
    val PortalCooldown: Int = 0,
    var Rotation: FloatArray = floatArrayOf(0.0f, 0.0f),
    val UUID: IntArray? = null,
)
