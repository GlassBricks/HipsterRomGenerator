package me.glassbricks.knbt

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import java.util.zip.GZIPInputStream

@Serializable
data class BigTest(
    val byteTest: Byte = Byte.MAX_VALUE,
    val shortTest: Short = Short.MAX_VALUE,
    val intTest: Int = Int.MAX_VALUE,
    val longTest: Long = Long.MAX_VALUE,
    val floatTest: Float = 0.49823147058486938f,
    val doubleTest: Double = 0.49312871321823148,
    val stringTest: String = "HELLO WORLD THIS IS A TEST STRING ÅÄÖ!",
    val `listTest (long)`: List<Long> = listOf(11L, 12L, 13L, 14L, 15L),
    @SerialName(byteArrayName)
    val byteArrayTest: ByteArray = byteArray,
    val `listTest (compound)`: List<NestedCompound2> = listOf(
        NestedCompound2(name = "Compound tag #0"),
        NestedCompound2(name = "Compound tag #1")
    ),
    val `nested compound test`: NestedCompound = NestedCompound(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BigTest) return false

        if (byteTest != other.byteTest) return false
        if (shortTest != other.shortTest) return false
        if (intTest != other.intTest) return false
        if (longTest != other.longTest) return false
        if (floatTest != other.floatTest) return false
        if (doubleTest != other.doubleTest) return false
        if (stringTest != other.stringTest) return false
        if (`listTest (long)` != other.`listTest (long)`) return false
        if (!byteArrayTest.contentEquals(other.byteArrayTest)) return false
        if (`listTest (compound)` != other.`listTest (compound)`) return false
        if (`nested compound test` != other.`nested compound test`) return false

        return true
    }

    override fun hashCode(): Int {
        var result = byteTest.toInt()
        result = 31 * result + shortTest
        result = 31 * result + intTest
        result = 31 * result + longTest.hashCode()
        result = 31 * result + floatTest.hashCode()
        result = 31 * result + doubleTest.hashCode()
        result = 31 * result + stringTest.hashCode()
        result = 31 * result + `listTest (long)`.hashCode()
        result = 31 * result + byteArrayTest.contentHashCode()
        result = 31 * result + `listTest (compound)`.hashCode()
        result = 31 * result + `nested compound test`.hashCode()
        return result
    }
}

@Serializable
data class NestedCompound(
    val egg: Item = Item("Eggbert", 0.5f),
    val ham: Item = Item("Hampus", 0.75f),
)

@Serializable
data class Item(val name: String, val value: Float)

@Serializable
data class NestedCompound2(
    val `created-on`: Long = 1264099775885L,
    val name: String,
)


const val byteArrayName =
    "byteArrayTest (the first 1000 values of (n*n*255+n*7)%100, starting with n=0 (0, 62, 34, 16, 8, ...))"

val byteArray = ByteArray(
    1000) { n ->
    ((n * n * 255 + n * 7) % 100).toByte()
}

@Suppress("BlockingMethodInNonBlockingContext")
class NotchTest : FreeSpec({
    val expectedClass = BigTest()
    val expectedTag = compoundTag {
        "byteTest" eq Byte.MAX_VALUE
        "shortTest" eq Short.MAX_VALUE
        "intTest" eq Int.MAX_VALUE
        "longTest" eq Long.MAX_VALUE
        "floatTest" eq 0.49823147058486938f
        "doubleTest" eq 0.49312871321823148
        "stringTest" eq "HELLO WORLD THIS IS A TEST STRING ÅÄÖ!"
        "listTest (long)" eq listTag(11L, 12L, 13L, 14L, 15L)
        "listTest (compound)" eq listTag(
            compoundTag {
                "created-on" eq 1264099775885L
                "name" eq "Compound tag #0"
            },
            compoundTag {
                "created-on" eq 1264099775885L
                "name" eq "Compound tag #1"
            }
        )
        byteArrayName eq byteArray
        "nested compound test" eq compoundTag {
            "egg" eq compoundTag {
                "name" eq "Eggbert"
                "value" eq 0.5f
            }
            "ham" eq compoundTag {
                "name" eq "Hampus"
                "value" eq 0.75f
            }
        }
    }
    "mirror" - {
        setupMirrorTest(BigTest(), serializer())
        "from class" {
            Nbt.encodeToTag(serializer(), expectedClass) shouldBe expectedTag
        }
        "from tag" {
            Nbt.decodeFromTag<BigTest>(serializer(), expectedTag) shouldBe expectedClass
        }
    }
    "bigtest.nbt" - {
        val file = Thread.currentThread().contextClassLoader.getResource("bigtest.nbt")!!
        "tag" {
            val stream = GZIPInputStream(file.openStream())
            val tagFromBinary = stream.use { Nbt.parseToTagFromStream(it) }
            tagFromBinary shouldBe expectedTag
        }
        "class" {
            val stream = GZIPInputStream(file.openStream())
            val classFromBinary = stream.use { Nbt.decodeFromStream(it, serializer<BigTest>()) }
            classFromBinary shouldBe expectedClass
        }
    }
})

