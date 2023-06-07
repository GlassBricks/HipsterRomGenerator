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



// in between chests, there are 2 free waiting moves
// if on boundary there happens to be waiting moves, we can remove up to 2 of them
fun toWaitOptimizedSSBoxes(
    ss: List<SignalStrength>,
    waitingMove: SignalStrength
) = SSBoxes(buildList {
    var lastI = 0
    while (lastI < ss.size) {
        val remaining = ss.size - lastI
        if (remaining <= CHEST_MAX) {
            add(
                SSBox(ss.subList(lastI, ss.size).padToMinimum(CHEST_MAX, waitingMove))
            )
            break
        }
        add(SSBox(ss.subList(lastI, lastI + CHEST_MAX)))
        lastI += CHEST_MAX
        repeat(2) {
            if (ss.getOrNull(lastI) == waitingMove) lastI++
        }
    }
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
