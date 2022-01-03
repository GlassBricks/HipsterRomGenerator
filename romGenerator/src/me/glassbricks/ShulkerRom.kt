package me.glassbricks.rom

import me.glassbricks.sequence.PistonSequence
import me.glassbricks.sequence.SequenceElement
import me.glassbricks.sequence.SequenceMove

class Encoding<M : SequenceElement?>(
    val encoding: Map<M, Int>,
    val numBits: Int,
) {
    init {
        require(numBits in 0 until 32)
    }

    fun encode(seq: Sequence<M>): List<BitSequence> {
        return List(numBits) { bit ->
            seq.map { encoding[it]!! and (1 shl bit) != 0 }
        }
    }

}

@JvmInline
value class ItemStack private constructor(val count: Int) {
    companion object {
        const val MAX_VALUE = 64
        val unstackable get() = ItemStack(0)
        fun stackable(count: Int): ItemStack {
            require(count in 1..MAX_VALUE)
            return ItemStack(count)
        }

    }

    override fun toString(): String = if (count == 0) {
        "##"
    } else {
        "%2d".format(count)
    }
}

fun <M : SequenceElement?> encodeToItems(sequence: Sequence<M>, encoding: Encoding<M>): List<List<ItemStack>> =
    encoding.encode(sequence).map {
        buildList {
            var curStack = 0 // 0 == last was unstackable
            for (bit in it) {
                if (bit) {
                    if (curStack != 0) add(ItemStack.stackable(curStack))
                    curStack = 0
                    add(ItemStack.unstackable)
                } else {
                    curStack++
                    if (curStack > ItemStack.MAX_VALUE) {
                        add(ItemStack.stackable(ItemStack.MAX_VALUE))
                        curStack -= ItemStack.MAX_VALUE
                    }
                }
            }
            if (curStack != 0) add(ItemStack.stackable(curStack))
        }
    }


typealias BitSequence = Sequence<Boolean>

const val CHEST_MAX_STACKS = 27

data class ShulkerBox(val items: List<ItemStack>) {
    override fun toString(): String = items.joinToString(separator = ",", prefix = "Box(", postfix = ")")

    companion object {
        const val MAX_STACKS = CHEST_MAX_STACKS
        const val MIN_STACKS = 3 // actually min items but 1 stack at least 1 item

    }
}


fun toShulkerBoxes(stacks: List<ItemStack>): List<ShulkerBox> {
    val boxes = stacks.chunked(ShulkerBox.MAX_STACKS).mapTo(mutableListOf(), ::ShulkerBox)
    // ensure last box has minimum amount items
    val last = boxes.last()
    val lastCount = last.items.count()
    if (lastCount < ShulkerBox.MIN_STACKS) {
        val toMove = ShulkerBox.MIN_STACKS - lastCount
        val secondToLast = boxes[boxes.lastIndex - 1]
        val newSecondToLast = secondToLast.items.dropLast(toMove)
        val newLast = secondToLast.items.takeLast(toMove) + last.items

        boxes.apply {
            set(lastIndex - 1, ShulkerBox(newSecondToLast))
            set(lastIndex, ShulkerBox(newLast))
        }
    }
    return boxes
}

class ShulkerRom(
    val channels: List<List<ShulkerBox>>
) {
    override fun toString(): String {
        return channels.toString()
    }
}

fun <M : SequenceMove> PistonSequence<M>.toRom(encoding: Encoding<M>): ShulkerRom = toRom(moves, encoding)

fun <M : SequenceMove> toRom(sequence: Sequence<M>, encoding: Encoding<M>): ShulkerRom = ShulkerRom(
    encodeToItems(sequence, encoding).map(::toShulkerBoxes)
)
