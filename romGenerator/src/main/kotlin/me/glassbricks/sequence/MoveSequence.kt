package me.glassbricks.sequence


class MoveSequence(
    val name: String,
    parts: List<SequencePart>,
    val inline: Boolean = false,
) : SequencePart {
    init {
        require(parts.none { it is MoveSequence && it.inline })
    }

    val parts = parts.toList()

    val flattened: Sequence<SequenceItem> = parts.asSequence().flatMap {
        when (it) {
            is SequenceItem -> sequenceOf(it)
            is MoveSequence -> it.flattened
        }
    }
    val flatSize = parts.flatSize()

    override fun toString(): String = "$name: " + parts.joinToString {
        when (it) {
            is SequenceItem -> it.toString()
            is MoveSequence -> it.name
        }
    }
}

fun List<SequencePart>.flatSize(): Int = sumBy { if (it is MoveSequence) it.flatSize else 1 }

sealed interface SequencePart

interface SequenceItem : SequencePart
