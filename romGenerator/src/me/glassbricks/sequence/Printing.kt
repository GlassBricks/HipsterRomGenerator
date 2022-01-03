package me.glassbricks.sequence


fun PistonSequence<*>.printTree(out: Appendable) = printTree(this, out, 0)

private fun printTree(sequence: PistonSequence<*>, out: Appendable, indent: Int) {
    sequence.elements.forEach { item ->
        repeat(indent) { out.append(' ') }
        when (item) {
            is SequenceMove -> out.appendLine(item.toString())
            is PistonSequence<*> -> {
                out.append(item.name).appendLine(':')
                printTree(item, out, indent + 2)
            }
        }
    }
}

fun PistonSequence<*>.printGroups(out: Appendable) {
    val o = object {
        val alreadyPrinted = hashSetOf<PistonSequence<*>>()
        fun ensureAlreadyPrinted(sequence: PistonSequence<*>) {
            alreadyPrinted.add(sequence)
            if (sequence !in alreadyPrinted)
                print(sequence)
        }

        fun print(sequence: PistonSequence<*>) {
            sequence.elements.forEach {
                if (it is PistonSequence<*>) ensureAlreadyPrinted(it)
            }
            out.append(sequence.name).appendLine(':')
            sequence.elements.forEach {
                out.append("    ")
                when (it) {
                    is SequenceMove -> out.append(it.toString())
                    is PistonSequence<*> -> out.append(it.name)
                }
                out.appendLine()
            }
            out.appendLine()
        }
    }
    o.print(this)
}
