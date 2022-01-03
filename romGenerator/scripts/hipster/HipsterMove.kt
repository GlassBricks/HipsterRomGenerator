package hipster

import me.glassbricks.sequence.SequenceMove
import me.glassbricks.splitCamelCase

enum class HipsterMove : SequenceMove {
    MorePistons, ClearPistons, MoreObs, ClearObs, Store, Spe, Dpe, Tpe;

    override fun toString(): String = name.splitCamelCase()
}
