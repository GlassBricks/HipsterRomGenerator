package me.glassbricks.schem

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer
import me.glassbricks.knbt.Nbt
import me.glassbricks.knbt.compoundTag


@JvmInline
value class SignalStrength(val ss: Int)


const val ChestSize = 27


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

fun divCeil(a: Int, b: Int): Int = (a + b - 1) / b


const val MinInBox = 3
const val MinInLastBox = 10
const val MinRecordsPerCart = (MinInBox * (ChestSize - 1)) + MinInLastBox
const val MaxRecordsPerCart = ChestSize * ChestSize
const val MinCarts = 3
const val MinRecordsPerRom = MinRecordsPerCart * MinCarts

private fun partitionRecordsToCarts(
    numRecords: Int,
): List<Int> {
    require(numRecords >= MinRecordsPerRom)
    val maxPerCart = ChestSize * ChestSize
    val numCarts = divCeil(numRecords, maxPerCart).coerceAtLeast(MinCarts)

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
        numPartitions = ChestSize,
        minPerPartition = MinInBox,
        maxPerPartition = ChestSize,
    )
}

private
fun List<SignalStrength>.padToMinimum(
    waitingMove: SignalStrength,
): List<SignalStrength> {

    val numToPad = MinRecordsPerRom - size
    if (numToPad > 0) {
        return this + List(numToPad) { waitingMove }
    }
    return this
}


private
class RecordBox(val records: List<SignalStrength>)

private
class BoxCart(val boxes: List<RecordBox>)

private
class ChungusRom(val carts: List<BoxCart>)


private fun ssToChungusRom(
    ss: List<SignalStrength>,
    waitingMove: SignalStrength? = null,
): ChungusRom {
    val actualSS = ss.padToMinimum(waitingMove ?: error("no waiting move provided"))

    var iRom = 0
    val cartSizes = partitionRecordsToCarts(actualSS.size)
    val carts = cartSizes.map { numInCart ->
        val cartSS = actualSS.subList(iRom, iRom + numInCart)
        iRom += numInCart

        var iCart = 0
        val boxes = partitionRecordsToShulkerBoxes(numInCart).map { numInBox ->
            val boxSS = cartSS.subList(iCart, iCart + numInBox)
            iCart += numInBox
            RecordBox(boxSS)
        }
        BoxCart(boxes)

    }

    return ChungusRom(carts)
}

private
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
)

val nbt = Nbt { encodeDefaults=true }
private fun chungusRomToSchematic(
    rom: ChungusRom,
): SchemFile {
    val entities = rom.carts.map {
        val chestItems = it.boxes.mapIndexed { index, indexChest ->
            val items = indexChest.records.mapIndexed { indexBox, ss ->
                Item(
                    Slot = indexBox.toByte(),
                    id = ssToRecord[ss.ss]!!,
                    Count = 1,
                )
            }
            Item(
                Slot = index.toByte(),
                id = "cyan_shulker_box",
                Count = 1,
                tag = compoundTag {
                    "BlockEntityTag" {
                        "Items" eq nbt.encodeToTag(serializer(), items)
                    }
                }
            )
        }

        Entity(
            Id = "chest_minecart",
            Pos = listOf(0.5, 0.0, 0.5),
            Items = chestItems,
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


private fun <T> toSignalStrengths(
    moves: List<T>,
    encoding: Map<T, Int>,
): List<SignalStrength> {
    return moves.map { SignalStrength(encoding.getValue(it)) }
}


fun <T> encodeToChungusRom(
    moves: List<T>,
    encoding: Map<T, Int>,
    waitingMove: T,
): SchemFile {
    val ss = toSignalStrengths(moves, encoding)
    val waitingSS = SignalStrength(encoding.getValue(waitingMove))
    val rom = ssToChungusRom(ss, waitingSS)
    return chungusRomToSchematic(rom)
}