package me.glassbricks.knbt

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.*
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*


@OptIn(ExperimentalSerializationApi::class)
internal abstract class AbstractTagSerializer<T : Tag>(
    val type: TagType?,
    delegateDescriptor: SerialDescriptor,
) : KSerializer<T> {

    override val descriptor: SerialDescriptor = object : SerialDescriptor by delegateDescriptor {
        override val serialName: String = "me.glassbricks.knbt.${type?.name.orEmpty()}Tag"
    }

    abstract fun serialize(encoder: AbstractNbtEncoder, value: T)
    abstract fun deserialize(decoder: AbstractNbtDecoder): T

    final override fun serialize(encoder: Encoder, value: T) {
        if (encoder !is AbstractNbtEncoder)
            throw SerializationException("Tag objects can (currently) only be encoded with built-in NbtEncoder")
        if (encoder is AbstractNbtTagEncoder) {
            //encode directly
            encoder.encodeNbtTag(value)
        } else {
            serialize(encoder, value)
        }
    }

    final override fun deserialize(decoder: Decoder): T {
        if (decoder !is AbstractNbtDecoder)
            throw SerializationException("Tag objects can (currently) only be encoded with built-in NbtDecoder")
        if (type != null) decoder.checkTagType(type)
        return if (decoder is AbstractNbtTagDecoder) {
            //decode directly
            @Suppress("UNCHECKED_CAST")
            decoder.decodeNbtTag() as T
        } else {
            deserialize(decoder)
        }
    }
}

private val typeToSerializer = arrayOf(
    ByteTagSerializer,
    ShortTagSerializer,
    IntTagSerializer,
    LongTagSerializer,
    FloatTagSerializer,
    DoubleTagSerializer,
    ByteArrayTagSerializer,
    StringTagSerializer,
    ListTagSerializer,
    CompoundTagSerializer,
    IntArrayTagSerializer,
    LongArrayTagSerializer
).associateByTo(EnumMap<TagType, AbstractTagSerializer<*>>(TagType::class.java)) { it.type }

@Suppress("UNCHECKED_CAST")
private fun getSerializerByType(type: TagType): AbstractTagSerializer<Tag> {
    return typeToSerializer[type] as AbstractTagSerializer<Tag>? ?: throw AssertionError("No serializer for type $type")
}


@OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
internal object TagSerializer : AbstractTagSerializer<Tag>(
    null,
    buildSerialDescriptor("me.glassbricks.knbt.Tag", PolymorphicKind.SEALED)
) {

    override fun serialize(encoder: AbstractNbtEncoder, value: Tag) {
        getSerializerByType(value.type).serialize(encoder, value)
    }

    override fun deserialize(decoder: AbstractNbtDecoder): Tag {
        val type = decoder.getElementType()
        return getSerializerByType(type).deserialize(decoder)
    }
}

internal abstract class DelegatingTagSerializer<V : Any, T : Tag>(
    private val delegateSerializer: KSerializer<V>,
    type: TagType,
) : AbstractTagSerializer<T>(type, delegateSerializer.descriptor) {
    override fun serialize(encoder: AbstractNbtEncoder, value: T) {
        @Suppress("UNCHECKED_CAST")
        encoder.encodeSerializableValue(delegateSerializer, value.value as V)
    }

    override fun deserialize(decoder: AbstractNbtDecoder): T {
        return createTag(decoder.decodeSerializableValue(delegateSerializer))
    }

    abstract fun createTag(value: V): T
}


internal object ByteTagSerializer : DelegatingTagSerializer<Byte, ByteTag>(Byte.serializer(), TagType.Byte) {
    override fun createTag(value: Byte) = ByteTag(value)
}

internal object ShortTagSerializer : DelegatingTagSerializer<Short, ShortTag>(Short.serializer(), TagType.Short) {
    override fun createTag(value: Short) = ShortTag(value)
}

internal object IntTagSerializer : DelegatingTagSerializer<Int, IntTag>(Int.serializer(), TagType.Int) {
    override fun createTag(value: Int) = IntTag(value)
}

internal object LongTagSerializer : DelegatingTagSerializer<Long, LongTag>(Long.serializer(), TagType.Long) {
    override fun createTag(value: Long) = LongTag(value)
}

internal object FloatTagSerializer : DelegatingTagSerializer<Float, FloatTag>(Float.serializer(), TagType.Float) {
    override fun createTag(value: Float) = FloatTag(value)
}

internal object DoubleTagSerializer : DelegatingTagSerializer<Double, DoubleTag>(Double.serializer(), TagType.Double) {
    override fun createTag(value: Double) = DoubleTag(value)
}

internal object ByteArrayTagSerializer :
    DelegatingTagSerializer<ByteArray, ByteArrayTag>(ByteArraySerializer(), TagType.ByteArray) {
    override fun createTag(value: ByteArray) = ByteArrayTag(value)
}

internal object StringTagSerializer : DelegatingTagSerializer<String, StringTag>(String.serializer(), TagType.String) {
    override fun createTag(value: String) = StringTag(value)
}

@OptIn(ExperimentalSerializationApi::class)
internal object ListTagSerializer : AbstractTagSerializer<ListTag<*>>(
    TagType.List,
    listSerialDescriptor(Tag.serializer().descriptor)
) {

    override fun serialize(encoder: AbstractNbtEncoder, value: ListTag<*>) {
        val elementType = value.elementType
        val elementSerializer = getSerializerByType(elementType)

        with(encoder.beginList(value.value.size, elementType)) {
            for ((index, tag) in value.value.withIndex()) {
                encodeSerializableElement(descriptor, index, elementSerializer, tag)
            }
            endStructure(descriptor)
        }

    }

    override fun deserialize(decoder: AbstractNbtDecoder): ListTag<*> {
        with(decoder.beginList()) {
            check(decodeSequentially())
            val listSize = decodeCollectionSize(descriptor)
            val elementType = getElementType()
            val elementSerializer = getSerializerByType(elementType)

            val list = Array(listSize) {
                decodeSerializableElement(descriptor, it, elementSerializer)
            }

            return ListTag(elementType, list.asList())
                .also { endStructure(descriptor) }
        }
    }
}

internal object CompoundTagSerializer : DelegatingTagSerializer<CompoundItems, CompoundTag>(
    MapSerializer(String.serializer(), TagSerializer), TagType.Compound
) {
    override fun createTag(value: CompoundItems) = CompoundTag(value)
}

internal object IntArrayTagSerializer :
    DelegatingTagSerializer<IntArray, IntArrayTag>(IntArraySerializer(), TagType.IntArray) {
    override fun createTag(value: IntArray) = IntArrayTag(value)
}

internal object LongArrayTagSerializer :
    DelegatingTagSerializer<LongArray, LongArrayTag>(LongArraySerializer(), TagType.LongArray) {
    override fun createTag(value: LongArray) = LongArrayTag(value)
}

/*
fun main() {
    for (i in TagType.values()) {
        println("""
internal object ByteTagSerializer : DelegatingTagSerializer<Byte, ByteTag>(Byte.serializer(), TagType.Byte) {
    override fun createTag(value: Byte) = ByteTag(value)
}

        """.trimIndent().replace("Byte", i.name))
    }
    for (i in TagType.values()) {
        println("TagType.${i.name} -> encoder.encode${i.name}(value.value as ${i.name})")
    }
}
*/
