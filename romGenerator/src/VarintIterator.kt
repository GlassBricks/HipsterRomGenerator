import java.util.*

class VarIntIterator(private val source: ByteArray) : Iterator<Int>{
    private var index = 0
    private var hasNextInt = false
    private var nextInt = 0

    override fun hasNext(): Boolean {
        if (hasNextInt) {
            return true
        }
        if (index >= source.size) {
            return false
        }
        nextInt = readNextInt()
        return true.also { hasNextInt = it }
    }

    private fun readNextInt(): Int {
        var value = 0
        var bitsRead = 0
        while (true) {
            check(index < source.size) { "Ran out of bytes while reading VarInt (probably corrupted data)" }
            val next = source[index]
            index++
            value = value or (next.toInt() and 0x7F shl bitsRead)
            check(bitsRead <= 7 * 5) { "VarInt too big (probably corrupted data)" }
            if (next.toInt() and 0x80 == 0) {
                break
            }
            bitsRead += 7
        }
        return value
    }

    override fun next(): Int {
        if (!hasNext()) {
            throw NoSuchElementException()
        }
        hasNextInt = false
        return nextInt
    }
}
