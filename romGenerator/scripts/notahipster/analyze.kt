package notahipster

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.schem.writeSchematic
import java.io.File

class AnalyzeSeq : StringSpec({
    "analyze" {
        val fullSeq = moves.flatMap { it.value ?: emptyList() }

        val PinkModSize = 18
        var pinkPos = -8 + 18

        val heightToWaits = List(PinkModSize) { sortedSetOf<Int>() }

        var lastPurple: Int? = null

        for ((i, move) in fullSeq.withIndex()) when (move) {
            Move.pink -> {
                pinkPos = (pinkPos + 1) % PinkModSize
            }

            Move.purple -> lastPurple = if (lastPurple == null) {
                i
            } else {
                val value = i - lastPurple!! - 1
                heightToWaits[pinkPos] += value
                null
            }

            else -> {}
        }

        for (height in 0 until PinkModSize) {
            println("$height: ${heightToWaits[height].joinToString()}")
        }
    }

    "annotate seq" {
        val waits = intArrayOf(17, 19, 20, 22, 23, 25, 27)
        var curPinkPos = -8 + 18

        fun annotateFile(
            file: List<MoveLine>,
            waits: IntArray
        ): MutableList<MoveLine> {
            var lastWait: MoveLine? = null
            var lastPurple: MoveLine? = null
            var lastPurpleIndex = -1

            val newList = mutableListOf<MoveLine>()

            var i = 0
            for (cur in file) {
                val (move, n) = cur
                when (move) {
                    Move.pink -> {
                        curPinkPos = (curPinkPos + n) % 18
                    }

                    Move.purple -> {
                        if (lastPurpleIndex == -1) {
                            lastPurpleIndex = i
                            lastPurple = cur
                            lastWait = null
                        } else {
                            lastPurple!!.comment = "height $curPinkPos"
                            cur.comment = "end    $curPinkPos"

                            val nWaits = i - lastPurpleIndex - 1
                            val expected = waits[curPinkPos]

                            val diff = nWaits - expected

                            lastWait!!.count -= diff

//                            val newCount = lastWait!!.count - diff
//                            if (newCount != lastWait!!.count) {
//                                lastWait!!.comment = "maybe $newCount"
//                            }

                            lastPurpleIndex = -1
                            lastPurple = null
                        }
                    }

                    Move.wait -> {
                        lastWait = cur
                    }

                    else -> {}
                }
                newList.add(cur)
                i += n
            }
            return newList
        }

        for (filename in fileNames) {
            val f = File("9x9fs moves/$filename")
            if (!f.exists()) continue
            val file = f
                .readLines()
                .filter { it.isNotBlank() }
                .map(::parseLine)


            // write new file
            f.writeText(annotateFile(file, waits).joinToString("\n", postfix = "\n"))
        }
    }
})
