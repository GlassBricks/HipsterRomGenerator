package me.glassbricks.rom

import me.glassbricks.rom.ItemStack.Companion.STACK_MAX
import me.glassbricks.rom.ShulkerBox.Companion.BOX_MIN_STACKS
import me.glassbricks.sequence.PistonSequence

const val NUM_BITS = 3

private typealias BitSequence = Sequence<Boolean>

private fun PistonSequence.toBitLists(): List<BitSequence> {
    return List(NUM_BITS) { bit ->
        this.flattened.map { it.encoding.toInt() and (1 shl bit) != 0 }
    }
}


sealed class ItemStack {
    object Unstackable : ItemStack()
    class Stackable(val count: Int) : ItemStack() {
        init {
            require(count in 1..STACK_MAX)
        }
    }

    override fun toString(): String {
        return when (this) {
            Unstackable -> "##"
            is Stackable -> "%2d".format(count)
        }
    }

    companion object {
        const val STACK_MAX = 64
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun BitSequence.toItems(): List<ItemStack> = buildList {
    var stackCount = 0 // 0 == last was unstackable
    for (b in this@toItems) {
        if (b) {
            if (stackCount != 0) add(ItemStack.Stackable(stackCount))
            stackCount = 0
            add(ItemStack.Unstackable)
        } else {
            stackCount++
            if (stackCount > STACK_MAX) {
                add(ItemStack.Stackable(STACK_MAX))
                stackCount -= STACK_MAX
            }
        }
    }
    if (stackCount != 0) add(ItemStack.Stackable(stackCount))
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

typealias BoxList = List<ShulkerBox>

fun PistonSequence.toRom(): List<BoxList> =
        toBitLists().map {
            it.toItems()
                    .toBoxes()
                    .balanced()
        }
