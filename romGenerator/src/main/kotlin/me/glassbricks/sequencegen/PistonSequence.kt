package me.glassbricks.sequencegen

sealed class PistonSequenceItem

class PistonSequence(
    val name: String,
    items: List<PistonSequenceItem>,
    val inline: Boolean = false,
) : PistonSequenceItem() {
    init {
        require(items.none { it is PistonSequence && it.inline })
    }

    val items: List<PistonSequenceItem> = items.toList()
    val flatSize = items.flatSize()
    val flattened: Sequence<Move> = this.items.asSequence().flatMap {
        when (it) {
            is Move -> sequenceOf(it)
            is PistonSequence -> it.flattened
        }
    }

    override fun toString(): String = "$name: " + items.joinToString {
        when (it) {
            is Move -> it.toString()
            is PistonSequence -> it.name
        }
    }
}

fun List<PistonSequenceItem>.flatSize(): Int = sumBy { if (it is PistonSequence) it.flatSize else 1 }

sealed class Move(
    val encoding: Byte,
) : PistonSequenceItem() {
    override fun toString(): String = javaClass.simpleName.splitCamelCase()

    object MorePistons : Move(0b011)
    object ClearPistons : Move(0b010)
    object MoreObs : Move(0b100)
    object ClearObs : Move(0b110)
    object Store : Move(0b111)
    object Spe : Move(0b101)
    object Dpe : Move(0b000)
    object Tpe : Move(0b001)
}

private fun String.splitCamelCase(): String =
    replace(String.format("%s|%s|%s",
        "(?<=[A-Z])(?=[A-Z][a-z])",
        "(?<=[^A-Z])(?=[A-Z])",
        "(?<=[A-Za-z])(?=[^A-Za-z])"
    ).toRegex(), " ").toLowerCase()
