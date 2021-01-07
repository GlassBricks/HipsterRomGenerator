package me.glassbricks.sequence

import me.glassbricks.sequence.Move.*

class PistonSequenceBuilder(
    val name: String,
    inline: Boolean = false,
) {
    var inline = inline
        set(value) {
            nonDefaultInline = true
            field = value
        }

    private var nonDefaultInline: Boolean = inline

    private val items = mutableListOf<PistonSequenceItem>()
    val size = items.size
    val flatSize get() = items.flatSize()

    fun add(item: PistonSequenceItem) {
        if (item is PistonSequence && item.inline) {
            items += item.items
            return
        }
        items += item
    }

    operator fun PistonSequenceItem.invoke() = add(this)
    operator fun PistonSequenceGroup.invoke(n: Int) = add(get(n))

    val Int.pe: Unit
        get() = when (this) {
            1 -> Spe()
            2 -> Dpe()
            3 -> Tpe()
            else -> throw UnsupportedOperationException()
        }

    fun build(): PistonSequence {
        if (!nonDefaultInline) {
            inline = defaultInline()
        }
        return PistonSequence(name, items, inline)
    }

    private fun defaultInline() = items.size <= 3 || flatSize <= 3
}

