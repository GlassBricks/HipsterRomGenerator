package me.glassbricks.sequence

import kotlin.properties.PropertyDelegateProvider

/**
 * A group of pistons sequences; for several different row heights n
 */
interface MoveSequenceGroup {
    val name: String
    operator fun get(n: Int): MoveSequence
}

abstract class AbstractMoveSequenceGroup(
    final override val name: String,
) : MoveSequenceGroup {
    private val sequences = mutableMapOf<Int, MoveSequence>()

    final override fun get(n: Int): MoveSequence {
        return sequences.getOrPut(n) {
            MoveSequenceBuilder("$name($n)").apply { create(n) }.build()
        }
    }

    protected abstract fun MoveSequenceBuilder.create(n: Int)
}

typealias SequenceFunc<E> = MoveSequenceBuilder.(n: Int) -> Unit

/**
 * A collection of sequence groups a.k.a All the sequences
 */
abstract class SequenceGroupHolder<E : SequenceItem> {
    private val _groups = mutableMapOf<String, MoveSequenceGroup>()
    val groups: Map<String, MoveSequenceGroup> get() = _groups

    protected fun group(
        name: String? = null,
        build: MoveSequenceBuilder.(n: Int) -> Unit,
    ): PropertyDelegateProvider<Any?, MoveSequenceGroup> = PropertyDelegateProvider { _, property ->
        object : AbstractMoveSequenceGroup(name = name ?: property.name) {
            override fun MoveSequenceBuilder.create(n: Int) = build(n)
        }
            .also { _groups[it.name] = it }
    }

    protected fun func(build: SequenceFunc<E>) = build

    protected operator fun MoveSequenceGroup.getValue(thisRef: Any?, property: Any) = this

    protected operator fun SequenceFunc<E>.getValue(thisRef: Any?, property: Any) = this
}
