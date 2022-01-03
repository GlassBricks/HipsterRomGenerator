import hipster.HipsterSequences
import hipster.glassHipster11Encoding
import me.glassbricks.rom.toRom

val boxes = toRom(HipsterSequences.glassSequence[3].moves, glassHipster11Encoding)
println(boxes)
