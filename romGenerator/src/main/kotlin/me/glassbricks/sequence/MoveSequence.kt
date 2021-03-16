package me.glassbricks.sequence


/**
 * A piston sequence.
 *
 * Can contain other piston sequences.
 *
 * If [inline], will be inlined when inserted into other pistons sequences.
 *
 */
class MoveSequence<E : SequenceItem>(
    val name: String,
    parts: List<SequencePart>,
    val inline: Boolean = false,
) : SequencePart {
    init {
        require(parts.none { it is MoveSequence<*> && it.inline })
    }

    val parts = parts.toList()

    @Suppress("UNCHECKED_CAST")
    val flattened: Sequence<E> = parts.asSequence().flatMap {
        when (it) {
            is SequenceItem -> sequenceOf(it as E)
            is MoveSequence<*> -> it.flattened as Sequence<E>
        }
    }
    val flatSize = parts.flatSize()

    override fun toString(): String = "$name: " + parts.joinToString {
        when (it) {
            is SequenceItem -> it.toString()
            is MoveSequence<*> -> it.name
        }
    }
}

fun List<SequencePart>.flatSize(): Int = sumBy { if (it is MoveSequence<*>) it.flatSize else 1 }

sealed interface SequencePart

interface SequenceItem : SequencePart
