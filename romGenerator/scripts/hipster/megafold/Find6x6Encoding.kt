package hipster.megafold

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.schem.BinaryEncoding
import me.glassbricks.schem.encodeToItems

class Find6x6Encoding : StringSpec({
    "print sequence" {
        println(special6x6Sequence.size)
        println(special6x6Sequence.joinToString(separator = "\n"))
    }

    "find minimal encoding" {
        findMinimalEncoding(normal6x6Sequence)
    }

    "find minimal encoding 2" {
        findMinimalEncoding(special6x6Sequence)
    }
})

fun findMinimalEncoding(
    sequence: List<Move>,
) {
    data class EncodingStat(
        val encoding: Map<Move, Int>,
        val stacksNeeded: Int,
    )

    val moves = sequence.distinct()

    val curMins = mutableListOf<EncodingStat>()
    permutations(moves.size)
        .map { arr ->
            moves.indices.associate { i ->
                moves[i] to arr[i]
            }
        }
        .forEach { encodingMap ->
            val encoding = BinaryEncoding(encodingMap, 3)
            val stackLists = encodeToItems(sequence, encoding)
            val stacksNeeded = stackLists.maxOf { it.size }
            val stat = EncodingStat(encodingMap, stacksNeeded)

            val curMin = curMins.firstOrNull()?.stacksNeeded ?: Int.MAX_VALUE

            if (stacksNeeded < curMin) {
                curMins.clear()
            }
            if (stacksNeeded <= curMin) {
                curMins.add(stat)
            }
        }

    println("Min stacks needed: " + curMins[0].stacksNeeded)

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
