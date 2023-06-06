package me.glassbricks.schem

import me.glassbricks.knbt.*


class BinaryEncoding<M>(
    val encoding: Map<M, Int>,
    val numBits: Int,
) {
    init {
        require(numBits in 0 until 32)
    }

    fun encode(seq: Iterable<M>): List<BitSequence> {
        return List(numBits) { bit ->
            seq.map { encoding[it]!! and (1 shl bit) != 0 }
        }
    }

}

@JvmInline
value class ItemStack private constructor(val count: Int) {
    companion object {
        const val MAX_VALUE = 64
        val unstackable get() = ItemStack(0)
        fun stackable(count: Int): ItemStack {
            require(count in 1..MAX_VALUE)
            return ItemStack(count)
        }

    }

    override fun toString(): String = if (count == 0) {
        "##"
    } else {
        "%2d".format(count)
    }
}

fun <M> encodeToItems(sequence: Iterable<M>, encoding: BinaryEncoding<M>): List<List<ItemStack>> =
    encoding.encode(sequence).map {
        buildList {
            var curStack = 0 // 0 == last was unstackable
            for (bit in it) {
                if (bit) {
                    if (curStack != 0) add(ItemStack.stackable(curStack))
                    curStack = 0
                    add(ItemStack.unstackable)
                } else {
                    curStack++
                    if (curStack > ItemStack.MAX_VALUE) {
                        add(ItemStack.stackable(ItemStack.MAX_VALUE))
                        curStack -= ItemStack.MAX_VALUE
                    }
                }
            }
            if (curStack != 0) add(ItemStack.stackable(curStack))
        }
    }


typealias BitSequence = Iterable<Boolean>

const val CHEST_MAX_STACKS = 27

data class ShulkerBox(val items: List<ItemStack>) {
    override fun toString(): String = items.joinToString(separator = ",", prefix = "Box(", postfix = ")")

    companion object {
        const val MAX_STACKS = CHEST_MAX_STACKS
        const val MIN_STACKS = 3 // actually min items but 1 stack at least 1 item

    }
}


fun toShulkerBoxes(stacks: List<ItemStack>): List<ShulkerBox> {
    val boxes = stacks.chunked(ShulkerBox.MAX_STACKS).mapTo(mutableListOf(), ::ShulkerBox)
    // ensure last box has minimum amount items
    val last = boxes.last()
    val lastCount = last.items.count()
    if (lastCount < ShulkerBox.MIN_STACKS) {
        val toMove = ShulkerBox.MIN_STACKS - lastCount
        val secondToLast = boxes[boxes.lastIndex - 1]
        val newSecondToLast = secondToLast.items.dropLast(toMove)
        val newLast = secondToLast.items.takeLast(toMove) + last.items

        boxes.apply {
            set(lastIndex - 1, ShulkerBox(newSecondToLast))
            set(lastIndex, ShulkerBox(newLast))
        }
    }
    return boxes
}

class ShulkerRom(
    val channels: List<List<ShulkerBox>>
) {
    override fun toString(): String {
        return channels.toString()
    }
}

//fun <M : RsInput> Iterable<M>.toRom(encoding: Encoding<M>): ShulkerRom = toRom(this, encoding)

fun <M> toRom(sequence: Iterable<M>, encoding: BinaryEncoding<M>): ShulkerRom = ShulkerRom(
    encodeToItems(sequence, encoding).map(::toShulkerBoxes)
)

fun stackedChests(rom: ShulkerRom): SchemFile {

    val numChests = rom.channels.size

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
        BlockEntities = rom.channels.flatMapIndexed { chestIdx, boxes ->
            boxes.toDoubleChest(chestIdx) { chestSide ->
                intArrayOf(chestSide, chestIdx, 0)
            }
        },
        Metadata = CompoundTag.empty
    )
}

fun schem1(rom: ShulkerRom): SchemFile {
    require(rom.channels.size == 3)

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
        BlockEntities = rom.channels.flatMapIndexed { chestIdx, boxes ->
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

fun noBoxesSchem(channels: List<List<ItemStack>>): SchemFile {
    require(channels.size == 3)

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
        BlockEntities = channels.flatMapIndexed { chestIdx, items ->
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
    elementToItem: (slot: Int, element: T) -> Item,
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
    ): Item {
        val (id, count) = when (itemStack.count) {
            0 -> unstackableId to 1
            else -> stackableIds[unstackables++] to itemStack.count
        }
        return Item(
            Slot = slot.toByte(),
            id = id,
            Count = count.toByte()
        )
    }
}

private fun ShulkerBox.toItem(
    slot: Int,
    boxId: String,
): Item {
    val m = Itemizer()

    val items = items.mapIndexed { idx, stack ->
        m.toItem(idx, stack)
            .let { Nbt.encodeToTag(Item.serializer(), it) }
    }
        .let { ListTag(TagType.Compound, it) }

    return Item(
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
