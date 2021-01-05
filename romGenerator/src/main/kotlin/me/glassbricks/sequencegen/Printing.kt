package me.glassbricks.sequencegen


fun PistonSequence.printFlat(out: Appendable) = with(out) {
    flattened.forEach {
        appendLine(it.toString())
    }
}

fun PistonSequence.printGroups(out: Appendable) {
    val o = object {
        val alreadyPrinted = mutableSetOf<PistonSequence>()
        fun ensureAlreadyPrinted(
            sequence: PistonSequence,
        ) {
            if (sequence !in alreadyPrinted)
                doPrint(sequence)
            alreadyPrinted.add(sequence)
        }

        fun doPrint(sequence: PistonSequence) {
            sequence.items.forEach {
                if (it is PistonSequence) ensureAlreadyPrinted(it)
            }
            out.append(sequence.name).appendLine(':')
            sequence.items.forEach {
                out.append("    ")
                when (it) {
                    is Move -> out.append(it.toString())
                    is PistonSequence -> out.append(it.name)
                }
                out.appendLine()
            }
            out.appendLine()
        }
    }
    o.doPrint(this)
}