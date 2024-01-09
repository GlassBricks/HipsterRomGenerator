package me.glassbricks

open class SequenceBuilder<T> {
    protected val elements = mutableListOf<T>()

    open fun add(element: T) {
        elements.add(element)
    }
    open fun add(vararg elements: T) {
        elements.forEach(::add)
    }
    open fun addAll(elements: Iterable<T>) {
        elements.forEach(::add)
    }
    open fun addAll(elements: Array<T>) {
        elements.forEach(::add)
    }

    open fun build() = elements.toList()


    operator fun T.unaryPlus() = add(this)
    open operator fun T.times(n: Int) = repeat(n) { add(this) }
}

inline fun <I> getSequence(build: SequenceBuilder<I>.() -> Unit) =
    SequenceBuilder<I>().apply(build).build()
