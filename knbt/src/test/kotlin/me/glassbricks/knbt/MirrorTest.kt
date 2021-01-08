package me.glassbricks.knbt

import io.kotest.core.spec.style.scopes.ContainerScope
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToByteArray


@Suppress("SuspendFunctionOnCoroutineScope")
suspend fun <T> ContainerScope.setupMirrorTest(value: T, serializer: KSerializer<T>) {
    // object -> binary -> object
    val binary = async { Nbt.encodeToByteArray(serializer, value) }
    val nbt = async { Nbt.encodeToTag(serializer, value) }

    addTest("object -> binary -> object") {
        binaryMirror(serializer, value, binary.await())
    }
    addTest("object -> nbt -> object") {
        tagMirror(serializer, value, nbt.await())
        println(nbt.await())
    }
    addTest("object -> binary -> nbt -> object:") {
        binaryTriMirror(serializer, value, binary.await())
    }
    addTest("object -> nbt -> binary -> object:") {
        tagTriMirror(serializer, value, nbt.await())
    }
}

fun <T> doMirrorTest(value: T, serializer: KSerializer<T>) {
    val binary = Nbt.encodeToByteArray(serializer, value)
    val nbt = Nbt.encodeToTag(serializer, value)
    binaryMirror(serializer, value, binary)
    tagMirror(serializer, value, nbt)
    binaryTriMirror(serializer, value, binary)
    tagTriMirror(serializer, value, nbt)
}

private fun <T> binaryMirror(
    serializer: KSerializer<T>,
    value: T,
    bytes: ByteArray,
) {
    val fromBinary = Nbt.decodeFromByteArray(serializer, bytes)
    fromBinary shouldBe value
}

private fun <T> tagMirror(
    serializer: KSerializer<T>,
    value: T,
    tag: Tag,
) {
    val fromTag = Nbt.decodeFromTag(serializer, tag)
    fromTag shouldBe value
}

private fun <T> binaryTriMirror(
    serializer: KSerializer<T>,
    value: T,
    bytes: ByteArray,
) {
    val nbtFromBinary = Nbt.decodeToTag(bytes)
    val fromNbtFromBinary = Nbt.decodeFromTag(serializer, nbtFromBinary)
    fromNbtFromBinary shouldBe value
}

private fun <T> tagTriMirror(
    serializer: KSerializer<T>,
    value: T,
    tag: Tag,
) {
    val binaryFromNbt = Nbt.encodeToByteArray(tag)
    val fromBinaryFromNbt = Nbt.decodeFromByteArray(serializer, binaryFromNbt)
    fromBinaryFromNbt shouldBe value
}
