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

sealed class Move : PistonSequenceItem() {
    override fun toString(): String = javaClass.simpleName.splitCamelCase()

    object MorePistons : Move()
    object ClearPistons : Move()
    object MoreObs : Move()
    object ClearObs : Move()
    object Store : Move()
    object Spe : Move()
    object Dpe : Move()
    object Tpe : Move()
}

private fun String.splitCamelCase(): String =
    replace(String.format("%s|%s|%s",
        "(?<=[A-Z])(?=[A-Z][a-z])",
        "(?<=[^A-Z])(?=[A-Z])",
        "(?<=[A-Za-z])(?=[^A-Za-z])"
    ).toRegex(), " ").toLowerCase()
