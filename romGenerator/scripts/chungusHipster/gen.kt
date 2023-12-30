package chungusHipster

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.CHEST_MAX
import me.glassbricks.infinirom.*
import me.glassbricks.schem.writeSchematic
import java.io.File

private val romRestrictions = RomRestrictions(
    minBoxSize = 3,
    minInSomeBox = 10,
    minBoxesPerCart = CHEST_MAX,
    minCarts = 3,
)

private fun writeSimpleSchem(
    seq: List<Move>,
    name: String,
) {
    val rom = encodeSimpleChungusRom(seq, ChungusEncoding, romRestrictions)
    val schem = rom.toSchem()
    File(name).writeSchematic(schem)
}

fun writeOptimizedSchem(
    seq: List<Move>,
    name: String,
) {
    val rom = encodeWaitOptimizedChungusRom(seq, ChungusEncoding, romRestrictions)
    val schem = rom.toSchem()
    File(name).writeSchematic(schem)
}

class MakeSchems : StringSpec({
    "each row" {
        for (row in 1..10) {
            val seq = getChungusSequence { row(9) }
            writeSimpleSchem(seq, "chungus/row${row}.schem")
        }
    }
    "full door 10" {
        val seq = getChungusSequence { fullDoor(10, false) }

        writeSimpleSchem(
            seq,
            "~/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/Fabulously Optimized/.minecraft/config/worldedit/schematics/door10-v4.schem"
        )
    }

    "full door 10 optimized" {
        val seq = getChungusSequence { fullDoor(10, false) }
        writeOptimizedSchem(
            seq,
            "~/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/Fabulously Optimized/.minecraft/config/worldedit/schematics/door10-v5.schem"
        )
    }

    "print full door 10" {
        val seq = getChungusSequence { fullDoor(10, false) }
        println(seq.size)
        println(seq.zipWithNext().count { (a, b) -> a == Move.wait && b != Move.wait })
    }
})
