package me.glassbricks.sequencegen

import me.glassbricks.sequencegen.Move.*

class PistonSequenceBuilder(
    val name: String,
    var inline: Boolean = false,
) {
    val lastMove: PistonSequenceItem? get() = items.lastOrNull()
    val items = mutableListOf<PistonSequenceItem>()

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

    fun build() = PistonSequence(name, items, inline)
}

