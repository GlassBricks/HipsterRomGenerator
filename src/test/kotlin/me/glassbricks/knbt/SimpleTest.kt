package me.glassbricks.knbt

import io.kotest.core.spec.style.FreeSpec
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer

//Exposed serialization for debugging purposes

@Serializable(with = SimpleSerializer::class)
data class Simple(val a: Int)

object SimpleSerializer : KSerializer<Simple> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("simple") {
        element<Int>("a")
    }

    override fun serialize(encoder: Encoder, value: Simple) {
        val composite = encoder.beginStructure(descriptor)
        try {
            composite.encodeIntElement(descriptor, 0, value.a)
        } finally {
            composite.endStructure(descriptor)
        }
    }

    override fun deserialize(decoder: Decoder): Simple {
        var a: Int? = null
        val composite = decoder.beginStructure(descriptor)
        while (true) when (composite.decodeElementIndex(descriptor)) {
            -1 -> break
            0 -> {
                a = composite.decodeIntElement(descriptor, 0)
            }
            else -> throw SerializationException()
        }
        composite.endStructure(descriptor)
        return Simple(checkNotNull(a))
    }
}

class SimpleTest : FreeSpec({
    "simple" - {
        setupMirrorTest(Simple(1), serializer())
    }
})