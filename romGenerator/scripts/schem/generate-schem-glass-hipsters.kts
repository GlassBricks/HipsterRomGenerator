package schem

import hipster.HipsterSequences
import hipster.glassHipster11Encoding
import me.glassbricks.rom.toRom
import me.glassbricks.schem.schem1
import java.io.File


val home: String = System.getProperty("user.home")
val dest = "$home/.local/share/multimc/instances/1.16.4/.minecraft/config/worldedit/schematics"

HipsterSequences.groups.forEach { (name, group) ->
    val range = (6..11).let {
        if (name == "extend") it + (-7..-6) else it
    }
    for (i in range) {
        writeSchematic(
            schem1(
                group[i].toRom(glassHipster11Encoding)
            ), File("$dest/$name-$i.schem")
        )
    }
}
