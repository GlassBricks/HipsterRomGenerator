package me.glassbricks.sequence

import kotlin.properties.PropertyDelegateProvider

/**
 * A group of pistons sequences; for several different row heights n
 */
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

typealias SequenceFunc = PistonSequenceBuilder.(n: Int) -> Unit

/**
 * A collection of sequence groups a.k.a All the sequences
 */
abstract class SequenceGroupHolder {
    private val _groups = mutableMapOf<String, PistonSequenceGroup>()
    val groups get() = _groups

    protected fun group(
            name: String? = null,
            build: PistonSequenceBuilder.(n: Int) -> Unit,
    ): PropertyDelegateProvider<Any?, PistonSequenceGroup> {
        return PropertyDelegateProvider { _, property ->
            object : AbstractPistonSequenceGroup(name = name ?: property.name) {
                override fun PistonSequenceBuilder.create(n: Int) = build(n)
            }
                    .also { _groups[it.name] = it }
        }
    }

    protected fun func(build: SequenceFunc) = build

    protected operator fun PistonSequenceGroup.getValue(thisRef: Any?, property: Any) = this

    protected operator fun SequenceFunc.getValue(thisRef: Any?, property: Any) = this
}
