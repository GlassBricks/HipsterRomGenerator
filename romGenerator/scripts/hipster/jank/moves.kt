package hipster.jank

import hipster.jank.Move.*
import me.glassbricks.sequence.RsSequenceVisitor
import me.glassbricks.splitCamelCase

@Suppress("EnumEntryName")
enum class Move(strName: String? = null) {
    bot,
    mid,
    fold,
    fold2,
    t1("1t"),
    t3("3t"),
    t4("4t"),
    jank("J"),
    o,
    b,
    back;


    private val strName = strName ?: name.splitCamelCase()

    override fun toString(): String = strName
}

private typealias B = RsSequenceVisitor<Move>

fun B.store() {
    add(t4, o, o)
}

fun B.row2WithPistonsOut(obsOut: Boolean) {
    add(t1, t1)
    if (obsOut) {
        add(o, o, b, b)
    } else {
        add(o, b, b, o)
    }
    add(mid, t4, t1)
}

/** Also pull2 */
fun B.row2(obsOut: Boolean) {
    add(mid, t4)
    row2WithPistonsOut(obsOut)
}


val J = arrayOf(jank, t1, t3, o)

fun B.powRetract3(obsOut: Boolean) {
    add(*J)
    row2WithPistonsOut(obsOut)
}

fun B.row3() {
    add(mid, t4)
    powRetract3(false)
}

fun B.powRetract4() {
    add(o, t4, t4, o)
    retract4(false)
}

fun B.retract4(obsOut: Boolean) {
    row2(obsOut); +t1
    add(bot, mid)
    powRetract3(obsOut)
}

fun B.row4() {
    add(bot, t4)
    powRetract4()
}

/** Extension and first part of retraction */
fun B.extendPull5() {
    add(o, mid)
    t1 * 4
    +b; o * 6; +b
    add(
        mid, t4, t1, t4,
        o,
        mid, t4,
        *J, t4, mid, t1, t1
    )
}

fun B.powRetract5() {
    extendPull5()
    powRetract4()
}

fun B.row5() {
    add(bot, t4)
    powRetract5()
}

fun B.row6() {
    add(bot, fold, t4)
    powRetract6()
}

fun B.unFold() {
    add(bot, mid, mid, fold, t1, t1)
}

fun B.powRetract6() {
    // extend
    add(
        o,
        mid,
        t4, b,
        *J, *J,
    )
    // retract obs
    // row3, but it's already powered down; so row2
    row2WithPistonsOut(true); +t4
    add(b, o)

    // pull 4
    add(bot, t4)
    add(o, t4, t4, o)
    row2(false); +t4

    unFold()
    powRetract5()
}

fun B.row7() {
    add(
        bot, fold,
        t4, o, bot, t4,
        b,
        o, t4, t4, t4, t4, o,
    )

    // retract obs at row 4
    retract4(true); +t4
    add(b, o)

    // pull 5
    add(bot, t4)
    extendPull5()
    row2(false); +t4

    unFold()
    powRetract6()
}

fun B.seq6() {
    store()
    row2(false)
    store()
    row3()
    store()
    row4()
    store()
    row5()
    store()
    row6()
    t1 * 3
}


fun B.seq7() {
    +t4
    store()
    row2(false)
    store()
    row3()
    store()
    row4()
    store()
    row5()
    store()
    row6()
    store()
    row7()
    t1 * 3
}

fun B.only7() {
    row7()
    t1 * 3
}


val encoding1 = mapOf(
    mid to 1,
    bot to 2,
    b to 4,
    fold to 5,
    o to 7,
    jank to 8,
    t1 to 11,
    t3 to 12,
    t4 to 13,
)
