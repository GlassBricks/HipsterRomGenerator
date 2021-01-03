package me.glassbricks.knbt


fun listTag(vararg items: Byte): ListTag<ByteTag> = ListTag(TagType.Byte, items.map(::ByteTag))
fun listTag(vararg items: Short): ListTag<ShortTag> = ListTag(TagType.Short, items.map(::ShortTag))
fun listTag(vararg items: Int): ListTag<IntTag> = ListTag(TagType.Int, items.map(::IntTag))
fun listTag(vararg items: Long): ListTag<LongTag> = ListTag(TagType.Long, items.map(::LongTag))
fun listTag(vararg items: Float): ListTag<FloatTag> = ListTag(TagType.Float, items.map(::FloatTag))
fun listTag(vararg items: Double): ListTag<DoubleTag> = ListTag(TagType.Double, items.map(::DoubleTag))
fun listTag(vararg items: ByteArray): ListTag<ByteArrayTag> = ListTag(TagType.ByteArray, items.map(::ByteArrayTag))
fun listTag(vararg items: String): ListTag<StringTag> = ListTag(TagType.String, items.map(::StringTag))
fun listTag(vararg items: ListTag<*>): ListTag<ListTag<*>> = ListTag(TagType.List, items.asList())
fun listTag(vararg items: CompoundTag): ListTag<CompoundTag> = ListTag(TagType.Compound, items.asList())
fun listTag(vararg items: IntArray): ListTag<IntArrayTag> = ListTag(TagType.IntArray, items.map(::IntArrayTag))
fun listTag(vararg items: LongArray): ListTag<LongArrayTag> = ListTag(TagType.LongArray, items.map(::LongArrayTag))


fun main() {
    for (i in TagType.values()) {
        println("""
fun listTag(vararg items: Byte): ListTag<ByteTag> = ListTag(TagType.Byte, items.map(::ByteTag))
        """.trimIndent().replace("Byte", i.name))
    }
}
