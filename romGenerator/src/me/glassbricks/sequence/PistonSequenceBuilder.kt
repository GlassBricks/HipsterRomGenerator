package me.glassbricks.sequence

class PistonSequenceBuilder<M : SequenceMove>(
    private val name: String,
    inline: Boolean = false,
) {
    var inline = inline
        set(value) {
            useDefaultInline = false
            field = value
        }

    private var useDefaultInline: Boolean = !inline

    private val elements = mutableListOf<SequenceElement>()
    private val flatSize get() = elements.numMoves()

    fun add(sequence: PistonSequence<M>) {
        if (sequence.inline) {
            elements += sequence.elements
        } else {
            elements += sequence
        }
    }

    fun add(part: M) {
        elements += part
    }

    operator fun PistonSequenceGroup<M>.invoke(n: Int) = add(this[n])

    fun build(): PistonSequence<M> {
        if (useDefaultInline) {
            inline = defaultInline()
        }
        return PistonSequence(name, elements, inline)
    }

    fun defaultInline() = elements.size <= 3 || flatSize <= 3
}

@Suppress("FunctionName")
fun <M : SequenceMove> PistonSequence(name: String, build: PistonSequenceBuilder<M>.() -> Unit) =
    PistonSequenceBuilder<M>(name).apply(build).build()
