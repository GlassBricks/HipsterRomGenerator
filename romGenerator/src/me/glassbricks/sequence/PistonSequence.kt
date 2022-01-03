package me.glassbricks.sequence

sealed interface SequenceElement
interface SequenceMove : SequenceElement

class PistonSequence<out M : SequenceMove>(
    val name: String,
    elements: List<SequenceElement>,
    val inline: Boolean = false,
) : SequenceElement {
    init {
        require(elements.none { it is PistonSequence<*> && it.inline })
    }

    val elements = elements.toList()

    @Suppress("UNCHECKED_CAST")
    val moves: Sequence<M> = elements.asSequence().flatMap {
        when (it) {
            is SequenceMove -> sequenceOf(it as M)
            is PistonSequence<*> -> (it as PistonSequence<M>).moves
        }
    }
    val numMoves = elements.numMoves()

    override fun toString(): String = """PistonSequence(name=$name, size=${elements.size})"""
}

fun List<SequenceElement>.numMoves(): Int = sumOf { if (it is PistonSequence<*>) it.numMoves else 1 }
