package me.glassbricks.sequence

import me.glassbricks.sequence.Move.*

class PistonSequenceBuilder(
    private val name: String,
    inline: Boolean = false,
) {
    var inline = inline
        set(value) {
            nonDefaultInline = true
            field = value
        }

    private var nonDefaultInline: Boolean = inline

    private val items = mutableListOf<PistonSequenceItem>()
    private val flatSize get() = items.flatSize()

    fun add(item: PistonSequenceItem) {
        if (item is PistonSequence && item.inline) {
            items += item.items
            return
        }
        items += item
    }

    operator fun PistonSequenceGroup.invoke(n: Int) = add(get(n))

    val Int.pe: Unit
        get() = when (this) {
            1 -> add(Spe)
            2 -> add(Dpe)
            3 -> add(Tpe)
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

