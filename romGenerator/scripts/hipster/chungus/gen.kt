package hipster.chungus

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.schem.chungusRomSchem
import me.glassbricks.schem.waitOptimizedChungusRomSchem
import schem.writeSchematic
import java.io.File


class MakeSchems : StringSpec({


    "row 9 only schem" {
        val seq = getChungusSequence { row(9) }
        val schem = chungusRomSchem(
            seq,
            ChungusEncoding,
        )

//        val nbt = Nbt { encodeDefaults = true }
//        val tag = nbt.encodeToTag(serializer(), schem)
//        println(tag)

        writeSchematic(schem, "row9-v5.schem")
    }

    "row 10 only schem" {
        val seq = getChungusSequence { row(10) }
        val schem = chungusRomSchem(
            seq,
            ChungusEncoding,
        )

        writeSchematic(schem, "row10-v9.schem")
    }
    "print row 10" {
        val seq = getChungusSequence { row(10) }
        println(seq)
    }

    "full door 10 schem" {
        val seq = getChungusSequence { fullDoor(10, false) }
        val schem = chungusRomSchem(
            seq,
            ChungusEncoding,
        )

        writeSchematic(
            schem,
            File("~/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/Fabulously Optimized/.minecraft/config/worldedit/schematics/door10-v4.schem")
        )
    }

    "full door 10 maybe optimized schem" {
        val seq = getChungusSequence { fullDoor(10, false) }
        val schem = waitOptimizedChungusRomSchem(
            seq,
            ChungusEncoding,
            Move.wait,
        )

        writeSchematic(
            schem,
//            File("~/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/Fabulously Optimized/.minecraft/config/worldedit/schematics/door10-v5.schem")
            "door10-v8.schem"
        )
    }

    "print full door 10" {
        val seq = getChungusSequence { fullDoor(10, false) }
        println(seq.size)
        println(seq.zipWithNext().count { (a, b) -> a == Move.wait && b != Move.wait })
    }
})
