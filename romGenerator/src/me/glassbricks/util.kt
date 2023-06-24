package me.glassbricks

import java.util.*

const val CHEST_MAX = 27
fun String.splitCamelCase(): String =
    replace(
        String.format(
            "%s|%s|%s",
            "(?<=[A-Z])(?=[A-Z][a-z])",
            "(?<=[^A-Z])(?=[A-Z])",
            "(?<=[A-Za-z])(?=[^A-Za-z])"
        ).toRegex(), " "
    ).lowercase(Locale.getDefault())
