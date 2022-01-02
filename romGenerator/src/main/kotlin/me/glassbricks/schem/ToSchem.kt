package me.glassbricks.schem

import me.glassbricks.knbt.*
import me.glassbricks.rom.CHEST_MAX_STACKS
import me.glassbricks.rom.ItemStack
import me.glassbricks.rom.ShulkerBox
import me.glassbricks.rom.ShulkerRom

@JvmName("toSchemShulkerRom")
fun ShulkerRom.toSchem(): SchemFile {

    val numChests = size

    return SchemFile(
        Width = 8,
        Height = numChests.toShort(),
        Length = 1,
        Palette = mapOf(
            "minecraft:chest[facing=south,type=right,waterlogged=false]" to 0,
            "minecraft:chest[facing=south,type=left,waterlogged=false]" to 1
        ),
        PaletteMax = 2,
        BlockData = ByteArray(numChests * 2) { (it % 2).toByte() },
        BlockEntities = flatMapIndexed { chestIdx, boxes ->
            boxes.toDoubleChest(chestIdx) { chestSide ->
                intArrayOf(chestSide, chestIdx, 0)
            }
        },
        Metadata = CompoundTag.empty
    )
}

fun ShulkerRom.toSchem2(): SchemFile {

    require(size == 3)

    return SchemFile(
        Width = 8,
        Height = 1,
        Length = 1,
        Palette = mapOf(
            "minecraft:chest[facing=south,type=right,waterlogged=false]" to 0,
            "minecraft:chest[facing=south,type=left,waterlogged=false]" to 1,
            "minecraft:air" to 2
        ),
        PaletteMax = 3,
        BlockData = byteArrayOf(0, 1, 2, 0, 1, 2, 0, 1),
        BlockEntities = flatMapIndexed { chestIdx, boxes ->
            boxes.toDoubleChest(chestIdx) { chestSide ->
                intArrayOf(chestIdx * 3 + chestSide, 0, 0)
            }
        },
        Metadata = compoundTag {
            "WEOffsetX" eq 0
            "WEOffsetY" eq -2
            "WEOffsetZ" eq 0
        }
    )
}

fun List<List<ItemStack>>.toSchem(): SchemFile {
    require(size == 3)

    return SchemFile(
        Width = 8,
        Height = 1,
        Length = 1,
        Palette = mapOf(
            "minecraft:chest[facing=south,type=right,waterlogged=false]" to 0,
            "minecraft:chest[facing=south,type=left,waterlogged=false]" to 1,
            "minecraft:air" to 2
        ),
        PaletteMax = 3,
        BlockData = byteArrayOf(0, 1, 2, 0, 1, 2, 0, 1),
        BlockEntities = flatMapIndexed { chestIdx, items ->
            items.toDoubleChest { chestSide ->
                intArrayOf(chestIdx * 3 + chestSide, 0, 0)
            }
        },
        Metadata = compoundTag {
            "WEOffsetX" eq 0
            "WEOffsetY" eq -2
            "WEOffsetZ" eq 0
        }
    )
}

private fun List<ShulkerBox>.toDoubleChest(
    chestIdx: Int,
    getChestPos: (chestSide: Int) -> IntArray,
): List<ChestBlockEntity> {
    val boxId = boxIds[chestIdx]
    return toDoubleChest(getChestPos) { slot, box ->
        box.toItem(slot, boxId)
    }
}

private fun List<ItemStack>.toDoubleChest(
    getChestPos: (chestSide: Int) -> IntArray,
): List<ChestBlockEntity> {
    val m = Itemizer()
    return toDoubleChest(getChestPos) { slot, stack ->
        m.toItem(slot, stack)
    }
}

private inline fun <T> List<T>.toDoubleChest(
    getChestPos: (chestSide: Int) -> IntArray,
    elementToItem: (slot: Int, element: T) -> ChestItem,
): List<ChestBlockEntity> {
    require(size <= 2 * CHEST_MAX_STACKS) { "Too many items to fit in a double chest" }
    return listOf(
        ChestBlockEntity(
            Items = take(CHEST_MAX_STACKS).mapIndexed(elementToItem),
            Pos = getChestPos(0)
        ),
        ChestBlockEntity(
            Items = drop(CHEST_MAX_STACKS).mapIndexed(elementToItem),
            Pos = getChestPos(1)
        ),
    )

}

// hard coded for now
private val boxIds = arrayOf("blue", "green", "red")
    .map { "minecraft:${it}_shulker_box" }

private const val unstackableId = "minecraft:diamond_horse_armor"
private val stackableIds = listOf(
    "white",
    "orange",
    "magenta",
    "light_blue",
    "yellow",
    "lime",
    "pink",
    "gray",
    "light_gray",
    "cyan",
    "purple",
    "blue",
    "brown",
    "green",
    "red",
    "black"
).run {
    map { "minecraft:${it}_stained_glass" } +
            map { "minecraft:${it}_stained_glass_pane" }
}

private class Itemizer {
    var unstackables = 0

    fun toItem(
        slot: Int,
        itemStack: ItemStack,
    ): ChestItem {
        val (id, count) = when (itemStack.count) {
            0 -> unstackableId to 1
            else -> stackableIds[unstackables++] to itemStack.count
        }
        return ChestItem(
            Slot = slot.toByte(),
            id = id,
            Count = count.toByte()
        )
    }
}

private fun ShulkerBox.toItem(
    slot: Int,
    boxId: String,
): ChestItem {
    val m = Itemizer()

    val items = items.mapIndexed { idx, stack ->
        m.toItem(idx, stack)
            .let { Nbt.encodeToTag(ChestItem.serializer(), it) }
    }
        .let { ListTag(TagType.Compound, it) }

    return ChestItem(
        Slot = slot.toByte(),
        id = boxId,
        Count = 1,
        tag = compoundTag {
            "BlockEntityTag" {
                "Items" eq items
            }
        }
    )
}
