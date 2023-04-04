package me.glassbricks.knbt

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.LongArraySerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

interface NbtEncoder : Encoder {
    val nbt: Nbt
    fun encodeNbtTag(value: Tag)
}

@OptIn(ExperimentalSerializationApi::class)
internal abstract class AbstractNbtEncoder(final override val nbt: Nbt) : AbstractEncoder(), NbtEncoder {
    final override val serializersModule: SerializersModule = nbt.serializersModule
    override fun shouldEncodeElementDefault(descriptor: SerialDescriptor, index: Int): Boolean =
        nbt.conf.encodeDefaults

    // Encode value
    abstract override fun encodeByte(value: Byte)
    abstract override fun encodeShort(value: Short)
    abstract override fun encodeInt(value: Int)
    abstract override fun encodeLong(value: Long)
    abstract override fun encodeFloat(value: Float)
    abstract override fun encodeDouble(value: Double)
    abstract override fun encodeString(value: String)

    abstract fun encodeByteArray(value: ByteArray)
    abstract fun encodeIntArray(value: IntArray)
    abstract fun encodeLongArray(value: LongArray)

    override fun encodeBoolean(value: Boolean) = encodeByte(if (value) 1 else 0)
    override fun encodeChar(value: Char) = encodeShort(value.toShort())
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) =
        encodeString(enumDescriptor.getElementName(index))

    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        return when (serializer) {
            ByteArraySerializer() -> encodeByteArray(value as ByteArray)
            IntArraySerializer() -> encodeIntArray(value as IntArray)
            LongArraySerializer() -> encodeLongArray(value as LongArray)
            else -> serializer.serialize(this, value)
        }
    }

    //encode structure

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder =
        when (descriptor.kind) {
            StructureKind.LIST -> beginList(collectionSize, getListType(descriptor.getElementDescriptor(0)))
            else -> beginStructure(descriptor)
        }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder =
        when (descriptor.kind) {
            StructureKind.LIST -> throw SerializationException(
                "NBT LIST serialization must know list size in advance. " +
                        "Use beginCollection instead of beginStructure."
            )
            StructureKind.MAP -> tryBeginMap(descriptor)
            else -> beginCompound()
        }

    abstract fun beginCompound(): AbstractNbtEncoder
    abstract fun beginMap(): AbstractNbtEncoder
    abstract fun beginList(collectionSize: Int, listType: TagType): AbstractNbtEncoder

    private fun tryBeginMap(descriptor: SerialDescriptor): CompositeEncoder {
        val keyDescriptor = descriptor.getElementDescriptor(0)
        val keyKind = keyDescriptor.kind
        if (keyKind is PrimitiveKind || keyKind == SerialKind.ENUM) {
            return beginMap()
        } else {
            throw InvalidKeyKind(keyDescriptor.toString())
        }
    }


    private fun getListType(elementDescriptor: SerialDescriptor) =
        when (elementDescriptor.kind) {
            PrimitiveKind.BOOLEAN, PrimitiveKind.BYTE -> TagType.Byte
            PrimitiveKind.CHAR, PrimitiveKind.SHORT -> TagType.Short
            PrimitiveKind.INT -> TagType.Int
            PrimitiveKind.LONG -> TagType.Long
            PrimitiveKind.FLOAT -> TagType.Float
            PrimitiveKind.DOUBLE -> TagType.Double
            PrimitiveKind.STRING, SerialKind.ENUM -> TagType.String
            StructureKind.CLASS, StructureKind.OBJECT, StructureKind.MAP -> TagType.Compound
            StructureKind.LIST -> when (elementDescriptor) {
                ByteArraySerializer().descriptor -> TagType.ByteArray
                IntArraySerializer().descriptor -> TagType.IntArray
                LongArraySerializer().descriptor -> TagType.LongArray
                else -> TagType.List
            }
            PolymorphicKind.SEALED, PolymorphicKind.OPEN, SerialKind.CONTEXTUAL -> TagType.Compound
        }
}
