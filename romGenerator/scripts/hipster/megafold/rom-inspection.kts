package hipster.megafold

import me.glassbricks.sequence.getSequence
import me.glassbricks.toRom

val boxes = toRom(getSequence { glassSequence[3] }, glassHipster11Encoding)
println(boxes)
