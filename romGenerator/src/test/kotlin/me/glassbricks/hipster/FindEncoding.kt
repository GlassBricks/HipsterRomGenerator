package me.glassbricks.hipster

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.rom.toItemsStacks
import me.glassbricks.sequence.SequenceItem

class FindSpecial6x6 : StringSpec({
    fun findMinimalEncoding(
        sequence: List<SequenceItem>,
        values: List<SequenceItem>,
    ) {
        data class EncodingStat(
            val encoding: Map<*, Int>,
            val stacksNeeded: Int,
        )


        val curMins = mutableListOf<EncodingStat>()
        permutations(8)
            .map { arr ->
                (0 until 8).associate { i ->
                    values.getOrNull(i) to arr[i]
                }
            }
            .forEach { encoding ->
                val stackLists = sequence.asSequence().toItemsStacks(encoding)
                val stacksNeeded = stackLists.maxOf { it.size }
                val stat = EncodingStat(encoding, stacksNeeded)

                val curMin = curMins.firstOrNull()?.stacksNeeded ?: Int.MAX_VALUE
                val chestsNeeded = stat.stacksNeeded

                if (chestsNeeded < curMin) {
                    curMins.clear()
                }
                if (chestsNeeded <= curMin) {
                    curMins.add(stat)
                }
            }

        println(curMins[0].stacksNeeded)

        curMins.take(32).forEach { encodingStat ->
            encodingStat.encoding.entries
                .sortedBy { it.value }
                .forEach { (k, v) ->
                    val vStr = v.toString(2).padStart(3, '0')
                    println("${k.toString().padEnd(14)} to 0b$vStr,")
                }
            println()
        }
    }
    "find minimal encoding" {
        findMinimalEncoding(
            normal6x6Sequence.flattened.toList(),
            enumValues<HipsterMove>().toList()
        )
    }

    "find minimal encoding 2" {
        findMinimalEncoding(
            special6x6Sequence.toList(),
            enumValues<HipsterMove>().filterNot { it == HipsterMove.Store }
        )
    }
})

private fun permutations(n: Int): Sequence<IntArray> = sequence {
    doPermut(
        BooleanArray(n),
        IntArray(n),
        0,
        n
    )
}

private suspend fun SequenceScope<IntArray>.doPermut(
    used: BooleanArray,
    current: IntArray,
    k: Int,
    n: Int,
) {
    if (k == n) {
        yield(current)
        return
    }
    for (i in 0 until n) {
        if (used[i]) continue
        used[i] = true
        current[k] = i
        doPermut(used, current, k + 1, n)
        used[i] = false
    }
}