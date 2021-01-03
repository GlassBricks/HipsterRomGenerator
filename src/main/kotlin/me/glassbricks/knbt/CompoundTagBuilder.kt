package me.glassbricks.knbt


@DslMarker
annotation class NbtDsl

@NbtDsl
class CompoundTagBuilder {
    val items = mutableMapOf<String, Tag>()

    infix fun String.to(tag: Tag) {
        items[this] = tag
    }

    infix fun String.to(value: Byte) {
        this to ByteTag(value)
    }

    infix fun String.to(value: Short) {
        this to ShortTag(value)
    }

    infix fun String.to(value: Int) {
        this to IntTag(value)
    }

    infix fun String.to(value: Long) {
        this to LongTag(value)
    }

    infix fun String.to(value: Float) {
        this to FloatTag(value)
    }

    infix fun String.to(value: Double) {
        this to DoubleTag(value)
    }

    infix fun String.to(value: ByteArray) {
        this to ByteArrayTag(value)
    }

    infix fun String.to(value: String) {
        this to StringTag(value)
    }

    infix fun String.to(value: CompoundItems) {
        this to CompoundTag(value.toMap()) as Tag
    }

    infix fun String.to(value: IntArray) {
        this to IntArrayTag(value)
    }

    infix fun String.to(value: LongArray) {
        this to LongArrayTag(value)
    }

    @JvmName("listByte")
    infix fun String.to(value: List<Byte>) {
        this to ListTag(TagType.Byte, value.map(::ByteTag))
    }

    @JvmName("listShort")
    infix fun String.to(value: List<Short>) {
        this to ListTag(TagType.Short, value.map(::ShortTag))
    }

    @JvmName("listInt")
    infix fun String.to(value: List<Int>) {
        this to ListTag(TagType.Int, value.map(::IntTag))
    }

    @JvmName("listLong")
    infix fun String.to(value: List<Long>) {
        this to ListTag(TagType.Long, value.map(::LongTag))
    }

    @JvmName("listFloat")
    infix fun String.to(value: List<Float>) {
        this to ListTag(TagType.Float, value.map(::FloatTag))
    }

    @JvmName("listDouble")
    infix fun String.to(value: List<Double>) {
        this to ListTag(TagType.Double, value.map(::DoubleTag))
    }

    @JvmName("listByteArray")
    infix fun String.to(value: List<ByteArray>) {
        this to ListTag(TagType.ByteArray, value.map(::ByteArrayTag))
    }

    @JvmName("listString")
    infix fun String.to(value: List<String>) {
        this to ListTag(TagType.String, value.map(::StringTag))
    }

    @JvmName("listList")
    infix fun String.to(value: List<ListTag<*>>) {
        this to ListTag(TagType.List, value.toList()) as Tag
    }

    @JvmName("listIntArray")
    infix fun String.to(value: List<IntArray>) {
        this to ListTag(TagType.IntArray, value.map(::IntArrayTag))
    }

    @JvmName("listLongArray")
    infix fun String.to(value: List<LongArray>) {
        this to ListTag(TagType.LongArray, value.map(::LongArrayTag))
    }


    fun build() = CompoundTag(items)
}

inline fun compoundTag(builder: CompoundTagBuilder.() -> Unit) = CompoundTagBuilder().apply(builder).build()

/*
fun main() {
    for (i in TagType.values()) {
        println("""
    @JvmName("listInt")
    infix fun String.to(value: List<Int>) {
        this to ListTag(TagType.Int, value.map(::IntTag))
    }
        """.trimIndent().replace("Int", i.name))
    }
}*/
