package me.glassbricks.sequence

import java.io.PrintWriter
import java.io.StringWriter


interface NamedRsSequence<I> {
    val name: String
    fun accept(builder: RsSequenceVisitor<I>)
}


fun interface RsSequenceGroup<I> {
    operator fun get(n: Int): NamedRsSequence<I>
}

operator fun <I> RsSequenceGroup<I>.getValue(
    thisRef: Any?,
    property: kotlin.reflect.KProperty<*>
): RsSequenceGroup<I> = this


fun <I> group(
    build: RsSequenceVisitor<I>.(Int) -> Unit
) = build

operator fun <I> (RsSequenceVisitor<I>.(Int) -> Unit).provideDelegate(
    thisRef: Any?,
    prop: kotlin.reflect.KProperty<*>
) = RsSequenceGroup { n ->
    object : NamedRsSequence<I> {
        override val name: String = "${prop.name}($n)"

        override fun accept(builder: RsSequenceVisitor<I>) {
            this@provideDelegate(builder, n)
        }
    }
}

fun <I> seq(
    build: RsSequenceVisitor<I>.() -> Unit
) = build

operator fun <I> (RsSequenceVisitor<I>.() -> Unit).provideDelegate(
    thisRef: Any?,
    prop: kotlin.reflect.KProperty<*>
) = object : NamedRsSequence<I> {
    override val name: String = prop.name

    override fun accept(builder: RsSequenceVisitor<I>) {
        this@provideDelegate(builder)
    }
}

operator fun <I> NamedRsSequence<I>.getValue(
    thisRef: Any?,
    property: kotlin.reflect.KProperty<*>
) = this



interface RsSequenceVisitor<I> {
    fun add(element: I)
    fun visit(sequence: NamedRsSequence<I>) = sequence.accept(this)


    fun addAll(elements: Iterable<I>) = elements.forEach { add(it) }
    fun add(vararg elements: I) = addAll(elements.asIterable())

    operator fun I.unaryPlus() = add(this)

    operator fun I.times(n: Int) = repeat(n) { add(this) }

    fun visit(group: RsSequenceGroup<I>, n: Int) = visit(group[n])

    operator fun NamedRsSequence<I>.invoke() = visit(this)
    operator fun RsSequenceGroup<I>.invoke(n: Int) = visit(this, n)
}

class SimpleSequenceVisitor<I> : RsSequenceVisitor<I> {
    private val elements = mutableListOf<I>()

    override fun add(element: I) {
        elements += element
    }

    fun build() = elements.toList()
}

inline fun <I> getSequence(visit: RsSequenceVisitor<I>.() -> Unit) =
    SimpleSequenceVisitor<I>().apply(visit).build()

class TreePrintVisitor<I> : RsSequenceVisitor<I> {
    private val out = StringWriter()
    private val printer = PrintWriter(out)

    private var indent = 0

    override fun add(element: I) {
        repeat(indent) { printer.append(' ') }
        printer.println(element)
    }

    override fun visit(sequence: NamedRsSequence<I>) {
        repeat(indent) { printer.append(' ') }
        printer.print(sequence.name)
        printer.println(':')
        indent += 2
        sequence.accept(this)
        indent -= 2

    }

    fun build() = out.toString()
}

inline fun <I> printTree(visit: RsSequenceVisitor<I>.() -> Unit) = TreePrintVisitor<I>().apply(visit).build()

class PrintAllVisitor<I> : RsSequenceVisitor<I> {

    private val result = mutableMapOf<String, CollectVisitor>()
    private fun ensureAlreadyPrinted(sequence: NamedRsSequence<I>) {
        if (sequence.name in result) return
        CollectVisitor().let {
            result[sequence.name] = it
            sequence.accept(it)
        }
    }

    private inner class CollectVisitor : RsSequenceVisitor<I> {
        val elements = mutableListOf<String>()
        override fun add(element: I) {
            elements += element.toString()
        }

        override fun visit(sequence: NamedRsSequence<I>) {
            ensureAlreadyPrinted(sequence)
            elements += sequence.name
        }
    }

    private val rootVisitor = CollectVisitor().also { result["root"] = it }

    override fun add(element: I) {
        rootVisitor.add(element)
    }

    override fun visit(group: RsSequenceGroup<I>, n: Int) {
        rootVisitor.visit(group, n)
    }

    fun build() = buildString {
        result.forEach { (name, visitor) ->
            append(name).appendLine(':')
            visitor.elements.forEach {
                append("    ").appendLine(it)
            }
            appendLine()
        }
    }
}

inline fun <I> printAll(visit: RsSequenceVisitor<I>.() -> Unit) = PrintAllVisitor<I>().apply(visit).build()
