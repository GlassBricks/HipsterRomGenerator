package me.glassbricks.infinirom

import me.glassbricks.schem.DataVersions
import me.glassbricks.schem.Entity
import me.glassbricks.schem.Item
import me.glassbricks.schem.SchemFile

// up to 1 item per cart

fun SignalStrength.toChestMinecart(): Entity {
    // div 64, rounded up
    val items = List(numCartStacks()) { index ->
        Item(
            Slot = index.toByte(),
            id = "minecraft:netherite_axe",
            Count = 1
        )
    }
    return Entity(
        Id = "minecraft:chest_minecart",
        Items = items,
        Pos = listOf(0.0, 0.0, 0.0),
    )
}

fun List<SignalStrength>.toInifinirom1(): SchemFile {
    val entities = this.map(SignalStrength::toChestMinecart)
    return SchemFile(
        Width = 1,
        Height = 1,
        Length = 1,
        Palette = mapOf("minecraft:air" to 0),
        PaletteMax = 1,
        BlockData = byteArrayOf(0),
        BlockEntities = emptyList(),
        Entities = entities,
        Offset = intArrayOf(0, 0, 0),
        DataVersion = DataVersions.v1_19_4
    )
}
