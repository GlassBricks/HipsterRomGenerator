package me.glassbricks.knbt

import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
private data class WithList<T>(val list: List<T>)


class ListTest : FreeSpec({
    "example" - {
        setupMirrorTest(WithList("I ate some pie today".split(" ")), serializer())
    }

    "int" - {
        Arb.list(Arb.int()).checkAll {
            doMirrorTest(WithList(it), serializer())
        }
    }
    "float" - {
        Arb.list(Arb.float()).checkAll {
            doMirrorTest(WithList(it), serializer())
        }
    }
    "string" - {
        Arb.list(Arb.string()).checkAll {
            doMirrorTest(WithList(it), serializer())
        }
    }
})