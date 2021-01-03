@file:Suppress("FunctionName")

package me.glassbricks.knbt

import kotlinx.serialization.SerializationException


internal fun InvalidKeyKind(type: String) = SerializationException(
    "Value of type $type cannot be used as a key in a map. " +
            "It should have either primitive or enum kind."
)

internal fun String.toBooleanStrict() = when (this) {
    "true" -> true
    "false" -> false
    else -> throw IllegalArgumentException("This string does not represent a boolean")
}
