package me.glassbricks.knbt

import io.kotest.core.spec.style.FreeSpec
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
data class Multiple(
    val byte: Byte,
    val int: Int,
    val arr: String,
)

@Serializable
data class Composed(
    val i: Int,
    val mul: Multiple,
    val sing: SingleString,
)


@OptIn(ExperimentalUnsignedTypes::class)
internal class CompoundTest : FreeSpec({
    "multiple" - {
        setupMirrorTest(Multiple(1, 2, "Hey"), serializer())
    }
    "composed" - {
        setupMirrorTest(Composed(3, Multiple(1, 2, "world"), SingleString("Hey")), serializer())
    }
})