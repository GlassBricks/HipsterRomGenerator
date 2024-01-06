package ogMegafoldHipster

import java.io.PrintWriter
import java.io.StringWriter


interface NamedRsSequence<I> {
    val name: String
    fun accept(builder: RsSequenceVisitor<I>)
}

fun <I> seq(build: RsSequenceVisitor<I>.() -> Unit) = build

operator fun <I> NamedRsSequence<I>.getValue(
    thisRef: Any?,
    property: kotlin.reflect.KProperty<*>
) = this


operator fun <I> (RsSequenceVisitor<I>.() -> Unit).provideDelegate(
    thisRef: Any?,
    prop: kotlin.reflect.KProperty<*>
) = object : NamedRsSequence<I> {
    override val name: String = prop.name

    override fun accept(builder: RsSequenceVisitor<I>) {
        this@provideDelegate(builder)
    }
}


fun interface RsSequenceFn<I, P> {
    operator fun get(n: P): NamedRsSequence<I>
}


fun <I> fn(build: RsSequenceVisitor<I>.(Int) -> Unit) = build
//fun <I, P> fn(build: RsSequenceVisitor<I>.(P) -> Unit) = build

operator fun <I, P> RsSequenceFn<I, P>.getValue(
    thisRef: Any?,
    property: kotlin.reflect.KProperty<*>
): RsSequenceFn<I, P> = this

operator fun <I, P> (RsSequenceVisitor<I>.(P) -> Unit).provideDelegate(
    thisRef: Any?,
    prop: kotlin.reflect.KProperty<*>
) = RsSequenceFn<I, P> { n ->
    object : NamedRsSequence<I> {
        override val name: String = "${prop.name}($n)"

        override fun accept(builder: RsSequenceVisitor<I>) {
            this@provideDelegate(builder, n)
        }
    }
}

fun interface RsSequenceFn2<I, P1, P2> {
    operator fun get(n1: P1, n2: P2): NamedRsSequence<I>
}

fun <I, P1, P2> fn(build: RsSequenceVisitor<I>.(P1, P2) -> Unit) = build

operator fun <I, P1, P2> RsSequenceFn2<I, P1, P2>.getValue(
    thisRef: Any?,
    property: kotlin.reflect.KProperty<*>
): RsSequenceFn2<I, P1, P2> = this

operator fun <I, P1, P2> (RsSequenceVisitor<I>.(P1, P2) -> Unit).provideDelegate(
    thisRef: Any?,
    prop: kotlin.reflect.KProperty<*>
) = RsSequenceFn2<I, P1, P2> { n1, n2 ->
    object : NamedRsSequence<I> {
        override val name: String = "${prop.name}($n1, $n2)"

        override fun accept(builder: RsSequenceVisitor<I>) {
            this@provideDelegate(builder, n1, n2)
        }
    }
}


interface RsSequenceVisitor<I> {
    fun add(element: I)
    fun visit(sequence: NamedRsSequence<I>) = sequence.accept(this)


    fun addAll(elements: Iterable<I>) = elements.forEach { add(it) }
    fun addAll(elements: Array<I>) = addAll(elements.asIterable())
    fun add(vararg elements: I) = addAll(elements.asIterable())

    operator fun I.unaryPlus() = add(this)
    operator fun I.times(n: Int) = repeat(n) { add(this) }

    operator fun NamedRsSequence<I>.invoke() = visit(this)
    operator fun <P> RsSequenceFn<I, P>.invoke(n: P) = visit(this[n])

    operator fun Array<I>.invoke() = addAll(this)
}

open class SequenceBuilder<I> : RsSequenceVisitor<I> {
    protected val elements = mutableListOf<I>()

    override fun add(element: I) {
        elements += element
    }

    open fun build() = elements.toList()
}

inline fun <I> getSequence(visit: RsSequenceVisitor<I>.() -> Unit) =
    SequenceBuilder<I>().apply(visit).build()

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

    override fun visit(sequence: NamedRsSequence<I>) {
        rootVisitor.visit(sequence)
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
