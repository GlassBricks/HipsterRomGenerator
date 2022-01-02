package me.glassbricks.rom

import me.glassbricks.rom.ItemStack.Companion.STACK_MAX
import me.glassbricks.rom.ShulkerBox.Companion.BOX_MIN_STACKS
import me.glassbricks.sequence.MoveSequence
import me.glassbricks.sequence.SequenceItem

const val NUM_BITS = 3

private typealias BitSequence = Sequence<Boolean>

private fun <T> Sequence<T>.toBitLists(encoding: Map<out T, Int>): List<BitSequence> {
    return List(NUM_BITS) { bit ->
        this.map { encoding[it]!! and (1 shl bit) != 0 }
    }
}


class ItemStack private constructor(val count: Int) {

    companion object {
        val unstackable = ItemStack(0)
        fun stackable(count: Int): ItemStack {
            require(count in 1..STACK_MAX)
            return ItemStack(count)
        }

        const val STACK_MAX = 64
    }

    override fun toString(): String =
        if (count == 0) {
            "##"
        } else {
            "%2d".format(count)
        }
}

fun BitSequence.toItemsStacks(): List<ItemStack> = buildList {
    var stackCount = 0 // 0 == last was unstackable
    for (b in this@toItemsStacks) {
        if (b) {
            if (stackCount != 0) add(ItemStack.stackable(stackCount))
            stackCount = 0
            add(ItemStack.unstackable)
        } else {
            stackCount++
            if (stackCount > STACK_MAX) {
                add(ItemStack.stackable(STACK_MAX))
                stackCount -= STACK_MAX
            }
        }
    }
    if (stackCount != 0) add(ItemStack.stackable(stackCount))
}

const val CHEST_MAX_STACKS = 27

data class ShulkerBox(val items: List<ItemStack>) {
    override fun toString(): String = items.joinToString(separator = ",", prefix = "Box(", postfix = ")")

    companion object {
        const val BOX_MIN_STACKS = 3 // actually MIN_ITEMS but 1 stack at least 1 item
    }
}

private fun List<ItemStack>.toBoxes(): List<ShulkerBox> =
    chunked(CHEST_MAX_STACKS)
        .map(::ShulkerBox)

private fun List<ShulkerBox>.balanced(): List<ShulkerBox> {
    val last = last()
    val lastCount = last.items.count()
    return if (lastCount >= BOX_MIN_STACKS) this else {
        val toMove = BOX_MIN_STACKS - lastCount
        val secondToLast = this[lastIndex - 1]
        val newSecondToLast = secondToLast.items.dropLast(toMove)
        val newLast = secondToLast.items.takeLast(toMove) + last.items

        toMutableList().apply {
            set(lastIndex - 1, ShulkerBox(newSecondToLast))
            set(lastIndex, ShulkerBox(newLast))
        }
    }
}

typealias ShulkerRom = List<List<ShulkerBox>>

fun MoveSequence.toRom(encoding: Map<out SequenceItem, Int>): ShulkerRom = flattened.toRom(encoding)

fun <T> Sequence<T>.toRom(encoding: Map<out T, Int>): ShulkerRom =
    toItemsStacks(encoding)
        .map {
            it.toBoxes()
                .balanced()
        }

fun <T> Sequence<T>.toItemsStacks(encoding: Map<out T, Int>): List<List<ItemStack>> =
    toBitLists(encoding)
        .map(BitSequence::toItemsStacks)
