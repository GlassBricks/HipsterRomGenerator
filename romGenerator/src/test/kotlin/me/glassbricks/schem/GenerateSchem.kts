package me.glassbricks.schem

import me.glassbricks.hipster.HipsterSequences
import me.glassbricks.rom.toRom
import java.io.File


val home = System.getProperty("user.home")
val dest = "$home/.local/share/multimc/instances/1.16.4/.minecraft/config/worldedit/schematics"

HipsterSequences.groups.forEach { (name, group) ->
    val range = (6..11).let {
        if (name == "extend") it + (-7..-6) else it
    }
    for (i in range) {
        group[i]
            .toRom(HipsterSequences.glassHipsterEncoding)
            .toSchem2()
            .writeTo(File("$dest/$name-$i.schem"))
    }
}
