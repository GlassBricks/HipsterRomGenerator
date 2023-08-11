package me.glassbricks.infinirom

import me.glassbricks.CHEST_MAX
import me.glassbricks.divCeil
import me.glassbricks.knbt.compoundTag
import me.glassbricks.schem.DataVersions
import me.glassbricks.schem.Entity
import me.glassbricks.schem.Item
import me.glassbricks.schem.SchemFile

// up to 27*27 = 729 items per cart

class BoxCart(val boxes: List<SSBox>)
class ChungusRom(val carts: List<BoxCart>)

fun <T> simpleChungusRom(
    moves: List<T>,
    encoding: SSEncoding<T>,
    waitingMove: T? = null,
): ChungusRom {
    val ss = encoding.encode(moves)
    val waitingSS = waitingMove?.let { encoding[waitingMove] }
    return simpleChungusRom(ss, waitingSS)
}

fun <T> waitOptimizedChungusRom(
    moves: List<T>,
    encoding: SSEncoding<T>,
    waitingMove: T,
): ChungusRom {
    val ss = encoding.encode(moves)
    val waitingSS = encoding[waitingMove]
    return waitOptimizedChungusRom(ss, waitingSS)
}


const val MinBoxSize = 3
const val MinInLastBox = 10
const val MinRecordsPerCart = (MinBoxSize * (CHEST_MAX - 1)) + MinInLastBox
const val MaxRecordsPerCart = CHEST_MAX * CHEST_MAX
const val MinCarts = 3
const val MinRecordsPerRom = MinRecordsPerCart * MinCarts

private fun partitionWithRestrictions(
    numItems: Int,
    numPartitions: Int,
    minPerPartition: Int,
    maxPerPartition: Int,
): List<Int> = buildList {
    require(numItems in (minPerPartition * numPartitions)..(maxPerPartition * numPartitions))

    var remaining = numItems

    for (i in 0 until numPartitions) {
        val numLater = numPartitions - (i + 1)
        val minLater = numLater * minPerPartition
        val maxForThis = remaining - minLater
        val toAdd = minOf(maxForThis, maxPerPartition)
        add(toAdd)
        remaining -= toAdd
    }

    require(remaining == 0)
    forEach { require(it in minPerPartition..maxPerPartition) }
}

private fun partitionRecordsToCarts(numRecords: Int): List<Int> {
    require(numRecords >= MinRecordsPerRom)
    val maxPerCart = CHEST_MAX * CHEST_MAX
    val numCarts = numRecords.divCeil(maxPerCart).coerceAtLeast(MinCarts)

    return partitionWithRestrictions(
        numItems = numRecords,
        numPartitions = numCarts,
        minPerPartition = MinRecordsPerCart,
        maxPerPartition = MaxRecordsPerCart,
    )
}

private fun partitionRecordsToShulkerBoxes(numRecords: Int): List<Int> {
    require(numRecords >= MinRecordsPerCart)
    return partitionWithRestrictions(
        numItems = numRecords,
        numPartitions = CHEST_MAX,
        minPerPartition = MinBoxSize,
        maxPerPartition = CHEST_MAX,
    )
}


fun simpleChungusRom(
    ss: List<SignalStrength>,
    waitingMove: SignalStrength? = null,
): ChungusRom {
    val actualSS =
        if (ss.size >= MinRecordsPerRom) {
            ss
        } else {
            waitingMove ?: error("Cannot pad to minimum without waiting move")
            ss.toMutableList().apply {
                repeat(MinRecordsPerRom - ss.size) { add(waitingMove) }
            }
        }

    var iRom = 0
    val cartSizes = partitionRecordsToCarts(actualSS.size)
    val carts = cartSizes.map { numInCart ->
        val cartSS = actualSS.subList(iRom, iRom + numInCart)
        iRom += numInCart

        var iCart = 0
        val boxes = partitionRecordsToShulkerBoxes(numInCart).map { numInBox ->
            val boxSS = cartSS.subList(iCart, iCart + numInBox)
            iCart += numInBox
            SSBox(boxSS)
        }
        BoxCart(boxes)
    }

    return ChungusRom(carts)
}

fun waitOptimizedChungusRom(
    ss: List<SignalStrength>,
    waitingMove: SignalStrength,
): ChungusRom {
    require(ss.size > MinRecordsPerRom)
    val maxPerCart = CHEST_MAX * CHEST_MAX
    val numCarts = ss.size.divCeil(maxPerCart).coerceAtLeast(MinCarts)
    val numBoxes = numCarts * CHEST_MAX

    // greedily divide boxes whenever there's a waiting move, with (at least min size + 1) for each box
    fun initialDivision(
        ss: List<SignalStrength>,
        minBoxSize: Int,
    ): MutableList<List<SignalStrength>> {
        val initialDivision = mutableListOf<List<SignalStrength>>()

        var nextStart = 0
        for (endPoint in 0 until (ss.size - minBoxSize)) {
            // endpoint is inclusive
            val cutSize = endPoint - nextStart + 1
            if (ss[endPoint] == waitingMove && cutSize >= minBoxSize) {
                // cut from lastIndex to index, to index included
                initialDivision.add(ss.subList(nextStart, endPoint + 1))
                nextStart = endPoint + 1
            }
        }
        initialDivision.add(ss.subList(nextStart, ss.size))
        println(initialDivision.size)
        return initialDivision
    }

    val initialDivision = initialDivision(ss, MinBoxSize + 1)

    val split = initialDivision.flatMap { box ->
        buildList {
            var i = 0
            while ((box.size - i) > CHEST_MAX) {
//                add(box.subList(i, i + ChestSize))
//                i += ChestSize
                val remainingNumItems = (box.size - i - CHEST_MAX)
                val maxCanTake = maxOf(CHEST_MAX, remainingNumItems - MinBoxSize)
                add(box.subList(i, i + maxCanTake))
                i += maxCanTake
            }
            add(box.subList(i, box.size))
        }
    }


    if (split.size < numBoxes) {
        TODO("not enough boxes; do splitting")
    }

    // do merging
    var numMergesNeeded = split.size - numBoxes

    val mergeResult = mutableListOf<List<SignalStrength>>()
    for (item in split) {
        if (
            numMergesNeeded > 0 &&
            mergeResult.isNotEmpty()
            && mergeResult.last().size + item.size <= CHEST_MAX
        ) {
            mergeResult.add(mergeResult.removeLast() + item)
            numMergesNeeded--
        } else {
            mergeResult.add(item)
        }
    }

    // check _chests_ (groups of boxes have min size)
    // not actually handled, we just assert and hope we don't get unlucky

    val chests = mergeResult.chunked(CHEST_MAX)

    require(chests.all { it.sumOf { it.size } >= MinRecordsPerCart })

    var numOptimized = 0
    val carts = chests.map { chest ->
        val boxes = chest.map {
            val actual = if (it.lastOrNull() == waitingMove) {
                // can optimize away
                numOptimized++
                it.subList(0, it.lastIndex)
            } else it
            SSBox(actual)
        }
        BoxCart(boxes)
    }

    println("optimized away $numOptimized waiting moves")


    return ChungusRom(carts)

}


fun ChungusRom.toSchem(
    cartRotation: Float = 0f,
): SchemFile {
    val entities = carts.map {
        val shulkerBoxes = it.boxes.mapIndexed { index, box ->
            val records = box.toRecordItems()
            Item(
                Slot = index.toByte(),
                id = "cyan_shulker_box",
                Count = 1,
                tag = compoundTag {
                    "BlockEntityTag" {
                        "Items" eq nbt.encodeToTag(records)
                    }
                }
            )
        }
        Entity(
            Id = "chest_minecart",
            Pos = listOf(0.5, 0.0, 0.5),
            Items = shulkerBoxes,
            Rotation = floatArrayOf(cartRotation, 0f),
        )
    }

    return SchemFile(
        Width = 1,
        Height = 1,
        Length = 1,
        Palette = mapOf("air" to 0),
        PaletteMax = 1,
        BlockData = byteArrayOf(0),
        BlockEntities = emptyList(),
        Entities = entities,
        DataVersion = DataVersions.v1_19_4,
    )
}
