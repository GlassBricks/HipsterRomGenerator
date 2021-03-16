package me.glassbricks.schem

import kotlinx.coroutines.runBlocking
import me.glassbricks.hipster.normal6x6Sequence

fun main(): Unit = runBlocking {
    val home = System.getProperty("user.home")
    val dest = "$home/.local/share/multimc/instances/1.16.4/.minecraft/config/worldedit/schematics"

    val sequence = normal6x6Sequence
//    val encoding = mapOf(
//        Dpe to 0b000,
//        MoreObs to 0b001,
//        ClearPistons to 0b010,
//        Spe to 0b011,
//        Tpe to 0b100,
//        MorePistons to 0b101,
//        Store to 0b110,
//        Unstore to 0b111,
//    )
//    writeSchem(sequence, "$dest/super-special-6x6-rom.schem", encoding)
}
