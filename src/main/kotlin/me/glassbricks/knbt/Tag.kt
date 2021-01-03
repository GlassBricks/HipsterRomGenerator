package me.glassbricks.knbt

import kotlinx.serialization.Serializable


enum class TagType {
    End,
    Byte,
    Short,
    Int,
    Long,
    Float,
    Double,
    ByteArray,
    String,
    List,
    Compound,
    IntArray,
    LongArray;

    val id get() = ordinal.toByte()

    companion object {
        fun fromId(id: kotlin.Byte): TagType? = values().getOrNull(id.toInt())
    }

}

@Serializable(with = TagSerializer::class)
sealed class Tag {
    abstract val type: TagType
    abstract val value: Any

    internal open fun appendTo(builder: StringBuilder, indent: Int) {
        builder.append(this.toString())
    }
}

@Serializable(with = ByteTagSerializer::class)
class ByteTag(override val value: Byte) : Tag() {
    override val type get() = TagType.Byte
    override fun toString(): String = "${value}b"
}

@Serializable(with = ShortTagSerializer::class)
class ShortTag(override val value: Short) : Tag() {
    override val type get() = TagType.Short
    override fun toString(): String = "${value}s"
}

@Serializable(with = IntTagSerializer::class)
class IntTag(override val value: Int) : Tag() {
    override val type get() = TagType.Int
    override fun toString(): String = value.toString()
}

@Serializable(with = LongTagSerializer::class)
class LongTag(override val value: Long) : Tag() {
    override val type get() = TagType.Long
    override fun toString(): String = "${value}l"
}

@Serializable(with = FloatTagSerializer::class)
class FloatTag(override val value: Float) : Tag() {
    override val type get() = TagType.Float
    override fun toString(): String = "${value}f"
}

@Serializable(with = DoubleTagSerializer::class)
class DoubleTag(override val value: Double) : Tag() {
    override val type get() = TagType.Double
    override fun toString(): String = value.toString()
}

@Serializable(with = ByteArrayTagSerializer::class)
class ByteArrayTag(override val value: ByteArray) : Tag() {
    override val type get() = TagType.ByteArray
    override fun toString(): String = value.contentToString()
}

@Serializable(with = StringTagSerializer::class)
class StringTag(override val value: String) : Tag() {
    override val type get() = TagType.String
    override fun toString(): String = "\"$value\""
}

@Serializable(with = ListTagSerializer::class)
class ListTag<T : Tag>(
    val elementType: TagType,
    override val value: List<T>,
) : Tag(), List<T> by value {
    override val type get() = TagType.List

    init {
        require(value.all { it.type == elementType }) {
            "All elements in ListTag must have type $elementType"
        }
    }

    override fun toString(): String = buildString { appendTo(this, 0) }

    override fun appendTo(builder: StringBuilder, indent: Int): Unit = with(builder) {
        append('[')
        var isFirst = true
        for (v in value) {
            if (isFirst) {
                isFirst = false
            } else {
                append(", ")
            }
            v.appendTo(this, indent)
        }
        append(']')
    }
}

typealias CompoundItems = Map<String, Tag>

@Serializable(with = CompoundTagSerializer::class)
class CompoundTag(
    override val value: CompoundItems,
) : Tag(), CompoundItems by value {
    override val type get() = TagType.Compound
    override fun toString(): String = buildString { appendTo(this, 0) }

    override fun appendTo(builder: StringBuilder, indent: Int): Unit = with(builder) {
        appendLine('{')
        for ((k, v) in value.toSortedMap()) {
            repeat(indent + 2) { append(' ') }
            append("$k: ")
            v.appendTo(this, indent + 2)
            appendLine()
        }
        repeat(indent) { append(' ') }
        append('}')
    }
}


@Serializable(with = IntArrayTagSerializer::class)
class IntArrayTag(override val value: IntArray) : Tag() {
    override val type get() = TagType.IntArray
    override fun toString(): String = value.contentToString()
}

@Serializable(with = LongArrayTagSerializer::class)
class LongArrayTag(override val value: LongArray) : Tag() {
    override val type get() = TagType.LongArray
    override fun toString(): String = value.contentToString()
}

/*
fun main() {
    for (i in TagType.values()) {
        println("""

@Serializable(with = ByteTagSerializer::class)
class ByteTag(override val value: Byte) : Tag() {
    override val type get() = TagType.Byte
}
        """.trimIndent()
            .replace("Byte", i.name)
        )
    }
}

*/
