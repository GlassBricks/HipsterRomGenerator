package me.glassbricks.sequence

import kotlin.properties.PropertyDelegateProvider

interface PistonSequenceGroup {
    val name: String
    operator fun get(n: Int): PistonSequence
}

abstract class AbstractPistonSequenceGroup(
    final override val name: String,
) : PistonSequenceGroup {
    private val sequences = mutableMapOf<Int, PistonSequence>()

    final override fun get(n: Int): PistonSequence {
        return sequences.getOrPut(n) {
            PistonSequenceBuilder("$name($n)").apply { create(n) }.build()
        }
    }

    protected abstract fun PistonSequenceBuilder.create(n: Int)
}


inline fun group(
    name: String? = null,
    crossinline build: PistonSequenceBuilder.(n: Int) -> Unit,
): PropertyDelegateProvider<Any?, PistonSequenceGroup> = PropertyDelegateProvider { _, property ->
    object : AbstractPistonSequenceGroup(name = name ?: property.name) {
        override fun PistonSequenceBuilder.create(n: Int) = build(n)
    }
}


typealias SequenceFunc = PistonSequenceBuilder.(n: Int) -> Unit

fun func(build: SequenceFunc) = build

inline fun seq(
    name: String,
    crossinline build: PistonSequenceBuilder.() -> Unit,
) = PistonSequenceBuilder(name).apply(build).build()


@Suppress("NOTHING_TO_INLINE")
inline operator fun PistonSequenceGroup.getValue(thisRef: Any?, property: Any) = this

@Suppress("NOTHING_TO_INLINE")
inline operator fun SequenceFunc.getValue(thisRef: Any?, property: Any) = this

@Suppress("NOTHING_TO_INLINE")
inline operator fun PistonSequence.getValue(thisRef: Any?, property: Any) = this
