package me.glassbricks.sequence


fun PistonSequence.printFlat(out: Appendable) {
    flattened.forEach {
        out.appendLine(it.toString())
    }
}

fun PistonSequence.printTree(out: Appendable) = printTree(this, out, 0)

private fun printTree(sequence: PistonSequence, out: Appendable, indent: Int) {
    sequence.items.forEach { item ->
        repeat(indent) { out.append(' ') }
        when (item) {
            is Move -> out.appendLine(item.toString())
            is PistonSequence -> {
                out.append(item.name).appendLine(':')
                printTree(item, out, indent + 2)
            }
        }
    }
}

fun PistonSequence.printGroups(out: Appendable) {
    val o = object {
        val alreadyPrinted = mutableSetOf<PistonSequence>()
        fun ensureAlreadyPrinted(sequence: PistonSequence) {
            if (sequence !in alreadyPrinted)
                print(sequence)
            alreadyPrinted.add(sequence)
        }

        fun print(sequence: PistonSequence) {
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
    o.print(this)
}
