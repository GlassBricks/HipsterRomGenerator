package me.glassbricks.sequence

import kotlin.properties.PropertyDelegateProvider

/**
 * A group of pistons sequences; for several different row heights n
 */
interface MoveSequenceGroup<E : SequenceItem> {
    val name: String
    operator fun get(n: Int): MoveSequence<E>
}

abstract class AbstractMoveSequenceGroup<E : SequenceItem>(
    final override val name: String,
) : MoveSequenceGroup<E> {
    private val sequences = mutableMapOf<Int, MoveSequence<E>>()

    final override fun get(n: Int): MoveSequence<E> {
        return sequences.getOrPut(n) {
            MoveSequenceBuilder<E>("$name($n)").apply { create(n) }.build()
        }
    }

    protected abstract fun MoveSequenceBuilder<E>.create(n: Int)
}

typealias SequenceFunc<E> = MoveSequenceBuilder<E>.(n: Int) -> Unit

/**
 * A collection of sequence groups a.k.a All the sequences
 */
abstract class SequenceGroupHolder<E : SequenceItem> {
    private val _groups = mutableMapOf<String, MoveSequenceGroup<E>>()
    val groups: Map<String, MoveSequenceGroup<E>> get() = _groups

    protected fun group(
        name: String? = null,
        build: MoveSequenceBuilder<E>.(n: Int) -> Unit,
    ): PropertyDelegateProvider<Any?, MoveSequenceGroup<E>> = PropertyDelegateProvider { _, property ->
        object : AbstractMoveSequenceGroup<E>(name = name ?: property.name) {
            override fun MoveSequenceBuilder<E>.create(n: Int) = build(n)
        }
            .also { _groups[it.name] = it }
    }

    protected fun func(build: SequenceFunc<E>) = build

    protected operator fun MoveSequenceGroup<E>.getValue(thisRef: Any?, property: Any) = this

    protected operator fun SequenceFunc<E>.getValue(thisRef: Any?, property: Any) = this
}
