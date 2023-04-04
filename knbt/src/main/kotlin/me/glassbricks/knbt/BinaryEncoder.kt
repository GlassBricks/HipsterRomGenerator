package me.glassbricks.knbt

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import java.io.DataOutput


internal abstract class AbstractNbtBinaryEncoder(
    nbt: Nbt,
    protected val output: DataOutput,
) : AbstractNbtEncoder(nbt) {

    protected fun writeTagType(tagType: TagType) {
        output.writeByte(tagType.id.toInt())
    }

    protected abstract fun beforeEncodeValue(tagType: TagType)

    private inline fun doEncode(tagType: TagType, rawEncode: DataOutput.() -> Unit) {
        beforeEncodeValue(tagType)
        output.rawEncode()
    }

    override fun encodeByte(value: Byte) = doEncode(TagType.Byte) { writeByte(value.toInt()) }
    override fun encodeShort(value: Short) = doEncode(TagType.Short) { writeShort(value.toInt()) }
    override fun encodeInt(value: Int) = doEncode(TagType.Int) { writeInt(value) }
    override fun encodeLong(value: Long) = doEncode(TagType.Long) { writeLong(value) }
    override fun encodeFloat(value: Float) = doEncode(TagType.Float) { writeFloat(value) }
    override fun encodeDouble(value: Double) = doEncode(TagType.Double) { writeDouble(value) }
    override fun encodeString(value: String) = doEncode(TagType.String) { writeUTF(value) }

    override fun encodeByteArray(value: ByteArray) = doEncode(TagType.ByteArray) {
        writeInt(value.size)
        write(value)
    }

    override fun encodeIntArray(value: IntArray) = doEncode(TagType.IntArray) {
        writeInt(value.size)
        for (i in value) writeInt(i)
    }

    override fun encodeLongArray(value: LongArray) = doEncode(TagType.LongArray) {
        writeInt(value.size)
        for (i in value) writeLong(i)
    }

    override fun encodeNbtTag(value: Tag) = encodeSerializableValue(TagSerializer, value)

    // structure encoding

    override fun beginCompound(): AbstractNbtEncoder {
        beforeEncodeValue(TagType.Compound)
        return NbtBinaryEncoder(nbt, output, null)
    }

    override fun beginMap(): AbstractNbtEncoder {
        beforeEncodeValue(TagType.Compound)
        return NbtBinaryMapEncoder(nbt, output)
    }

    override fun beginList(collectionSize: Int, listType: TagType): AbstractNbtEncoder {
        beforeEncodeValue(TagType.List)
        writeTagType(listType)
        output.writeInt(collectionSize)
        return NbtBinaryListEncoder(nbt, output, listType, collectionSize)
    }

}

@OptIn(ExperimentalSerializationApi::class)
internal open class NbtBinaryEncoder(
    nbt: Nbt,
    output: DataOutput,
    private var lastElementName: String?,
) : AbstractNbtBinaryEncoder(nbt, output) {

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        lastElementName = descriptor.getElementName(index)
        return true
    }

    override fun beforeEncodeValue(tagType: TagType) {
        val name = lastElementName!!
        writeTagType(tagType)
        output.writeUTF(name)
    }


    override fun encodeNull() {
        lastElementName = null
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        writeTagType(TagType.End)
    }
}

internal open class NbtBinaryMapEncoder(
    nbt: Nbt, output: DataOutput,
) : AbstractNbtBinaryEncoder(nbt, output) {
    private var isKey = true
    private lateinit var key: String
    override fun beforeEncodeValue(tagType: TagType) {
        if (isKey) {
            //should have called encodeKey() instead
            throw InvalidKeyKind(tagType.name)
        }
        //is value
        writeTagType(tagType)
        output.writeUTF(key)
        isKey = true
    }

    private fun encodeKey(key: String) {
        this.key = key
        isKey = false
    }

    override fun encodeBoolean(value: Boolean) = if (isKey) encodeKey(value.toString()) else super.encodeBoolean(value)
    override fun encodeByte(value: Byte) = if (isKey) encodeKey(value.toString()) else super.encodeByte(value)
    override fun encodeShort(value: Short) = if (isKey) encodeKey(value.toString()) else super.encodeShort(value)
    override fun encodeInt(value: Int) = if (isKey) encodeKey(value.toString()) else super.encodeInt(value)
    override fun encodeLong(value: Long) = if (isKey) encodeKey(value.toString()) else super.encodeLong(value)
    override fun encodeString(value: String) = if (isKey) encodeKey(value) else super.encodeString(value)

    override fun endStructure(descriptor: SerialDescriptor) {
        writeTagType(TagType.End)
    }
}

internal class NbtBinaryListEncoder(
    nbt: Nbt,
    output: DataOutput,
    private val listType: TagType,
    private val listSize: Int,
) : AbstractNbtBinaryEncoder(nbt, output) {
    private var index = 0

    override fun beforeEncodeValue(tagType: TagType) {
        if (tagType != listType) throw SerializationException("NBT list encoding: unexpected tag type $tagType")
        if (++index > listSize) throw SerializationException("Got more than expected $listSize items in list, got more")
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        if (index != listSize) throw SerializationException("Expected $listSize items in list, only got $index")
    }

}
