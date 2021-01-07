package me.glassbricks.rom

import me.glassbricks.rom.ItemStack.Companion.STACK_MAX
import me.glassbricks.rom.ShulkerBox.Companion.BOX_MAX_STACKS
import me.glassbricks.rom.ShulkerBox.Companion.BOX_MIN_ITEMS
import me.glassbricks.sequence.PistonSequence

const val NUM_CHANNELS = 3

private typealias BitSequence = Sequence<Boolean>

private fun PistonSequence.toBitLists(): List<BitSequence> {
    return List(NUM_CHANNELS) { bit ->
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

    val numItems
        get() = when (this) {
            Unstackable -> 1
            is Stackable -> count
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
private fun BitSequence.toItems(): Sequence<ItemStack> = sequence {
    var stackCount = 0 // 0 == last was unstackable
    for (b in this@toItems) {
        if (b) {
            if (stackCount != 0) yield(ItemStack.Stackable(stackCount))
            stackCount = 0
            yield(ItemStack.Unstackable)
        } else {
            stackCount++
            if (stackCount > STACK_MAX) {
                yield(ItemStack.Stackable(STACK_MAX))
                stackCount -= STACK_MAX
            }
        }
    }
    if (stackCount != 0) yield(ItemStack.Stackable(stackCount))
}


data class ShulkerBox(val items: List<ItemStack>) {
    fun numItems() = items.sumBy { it.numItems }
    override fun toString(): String = items.joinToString(separator = ",", prefix = "Box(", postfix = ")")

    companion object {
        const val BOX_MAX_STACKS = 27
        const val BOX_MIN_ITEMS = 3
    }
}

private fun Sequence<ItemStack>.toBoxes(): List<ShulkerBox> = chunked(BOX_MAX_STACKS, ::ShulkerBox).toList()

private fun List<ShulkerBox>.balanced(): List<ShulkerBox> {
    val last = last()
    val lastCount = last.numItems()
    return if (lastCount >= BOX_MIN_ITEMS) this else {
        val toMove = lastCount - BOX_MIN_ITEMS
        val secondToLast = this[lastIndex - 1]
        //simply just move 3 stacks; at LEAST 3 items
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
    this.toBitLists()
        .map {
            it.toItems()
                .toBoxes()
                .balanced()
        }
