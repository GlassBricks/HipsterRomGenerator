package me.glassbricks.sequence

import kotlin.properties.PropertyDelegateProvider

/**
 * A group of pistons sequences; for several row heights n
 */
interface PistonSequenceGroup<out M : SequenceMove> {
    val name: String
    operator fun get(n: Int): PistonSequence<M>
}

abstract class BasePistonSequenceGroup<M : SequenceMove>(
    final override val name: String,
) : PistonSequenceGroup<M> {
    private val sequences = mutableMapOf<Int, PistonSequence<M>>()

    final override fun get(n: Int): PistonSequence<M> {
        return sequences.getOrPut(n) {
            PistonSequenceBuilder<M>("$name[$n]").apply { create(n) }.build()
        }
    }

    protected abstract fun PistonSequenceBuilder<M>.create(n: Int)
}

typealias SequenceFunc<M> = PistonSequenceBuilder<M>.(n: Int) -> Unit

/**
 * A collection of sequence groups, a.k.a. all the sequences.
 */
abstract class SequenceCollection<M : SequenceMove> {
    private val _groups = mutableMapOf<String, PistonSequenceGroup<M>>()
    val groups: Map<String, PistonSequenceGroup<M>> get() = _groups

    /** Creates a [PistonSequenceGroup] */
    fun group(
        name: String? = null,
        build: PistonSequenceBuilder<M>.(n: Int) -> Unit,
    ): PropertyDelegateProvider<Any?, PistonSequenceGroup<M>> = PropertyDelegateProvider { _, property ->
        object : BasePistonSequenceGroup<M>(name = name ?: property.name) {
            override fun PistonSequenceBuilder<M>.create(n: Int) = build(n)
        }
            .also { _groups[it.name] = it }
    }

    /** Creates a piston sequence function, not saved */
    fun func(build: SequenceFunc<M>) = build

    operator fun PistonSequenceGroup<M>.getValue(thisRef: Any?, property: Any) = this
    operator fun SequenceFunc<M>.getValue(thisRef: Any?, property: Any) = this
}
