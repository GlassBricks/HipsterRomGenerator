package me.glassbricks.knbt

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import java.io.*

@OptIn(ExperimentalSerializationApi::class)
sealed class Nbt(internal val conf: NbtConf) : BinaryFormat {
    companion object Default : Nbt(NbtConf())

    override val serializersModule: SerializersModule
        get() = conf.serializersModule

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val stream = ByteArrayOutputStream()
        encodeToStream(stream, serializer, value)
        return stream.toByteArray()
    }

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val stream = ByteArrayInputStream(bytes)
        return decodeFromStream(stream, deserializer)
    }

    fun <T> encodeToStream(stream: OutputStream, serializer: SerializationStrategy<T>, value: T) {
        DataOutputStream(stream.buffered()).use {
            NbtBinaryEncoder(this, it, "").encodeSerializableValue(serializer, value)
        }
    }

    fun <T> decodeFromStream(stream: InputStream, deserializer: DeserializationStrategy<T>): T {
        return DataInputStream(stream.buffered()).use {
            NbtBinaryDecoder(this, it).run {
                onDecodeRoot()
                decodeSerializableValue(deserializer)
            }
        }
    }

    fun <T> encodeToTag(serializer: SerializationStrategy<T>, value: T): Tag {
        NbtRootTagEncoder(this).run {
            encodeSerializableValue(serializer, value)
            return getEncodedTag()
        }
    }

    fun <T> decodeFromTag(deserializer: DeserializationStrategy<T>, tag: Tag): T {
        return NbtRootTagDecoder(this, tag).run {
            decodeSerializableValue(deserializer)
        }
    }

    fun decodeToTag(binary: ByteArray): Tag {
        return decodeFromByteArray(TagSerializer, binary)
    }

    fun decodeToTagFromStream(stream: InputStream): Tag {
        return decodeFromStream(stream, TagSerializer)
    }

}

fun Nbt(from: Nbt = Nbt.Default, builder: NbtBuilder.() -> Unit): Nbt {
    return NbtImpl(NbtBuilder(from.conf).apply(builder).build())
}

@OptIn(ExperimentalSerializationApi::class)
internal class NbtConf constructor(
    val serializersModule: SerializersModule = EmptySerializersModule,
    val encodeDefaults: Boolean = false,
    val ignoreUnknownKeys: Boolean = false,
)

class NbtBuilder internal constructor(conf: NbtConf) {
    var module: SerializersModule = conf.serializersModule
    var encodeDefaults: Boolean = conf.encodeDefaults
    var ignoreUnknownKeys: Boolean = conf.ignoreUnknownKeys
    internal fun build(): NbtConf = NbtConf(
        module, encodeDefaults, ignoreUnknownKeys
    )
}

private class NbtImpl(conf: NbtConf) : Nbt(conf)
