package me.glassbricks.schem

import me.glassbricks.knbt.compoundTag
import kotlin.math.ceil


val ssToNumItems = intArrayOf(
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

fun getCartWithSs(
    ss: Int,
    pos: List<Double>,
): Entity {
    val numItems = ssToNumItems[ss]
    val numFullStacks = numItems / 64
    val numItemsInLastStack = numItems % 64
    val items = MutableList(numFullStacks) { index ->
        Item(
            Slot = index.toByte(),
            id = "minecraft:netherite_axe",
            Count = 1
        )
    }
    if (numItemsInLastStack != 0) {
        items.add(
            Item(
                Slot = items.size.toByte(),
                id = "minecraft:dragon_head",
                Count = numItemsInLastStack.toByte()
            )
        )
    }
    return Entity(
        Id = "minecraft:chest_minecart",
        Items = items,
        Pos = pos
    )
}

fun <I> toCartSchem(
    items: List<I>,
    encoding: Map<I, Int>,
): SchemFile {
    val cartHeight = 0.7
    val ss = items.map { encoding[it]!! }
    val height = ceil(ss.size * cartHeight).toInt() + 5
    return SchemFile(
        Width = 1,
        Height = height.toShort(),
        Length = 1,
        Palette = mapOf("minecraft:air" to 0),
        PaletteMax = 1,
        BlockData = ByteArray(height) { 0 },
        BlockEntities = emptyList(),
        Entities = ss.mapIndexed { index, ss ->
            getCartWithSs(ss, listOf(0.5, index * cartHeight + 2.5, 0.5))
        },
        Offset = intArrayOf(0, 0, 0),
        DataVersion = DataVersions.v1_19_4
    )
}
