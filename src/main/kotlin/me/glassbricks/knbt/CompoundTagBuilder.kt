package me.glassbricks.knbt


@NbtBuilder
class CompoundTagBuilder {
    val items = mutableMapOf<String, Tag>()

    infix fun String.eq(tag: Tag) {
        items[this] = tag
    }

    infix fun String.eq(value: Byte) {
        this eq ByteTag(value)
    }

    infix fun String.eq(value: Short) {
        this eq ShortTag(value)
    }

    infix fun String.eq(value: Int) {
        this eq IntTag(value)
    }

    infix fun String.eq(value: Long) {
        this eq LongTag(value)
    }

    infix fun String.eq(value: Float) {
        this eq FloatTag(value)
    }

    infix fun String.eq(value: Double) {
        this eq DoubleTag(value)
    }

    infix fun String.eq(value: ByteArray) {
        this eq ByteArrayTag(value)
    }

    infix fun String.eq(value: String) {
        this eq StringTag(value)
    }

    infix fun String.eq(value: IntArray) {
        this eq IntArrayTag(value)
    }

    infix fun String.eq(value: LongArray) {
        this eq LongArrayTag(value)
    }

    fun build(): CompoundTag = CompoundTag(items.toMap())
}

inline fun compoundTag(builder: CompoundTagBuilder.() -> Unit): CompoundTag =
    CompoundTagBuilder().apply(builder).build()

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
