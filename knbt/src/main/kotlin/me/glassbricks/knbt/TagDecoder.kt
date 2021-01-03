package me.glassbricks.knbt

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder

@Suppress("UNCHECKED_CAST")
internal abstract class AbstractNbtTagDecoder(
    nbt: Nbt,
) : AbstractNbtDecoder(nbt) {

    abstract fun getCurrentElement(): Tag
    override fun getElementType(): TagType = getCurrentElement().type

    protected open fun <T> doDecode(requestedType: TagType): T {
        beforeDecodeValue(requestedType)
        return (getCurrentElement().value as T)
    }

    override fun decodeByte(): Byte = doDecode(TagType.Byte)
    override fun decodeShort(): Short = doDecode(TagType.Short)
    override fun decodeInt(): Int = doDecode(TagType.Int)
    override fun decodeLong(): Long = doDecode(TagType.Long)
    override fun decodeFloat(): Float = doDecode(TagType.Float)
    override fun decodeDouble(): Double = doDecode(TagType.Double)
    override fun decodeString(): String = doDecode(TagType.String)

    override fun decodeByteArray(): ByteArray = doDecode(TagType.ByteArray)
    override fun decodeIntArray(): IntArray = doDecode(TagType.IntArray)
    override fun decodeLongArray(): LongArray = doDecode(TagType.LongArray)

    override fun decodeNbtTag(): Tag = getCurrentElement()

    override fun beginCompound(): AbstractNbtDecoder = NbtCompoundTagDecoder(nbt, getCurrentElement() as CompoundTag)
    override fun beginMap(): AbstractNbtDecoder = NbtMapTagDecoder(nbt, getCurrentElement() as CompoundTag)
    override fun beginList(): AbstractNbtDecoder = NbtListTagDecoder(nbt, getCurrentElement() as ListTag<*>)
}

//Not meant as a CompositeDecoder
internal class NbtRootTagDecoder(
    nbt: Nbt,
    private val value: Tag,
) : AbstractNbtTagDecoder(nbt) {
    override fun getCurrentElement(): Tag = value

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int =
        throw UnsupportedOperationException()
}

@OptIn(ExperimentalSerializationApi::class)
internal class NbtCompoundTagDecoder(
    nbt: Nbt,
    value: CompoundTag,
) : AbstractNbtTagDecoder(nbt) {
    private val iterator = value.value.iterator()
    private var currentElement: Tag? = null

    override fun getCurrentElement(): Tag = currentElement!!

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (iterator.hasNext()) {
            val (name, tag) = iterator.next()
            currentElement = tag
            return descriptor.getElementIndex(name)
        }
        currentElement = null
        return CompositeDecoder.DECODE_DONE
    }
}

internal class NbtMapTagDecoder(
    nbt: Nbt,
    value: CompoundTag,
) : AbstractNbtTagDecoder(nbt) {
    private var position = -1
    private val isKey get() = position % 2 == 0

    private val iterator = value.value.iterator()
    private lateinit var entry: Map.Entry<String, Tag>
    private val key get() = entry.key

    override fun getCurrentElement(): Tag {
        check(!isKey)
        return entry.value
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        position++
        if (isKey) {
            if (iterator.hasNext())
                entry = iterator.next()
            else return CompositeDecoder.DECODE_DONE
        }
        return position
    }

    override fun beforeDecodeValue(requestedType: TagType) {
        if (isKey) {
            throw InvalidKeyKind(requestedType.name)
        } else super.beforeDecodeValue(requestedType)
    }

    override fun decodeBoolean(): Boolean = if (isKey) key.toBooleanStrict() else super.decodeBoolean()
    override fun decodeByte(): Byte = if (isKey) key.toByte() else super.decodeByte()
    override fun decodeShort(): Short = if (isKey) key.toShort() else super.decodeShort()
    override fun decodeInt(): Int = if (isKey) key.toInt() else super.decodeInt()
    override fun decodeLong(): Long = if (isKey) key.toLong() else super.decodeLong()
    override fun decodeString(): String = if (isKey) key else super.decodeString()
}


@OptIn(ExperimentalSerializationApi::class)
internal class NbtListTagDecoder(
    nbt: Nbt,
    private val value: ListTag<*>,
) : AbstractNbtTagDecoder(nbt) {
    private val list: List<Tag> get() = value.value
    private var index = -1

    private var decodeSequentially = false

    override fun getElementType(): TagType = value.elementType
    override fun getCurrentElement(): Tag = list[index]

    override fun decodeSequentially(): Boolean {
        decodeSequentially = true
        return true
    }

    override fun beforeDecodeValue(requestedType: TagType) {
        if (decodeSequentially) index++
        super.beforeDecodeValue(requestedType)
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = list.size

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (++index < list.size) return index
        return CompositeDecoder.DECODE_DONE
    }


}
