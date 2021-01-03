package me.glassbricks.knbt

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor

@OptIn(ExperimentalSerializationApi::class)
internal abstract class AbstractNbtTagEncoder(
    nbt: Nbt,
    private val parent: AbstractNbtTagEncoder?,
) : AbstractNbtEncoder(nbt) {

    abstract fun putElement(tag: Tag)
    abstract fun getEncodedTag(): Tag

    override fun encodeByte(value: Byte) = putElement(ByteTag(value))
    override fun encodeShort(value: Short) = putElement(ShortTag(value))
    override fun encodeInt(value: Int) = putElement(IntTag(value))
    override fun encodeLong(value: Long) = putElement(LongTag(value))
    override fun encodeFloat(value: Float) = putElement(FloatTag(value))
    override fun encodeDouble(value: Double) = putElement(DoubleTag(value))
    override fun encodeString(value: String) = putElement(StringTag(value))

    override fun encodeByteArray(value: ByteArray) = putElement(ByteArrayTag(value))
    override fun encodeIntArray(value: IntArray) = putElement(IntArrayTag(value))
    override fun encodeLongArray(value: LongArray) = putElement(LongArrayTag(value))

    override fun encodeNbtTag(value: Tag) = putElement(value)

    override fun beginCompound() =
        NbtCompoundTagEncoder(nbt, this, null)

    override fun beginMap(): AbstractNbtEncoder = NbtMapTagEncoder(nbt, this)

    override fun beginList(collectionSize: Int, listType: TagType) =
        NbtListTagEncoder(nbt, this, listType, collectionSize)

    override fun endStructure(descriptor: SerialDescriptor) {
        parent?.putElement(getEncodedTag())
    }
}

//Not meant as a CompositeEncoder
internal class NbtRootTagEncoder(nbt: Nbt) : AbstractNbtTagEncoder(nbt, null) {
    private var theTag: Tag? = null

    override fun putElement(tag: Tag) {
        check(theTag == null)
        theTag = tag
    }

    override fun getEncodedTag(): Tag = theTag!!

}

@OptIn(ExperimentalSerializationApi::class)
internal class NbtCompoundTagEncoder(
    nbt: Nbt,
    parent: AbstractNbtTagEncoder,
    private var lastElementName: String?,
) : AbstractNbtTagEncoder(nbt, parent) {
    private var content = mutableMapOf<String, Tag>()

    override fun putElement(tag: Tag) {
        val name = lastElementName!!
        content[name] = tag
    }

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        lastElementName = descriptor.getElementName(index)
        return true
    }

    override fun getEncodedTag() = CompoundTag(content)
}

internal class NbtMapTagEncoder(
    nbt: Nbt,
    parent: AbstractNbtTagEncoder,
) : AbstractNbtTagEncoder(nbt, parent) {
    private var content = mutableMapOf<String, Tag>()

    private lateinit var key: String
    private var isKey = true

    override fun putElement(tag: Tag) {
        if (isKey) {
            key = tag.value.toString()
        } else {
            content[key] = tag
        }
        isKey = !isKey
    }

    override fun getEncodedTag(): Tag = CompoundTag(content)
}

internal class NbtListTagEncoder(
    nbt: Nbt,
    parent: AbstractNbtTagEncoder,
    private val listType: TagType,
    private val listSize: Int,
) : AbstractNbtTagEncoder(nbt, parent) {
    private val items = arrayOfNulls<Tag>(listSize)
    private var index = 0

    override fun putElement(tag: Tag) {
        if (tag.type != listType) throw SerializationException("Unexpected type ${tag.type} in NBT list encoding")
        if (index > items.lastIndex) throw SerializationException("Got more than expected $listSize items in list")

        items[index++] = tag
    }

    override fun getEncodedTag(): Tag {
        if (index != listSize) throw SerializationException("Expected $listSize items in list, only got $index")
        @Suppress("UNCHECKED_CAST")
        return ListTag(listType, items.asList() as List<Tag>)
    }
}

