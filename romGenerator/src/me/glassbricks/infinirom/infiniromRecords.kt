package me.glassbricks.infinirom

import me.glassbricks.schem.DataVersions
import me.glassbricks.schem.Entity
import me.glassbricks.schem.SchemFile


class RecordInfinirom(val chests: List<SSBox>)

/** All carts are full */
fun <T> simpleRecordInfinirom(
    moves: List<T>,
    encoding: SSEncoding<T>,
    waitingMove: T,
): RecordInfinirom =
    encoding.encode(moves)
        .chunkToFullBoxes(encoding[waitingMove])
        .let(::RecordInfinirom)

fun RecordInfinirom.toRecordCartSchem(
    rotation: Float = 0f,
): SchemFile {
    val entities = chests.map { box ->
        Entity(
            Id = "chest_minecart",
            Pos = listOf(0.5, 0.0, 0.5),
            Items = box.toRecordItems(),
            Rotation = floatArrayOf(rotation, 0f),
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
