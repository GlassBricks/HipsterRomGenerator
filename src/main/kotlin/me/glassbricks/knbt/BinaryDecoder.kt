package me.glassbricks.knbt

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import java.io.DataInput

@OptIn(ExperimentalSerializationApi::class)
internal abstract class AbstractNbtBinaryDecoder(
    nbt: Nbt,
    protected val input: DataInput,
) : AbstractNbtDecoder(nbt) {

    protected fun readTagType(): TagType {
        val id = input.readByte()
        return TagType.fromId(id) ?: throw SerializationException("Unknown tag id $id")
    }

    protected open fun beforeDecodeValue(requestedType: TagType) {
        checkTagType(requestedType)
    }

    private inline fun <T> doDecode(requestedType: TagType, rawDecode: DataInput.() -> T): T {
        beforeDecodeValue(requestedType)
        return input.rawDecode()
    }

    override fun decodeByte() = doDecode(TagType.Byte) { readByte() }
    override fun decodeShort() = doDecode(TagType.Short) { readShort() }
    override fun decodeInt() = doDecode(TagType.Int) { readInt() }
    override fun decodeLong() = doDecode(TagType.Long) { readLong() }
    override fun decodeFloat() = doDecode(TagType.Float) { readFloat() }
    override fun decodeDouble() = doDecode(TagType.Double) { readDouble() }
    override fun decodeString(): String = doDecode(TagType.String) { readUTF() }

    override fun decodeByteArray(): ByteArray = doDecode(TagType.ByteArray) {
        val size = readInt()
        return ByteArray(size).also { readFully(it) }
    }

    override fun decodeIntArray(): IntArray = doDecode(TagType.IntArray) {
        val size = readInt()
        return IntArray(size) { readInt() }
    }

    override fun decodeLongArray(): LongArray = doDecode(TagType.LongArray) {
        val size = readInt()
        return LongArray(size) { readLong() }
    }

    override fun decodeNbtTag(): Tag = decodeSerializableValue(TagSerializer)

    override fun beginCompound(): AbstractNbtDecoder = NbtBinaryDecoder(nbt, input)
    override fun beginMap(): AbstractNbtDecoder = NbtBinaryMapDecoder(nbt, input)
    override fun beginList(): AbstractNbtDecoder {
        val listType = readTagType()
        val listSize = input.readInt()
        return NbtBinaryListDecoder(nbt, input, listType, listSize)
    }
}

@OptIn(ExperimentalSerializationApi::class)
internal open class NbtBinaryDecoder(
    nbt: Nbt,
    input: DataInput,
) : AbstractNbtBinaryDecoder(nbt, input) {

    protected var lastTagType: TagType? = null

    /** reads and sets [lastTagType], returns tag name */
    protected fun readNamedTag(): String {
        val tagType = readTagType().also { lastTagType = it }
        return if (tagType == TagType.End) "" else input.readUTF()
    }

    override fun getElementType(): TagType = lastTagType!!

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        val name = readNamedTag()

        if (lastTagType !== TagType.End)
            return descriptor.getElementIndex(name)

        return CompositeDecoder.DECODE_DONE
    }

    fun onDecodeRoot() {
        readNamedTag()
        if (lastTagType != TagType.Compound) throw IllegalStateException("Root element must be of type compound")
    }
}

internal open class NbtBinaryMapDecoder(
    nbt: Nbt, input: DataInput,
) : NbtBinaryDecoder(nbt, input) {
    private var position = -1 // index = position / 2
    private val isKey get() = position % 2 == 0
    private lateinit var key: String

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        position++
        if (isKey) {
            key = readNamedTag()
            if (lastTagType === TagType.End) return CompositeDecoder.DECODE_DONE
        }
        return position
    }

    override fun beforeDecodeValue(requestedType: TagType) {
        if (isKey) {
            //should have called one of the below overrides instead
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
internal class NbtBinaryListDecoder(
    nbt: Nbt,
    input: DataInput,
    private val listType: TagType,
    private val listSize: Int,
) : AbstractNbtBinaryDecoder(nbt, input) {
    private var index = -1

    override fun getElementType() = listType

    override fun decodeSequentially(): Boolean = true
    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = listSize

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (++index < listSize) return index
        return CompositeDecoder.DECODE_DONE
    }
}