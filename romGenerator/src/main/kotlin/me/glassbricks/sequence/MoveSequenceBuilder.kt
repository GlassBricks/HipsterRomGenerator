package me.glassbricks.sequence

class MoveSequenceBuilder<E : SequenceItem>(
    private val name: String,
    inline: Boolean = false,
) {
    var inline = inline
        set(value) {
            nonDefaultInline = true
            field = value
        }

    private var nonDefaultInline: Boolean = inline

    private val items = mutableListOf<SequencePart>()
    private val flatSize get() = items.flatSize()

    fun add(sequence: MoveSequence<E>) {
        if (sequence.inline) {
            items += sequence.parts
        } else {
            items += sequence
        }
    }

    fun add(part: E) {
        items += part
    }

    operator fun MoveSequenceGroup<E>.invoke(n: Int) = add(get(n))

    fun build(): MoveSequence<E> {
        if (!nonDefaultInline) {
            inline = defaultInline()
        }
        return MoveSequence(name, items, inline)
    }

    private fun defaultInline() = items.size <= 3 || flatSize <= 3

}

fun <E : SequenceItem> MoveSequence(name: String, build: MoveSequenceBuilder<E>.() -> Unit) =
    MoveSequenceBuilder<E>(name).apply(build).build()
