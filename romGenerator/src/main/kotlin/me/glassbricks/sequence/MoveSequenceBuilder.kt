package me.glassbricks.sequence

class MoveSequenceBuilder(
    private val name: String,
    inline: Boolean = false,
) {
    var inline = inline
        set(value) {
            nonDefaultInline = true
            field = value
        }

    private var nonDefaultInline: Boolean = inline

    private val parts = mutableListOf<SequencePart>()
    private val flatSize get() = parts.flatSize()

    fun add(sequence: MoveSequence) {
        if (sequence.inline) {
            parts += sequence.parts
        } else {
            parts += sequence
        }
    }

    fun add(part: SequenceItem) {
        parts += part
    }

    operator fun MoveSequenceGroup.invoke(n: Int) = add(get(n))

    fun build(): MoveSequence {
        if (!nonDefaultInline) {
            inline = defaultInline()
        }
        return MoveSequence(name, parts, inline)
    }

    private fun defaultInline() = parts.size <= 3 || flatSize <= 3

}

fun MoveSequence(name: String, build: MoveSequenceBuilder.() -> Unit) =
    MoveSequenceBuilder(name).apply(build).build()
