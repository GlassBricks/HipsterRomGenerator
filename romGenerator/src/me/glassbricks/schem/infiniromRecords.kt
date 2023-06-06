package me.glassbricks.schem


private fun ssToRecordRom(
    ss: List<SignalStrength>,
    waitingMove: SignalStrength
) = SSBoxes(ss.windowed(ChestSize, ChestSize, partialWindows = true) {
    SSBox(it.toList().padToMinimum(ChestSize, waitingMove))
})

// in between chests, there's 2 free waiting moves
// if on boundary there happens to be waiting moves, we can remove up to 2 of them
private fun ssToWaitOptimizedRom(
    ss: List<SignalStrength>,
    waitingMove: SignalStrength
) = SSBoxes(buildList {
    var lastI = 0
    while (lastI < ss.size) {
        val remaining = ss.size - lastI
        if (remaining <= ChestSize) {
            add(
                SSBox(ss.subList(lastI, ss.size).padToMinimum(ChestSize, waitingMove))
            )
            break
        }
        add(SSBox(ss.subList(lastI, lastI + ChestSize)))
        lastI += ChestSize
        repeat(2) {
            if (ss.getOrNull(lastI) == waitingMove) lastI++
        }
    }
})

fun recordRomToSchematic(rom: SSBoxes): SchemFile {
    val entities = rom.boxes.map { box ->
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


fun <T> recordRomSchem(
    moves: List<T>,
    encoding: Map<T, Int>,
    waitingMove: T
): SchemFile {
    val waitingSS = SignalStrength(encoding.getValue(waitingMove))
    val ss = toSignalStrengths(moves, encoding)
    val rom = ssToRecordRom(ss, waitingSS)
    return recordRomToSchematic(rom)
}

fun <T> waitOptimizedRecordRomSchem(
    moves: List<T>,
    encoding: Map<T, Int>,
    waitingMove: T
): SchemFile {
    val waitingSS = SignalStrength(encoding.getValue(waitingMove))
    val ss = toSignalStrengths(moves, encoding)
    val rom = ssToWaitOptimizedRom(ss, waitingSS)
    return recordRomToSchematic(rom)
}
