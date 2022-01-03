package schem

import hipster.HipsterMove.*
import hipster.special6x6Sequence
import me.glassbricks.rom.Encoding
import me.glassbricks.rom.encodeToItems
import me.glassbricks.schem.noBoxesSchem
import java.io.File

val home = System.getProperty("user.home")
val dest = "$home/.local/share/multimc/instances/1.16.4/.minecraft/config/worldedit/schematics"

val sequence = special6x6Sequence
val encoding = Encoding(
    mapOf(
        Dpe to 0b000,
        ClearPistons to 0b001,
        MoreObs to 0b010,
        MorePistons to 0b011,
        ClearObs to 0b100,
        Tpe to 0b101,
        Spe to 0b110,
    ), 3
)

writeSchematic(
    encodeToItems(sequence.asSequence(), encoding).let(::noBoxesSchem),
    File("$dest/extra-special-6x5-rom.schem")
)
