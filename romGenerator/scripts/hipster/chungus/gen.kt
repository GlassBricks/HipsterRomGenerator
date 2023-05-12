package hipster.chungus

import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.serializer
import me.glassbricks.knbt.Nbt
import me.glassbricks.schem.encodeToChungusRom
import schem.writeSchematic


class MakeSchems : StringSpec({


    "row 9 only schem" {
        val seq = getChungusSequence { row(9) }
        val schem = encodeToChungusRom(
            seq,
            ChungusEncoding,
            Move.wait,
        )

        val nbt = Nbt { encodeDefaults = true }
        val tag = nbt.encodeToTag(serializer(), schem)
        println(tag)

        writeSchematic(schem, "ch-row-9.schem")
    }
})