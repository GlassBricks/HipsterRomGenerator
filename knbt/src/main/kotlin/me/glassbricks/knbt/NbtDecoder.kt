package me.glassbricks.knbt

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.LongArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule


interface NbtDecoder : Decoder {
    val nbt: Nbt
    fun decodeNbtTag(): Tag
}

@OptIn(ExperimentalSerializationApi::class)
internal abstract class AbstractNbtDecoder(override val nbt: Nbt) : AbstractDecoder(), NbtDecoder {
    override val serializersModule: SerializersModule
        get() = EmptySerializersModule

    abstract fun getElementType(): TagType

    fun checkTagType(requestedType: TagType) {
        val nextTagType = getElementType()
        if (nextTagType != requestedType)
            throw SerializationException("Found tag type ${nextTagType}, but tried to decode with tag type $requestedType")
    }

    protected open fun beforeDecodeValue(requestedType: TagType) {
        checkTagType(requestedType)
    }

    abstract override fun decodeByte(): Byte
    abstract override fun decodeShort(): Short
    abstract override fun decodeInt(): Int
    abstract override fun decodeLong(): Long
    abstract override fun decodeFloat(): Float
    abstract override fun decodeDouble(): Double
    abstract override fun decodeString(): String

    protected abstract fun decodeByteArray(): ByteArray
    protected abstract fun decodeIntArray(): IntArray
    protected abstract fun decodeLongArray(): LongArray

    override fun decodeBoolean() = decodeByte() != 0.toByte()
    override fun decodeChar() = decodeShort().toChar()
    override fun decodeEnum(enumDescriptor: SerialDescriptor) =
        enumDescriptor.getElementIndex(decodeString())

    @Suppress("UNCHECKED_CAST")
    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        return when (deserializer) {
            ByteArraySerializer() -> decodeByteArray() as T
            IntArraySerializer() -> decodeIntArray() as T
            LongArraySerializer() -> decodeLongArray() as T
            else -> deserializer.deserialize(this)
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return when (descriptor.kind) {
            StructureKind.LIST -> {
                beforeDecodeValue(TagType.List)
                beginList()
            }
            StructureKind.MAP -> {
                beforeDecodeValue(TagType.Compound)
                beginMap()
            }
            else -> {
                beforeDecodeValue(TagType.Compound)
                beginCompound()
            }
        }
    }

    abstract fun beginCompound(): AbstractNbtDecoder
    abstract fun beginMap(): AbstractNbtDecoder
    abstract fun beginList(): AbstractNbtDecoder

}