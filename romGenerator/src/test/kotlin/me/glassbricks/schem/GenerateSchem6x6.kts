package me.glassbricks.schem

import me.glassbricks.hipster.HipsterMove.*
import me.glassbricks.hipster.special6x6Sequence
import me.glassbricks.rom.toItemsStacks
import java.io.File

val home = System.getProperty("user.home")
val dest = "$home/.local/share/multimc/instances/1.16.4/.minecraft/config/worldedit/schematics"

val sequence = special6x6Sequence
val encoding = mapOf(
    Dpe to 0b000,
    ClearPistons to 0b001,
    MoreObs to 0b010,
    MorePistons to 0b011,
    ClearObs to 0b100,
    Tpe to 0b101,
    Spe to 0b110,
)

sequence
    .toItemsStacks(encoding)
    .toSchem()
    .writeTo(File("$dest/extra-special-6x5-rom.schem"))
