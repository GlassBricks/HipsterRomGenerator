package me.glassbricks.schem

fun <T> recordRomSchem(
    moves: List<T>,
    encoding: Map<T, Int>,
    waitingMove: T
): SchemFile {
    val waitingSS = SignalStrength(encoding.getValue(waitingMove))
    val ss = encodeToSignalStrengths(moves, encoding)
    val rom = toFullSSBoxes(ss, waitingSS)
    return toRecordChestRomSchem(rom)
}

fun toFullSSBoxes(
    ss: List<SignalStrength>,
    waitingMove: SignalStrength
) = SSBoxes(ss.windowed(CHEST_MAX, CHEST_MAX, partialWindows = true) {
    SSBox(it.toList().padToMinimum(CHEST_MAX, waitingMove))
})



fun toRecordChestRomSchem(ssBoxes: SSBoxes): SchemFile {
    val entities = ssBoxes.boxes.map { box ->
        Entity(
            Id = "chest_minecart",
            Pos = listOf(0.5, 0.0, 0.5),
            Items = box.toRecordItems(),
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
