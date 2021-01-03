package me.glassbricks.knbt

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.serializer

@Serializable
private data class WithMap<K, V>(val map: Map<K, V>)


class MapTest : FreeSpec({
    "example" - {
        setupMirrorTest(WithMap(mapOf(
            "one" to 1,
            "thweee" to 3,
            "two" to 2,
            "four" to 40,
        )), serializer())
    }

    "int, int" - {
        Arb.map(Arb.int(), Arb.int()).checkAll {
            doMirrorTest(WithMap(it), serializer())
        }
    }
    "string, int" - {
        Arb.map(Arb.string(), Arb.int()).checkAll {
            doMirrorTest(WithMap(it), serializer())
        }
    }
    "byte, string" - {
        Arb.map(Arb.byte(), Arb.string()).checkAll {
            doMirrorTest(WithMap(it), serializer())
        }
    }
    "invalid map" {
        shouldThrow<SerializationException> {
            doMirrorTest(WithMap(mapOf(1f to "yo")), serializer())
        }
    }
})