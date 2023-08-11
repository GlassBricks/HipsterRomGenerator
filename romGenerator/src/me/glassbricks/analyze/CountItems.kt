package me.glassbricks.analyze

import VarIntIterator
import me.glassbricks.divCeil
import me.glassbricks.infinirom.nbt
import me.glassbricks.knbt.CompoundTag
import me.glassbricks.schem.ChestBlockEntity
import me.glassbricks.schem.DataVersions
import me.glassbricks.schem.Item
import me.glassbricks.schem.SchemFile


class ItemsCount {
    val items = mutableMapOf<String, Int>()

    private val stackSize = mutableMapOf<String, Int>()

    fun itemName(name: String): String? {
        val item = name.substringAfter("minecraft:").substringBefore("[")
        return when {
            item == "air" || item == "piston_head" -> null
            item == "tripwire" -> "string"
            item == "redstone_wire" -> "redstone"
            item == "redstone_wall_torch" -> "redstone_torch"
            item.endsWith("_cauldron") -> "cauldron"
            item.endsWith("boat") -> "oak_$item"
            item.endsWith("_wall_head") -> item.substringBefore("_wall_head") + "_head"
            else -> item
        }

    }

    fun add(item: String, cnt: Int) {
        val name = itemName(item) ?: return
        items[name] = (items[name] ?: 0) + cnt

        if (item.endsWith("wool") || item.endsWith("glass")) {
            stackSize[name] = 64
        } else if (item == "snowball" || item == "ender_pearl" || item == "egg" || item == "armor_stand") {
            stackSize[name] = 16
        } else if (cnt > 1) {
            stackSize[name] = 64
        } else if (name !in stackSize) {
            stackSize[name] = 1
        }
    }

    fun markStackable(item: String) {
        val name = itemName(item) ?: return
        stackSize[name] = 64
    }


    operator fun get(item: String): Int {
        val name = itemName(item) ?: return 0
        return items[name] ?: 0
    }

    fun stackSize(item: String) = stackSize[itemName(item) ?: "?"] ?: 64
    fun numStacks(item: String) = get(item) divCeil stackSize(item)

}

fun countItems(schem: SchemFile): ItemsCount {
    val result = ItemsCount()
    fun countItems(items: List<Item>) {
        for (item in items) {
            result.add(item.id, item.Count.toInt())

            (item.tag?.get("BlockEntityTag") as? CompoundTag)?.get("Items")?.let {
                nbt.decodeFromTag<List<Item>>(it).let(::countItems)
            }
        }
    }
    for (entity in schem.BlockEntities) countItems(entity.Items)
    schem.Entities?.forEach {
        if (it.Items != null) countItems(it.Items)
        result.add(it.Id, 1)
    }
    val reversePalette = schem.Palette.entries.associateBy({ it.value }, { it.key })
    for (it in VarIntIterator(schem.BlockData)) {
        val item = reversePalette[it] ?: continue
        result.add(item, 1)
        result.markStackable(item)
    }
    return result
}


fun toChestsSchem(itemCount: ItemsCount): SchemFile {

    data class ItemCount(val item: String, val count: Int)
    data class Row(val items: List<ItemCount>)
    data class DoubleChest(val rows: List<Row>)

    var curRow = mutableListOf<ItemCount>()
    var curChest = mutableListOf<Row>()
    val chests = mutableListOf<DoubleChest>()

    fun shiftChest() {
        if (curChest.isNotEmpty()) {
            chests.add(DoubleChest(curChest))
            curChest = mutableListOf()
        }
    }

    fun shiftRow() {
        if (curRow.isNotEmpty()) {
            curChest.add(Row(curRow))
            curRow = mutableListOf()
        }
        if (curChest.size == 6) shiftChest()
    }

    fun addStack(stack: ItemCount) {
        curRow.add(stack)
        if (curRow.size == 9) shiftRow()
    }

    fun prepareAdd(nStacks: Int) {
        val wouldOverflowRow = curRow.size + nStacks > 9
        if (wouldOverflowRow) {
            shiftRow()
            val rowsLeft = 6 - curChest.size
            val wouldOverflowChest = nStacks > rowsLeft * 9
            if (wouldOverflowChest) {
                shiftChest()
            }
        }
    }

    itemCount.items.map { (name) ->
        name to (itemCount.numStacks(name) to itemCount[name])
    }.sortedWith(compareBy({ it.second.first }, { it.second.second }))
        .forEach { (item, nStacks) ->
            println(item)
            var (nStacks, nItems) = nStacks
            prepareAdd(nStacks)
            val stackSize = itemCount.stackSize(item)
            while (nItems > 0) {
                val n = minOf(nItems, stackSize)
                addStack(ItemCount(item, n))
                nItems -= n
            }
        }

    // chest representation to block entities
    val blockEntities = mutableListOf<ChestBlockEntity>()

    fun addChest(rows: List<Row>, pos: IntArray) {
        val items = mutableListOf<Item>()
        for ((i, row) in rows.withIndex()) {
            for ((j, item) in row.items.withIndex()) {
                val index = i * 9 + j
                items.add(Item(index.toByte(), item.item, item.count.toByte()))
            }
        }

        blockEntities += ChestBlockEntity(items, pos.copyOf())
    }

    var nChestStacks = 0
    val chestsHeight = 8
    val curPos = intArrayOf(-2, 0, 0)
    for (chest in chests) {
        curPos[1]--
        if (curPos[1] < 0) {
            curPos[1] = chestsHeight - 1
            curPos[0] += 2
            nChestStacks++
        }

        val firstChest = chest.rows.take(3)
        addChest(firstChest, curPos)
        val secondChest = chest.rows.drop(3)
        curPos[0]++
        addChest(secondChest, curPos)
        curPos[0]--
    }

    return SchemFile(
        Width = (nChestStacks * 2).toShort(),
        Height = chestsHeight.toShort(),
        Length = 1,
        Palette = mapOf(
            "minecraft:chest[facing=south,type=left]" to 0,
            "minecraft:chest[facing=south,type=right]" to 1
        ),
        PaletteMax = 2,
        BlockData = ByteArray(nChestStacks * 2 * chestsHeight) { ((it + 1) % 2).toByte() },
        BlockEntities = blockEntities,
        DataVersion = DataVersions.v1_19_4,
    )
}
