package me.glassbricks.hipster

import me.glassbricks.sequence.SequenceItem

enum class HipsterMove : SequenceItem {
    MorePistons, ClearPistons, MoreObs, ClearObs, Store, Spe, Dpe, Tpe;

    override fun toString(): String = name.splitCamelCase()
}

private fun String.splitCamelCase(): String =
    replace(
        String.format(
            "%s|%s|%s",
            "(?<=[A-Z])(?=[A-Z][a-z])",
            "(?<=[^A-Z])(?=[A-Z])",
            "(?<=[A-Za-z])(?=[^A-Za-z])"
        ).toRegex(), " "
    ).toLowerCase()
