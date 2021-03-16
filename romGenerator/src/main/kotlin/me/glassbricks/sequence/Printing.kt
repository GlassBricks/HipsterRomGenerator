package me.glassbricks.sequence


fun MoveSequence<*>.printFlat(out: Appendable) {
    flattened.forEach {
        out.appendLine(it.toString())
    }
}

fun MoveSequence<*>.printTree(out: Appendable) = printTree(this, out, 0)

private fun printTree(sequence: MoveSequence<*>, out: Appendable, indent: Int) {
    sequence.parts.forEach { item ->
        repeat(indent) { out.append(' ') }
        when (item) {
            is SequenceItem -> out.appendLine(item.toString())
            is MoveSequence<*> -> {
                out.append(item.name).appendLine(':')
                printTree(item, out, indent + 2)
            }
        }
    }
}

fun MoveSequence<*>.printGroups(out: Appendable) {
    val o = object {
        val alreadyPrinted = mutableSetOf<MoveSequence<*>>()
        fun ensureAlreadyPrinted(sequence: MoveSequence<*>) {
            if (sequence !in alreadyPrinted)
                print(sequence)
            alreadyPrinted.add(sequence)
        }

        fun print(sequence: MoveSequence<*>) {
            sequence.parts.forEach {
                if (it is MoveSequence<*>) ensureAlreadyPrinted(it)
            }
            out.append(sequence.name).appendLine(':')
            sequence.parts.forEach {
                out.append("    ")
                when (it) {
                    is SequenceItem -> out.append(it.toString())
                    is MoveSequence<*> -> out.append(it.name)
                }
                out.appendLine()
            }
            out.appendLine()
        }
    }
    o.print(this)
}
