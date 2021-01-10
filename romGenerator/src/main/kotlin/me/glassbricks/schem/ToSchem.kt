package me.glassbricks.schem

import me.glassbricks.knbt.*
import me.glassbricks.rom.BoxList
import me.glassbricks.rom.CHEST_MAX_STACKS
import me.glassbricks.rom.ItemStack
import me.glassbricks.rom.ShulkerBox

fun List<BoxList>.toSchem(): SchemFile {

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
                boxes.toChests(chestIdx) { chestSide, idx ->
                    intArrayOf(chestSide, idx, 0)
                }
            },
            Metadata = CompoundTag.empty
    )
}

fun List<BoxList>.toSchem2(): SchemFile {

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
                boxes.toChests(chestIdx) { chestSide, idx ->
                    intArrayOf(idx * 3 + chestSide, 0, 0)
                }
            },
            Metadata = compoundTag {
                "WEOffsetX" eq 0
                "WEOffsetY" eq -2
                "WEOffsetZ" eq 0
            }
    )
}

private fun BoxList.toChests(
        chestIdx: Int,
        getChestLoc: (chestSide: Int, chestIdx: Int) -> IntArray,
): List<ChestBlockEntity> =
        listOf(
                makeChest(take(CHEST_MAX_STACKS), chestIdx, getChestLoc(0, chestIdx)),
                makeChest(drop(CHEST_MAX_STACKS), chestIdx, getChestLoc(1, chestIdx)),
        )

private fun makeChest(
        boxes: BoxList,
        chestIdx: Int,
        loc: IntArray,
) = ChestBlockEntity(
        boxes.mapIndexed { slot, box ->
            box.toItem(slot, boxIds[chestIdx])
        },
        loc
)

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
).map { "minecraft:${it}_stained_glass" }

private fun ShulkerBox.toItem(
        slot: Int,
        boxId: String,
): ChestItem {
    var unstackables = 0

    fun ItemStack.getItem(
            pos: Int,
    ): ChestItem {
        val (id, count) = when (this) {
            ItemStack.Unstackable -> unstackableId to 1
            is ItemStack.Stackable -> stackableIds[unstackables++] to this.count
        }
        return ChestItem(pos.toByte(), id, count.toByte())
    }

    val items = items.mapIndexed { idx, stack ->
        stack.getItem(idx)
                .let { Nbt.encodeToTag(ChestItem.serializer(), it) }
    }.let { ListTag(TagType.Compound, it) }

    val tag = compoundTag {
        "BlockEntityTag" {
            "Items" eq items
        }
    }
    return ChestItem(
            slot.toByte(),
            boxId,
            1,
            tag
    )
}

