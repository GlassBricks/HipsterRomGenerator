package me.glassbricks.knbt

import io.kotest.core.spec.style.FunSpec
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable


@Serializable
data class SingleByte(val value: Byte)

@Serializable
data class SingleShort(val value: Short)

@Serializable
data class SingleInt(val value: Int)

@Serializable
data class SingleLong(val value: Long)

@Serializable
data class SingleFloat(val value: Float)

@Serializable
data class SingleDouble(val value: Double)

@Serializable
data class SingleByteArray(val value: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SingleByteArray

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}

@Serializable
data class SingleString(val value: String)

@Serializable
data class SingleList(val value: List<Int>)

@Serializable
data class SingleCompound(val value: SingleInt)

@Serializable
data class SingleIntArray(val value: IntArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SingleIntArray

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}

@Serializable
data class SingleLongArray(val value: LongArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SingleLongArray

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}

private fun allSingles() = listOf(
    SingleByte(1) to SingleByte.serializer(),
    SingleShort(1) to SingleShort.serializer(),
    SingleInt(1) to SingleInt.serializer(),
    SingleLong(1) to SingleLong.serializer(),
    SingleFloat(1f) to SingleFloat.serializer(),
    SingleDouble(1.0) to SingleDouble.serializer(),
    SingleByteArray(byteArrayOf(1)) to SingleByteArray.serializer(),
    SingleString("1") to SingleString.serializer(),
    SingleList(listOf(1)) to SingleList.serializer(),
    SingleCompound(SingleInt(1)) to SingleCompound.serializer(),
    SingleIntArray(IntArray(1)) to SingleIntArray.serializer(),
    SingleLongArray(LongArray(1)) to SingleLongArray.serializer(),
)
//
//fun main() {
//    for (i in TagType.values()) {
//        println("""
//        @Serializable
//        class SingleInt(val value: Int)
//
//        """.trimIndent().replace("Int", i.name))
//    }
//    for (i in TagType.values()) {
//        println("""
//            SingleInt(1) to SingleInt.serializer(),
//        """.trimIndent().replace("Int", i.name))
//    }
//}

class SingleValueTest : FunSpec({
    allSingles().forEach { (value, serializer) ->
        context(value::class.simpleName!!) {
            @Suppress("UNCHECKED_CAST")
            (setupMirrorTest(value, serializer as KSerializer<Any>))
        }
    }
})