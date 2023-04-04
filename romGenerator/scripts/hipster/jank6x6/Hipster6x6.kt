package hipster.jank6x6

import hipster.jank6x6.Move.*
import me.glassbricks.sequence.RsSequenceVisitor
import me.glassbricks.splitCamelCase

@Suppress("EnumEntryName")
enum class Move(strName: String? = null) {
    bot,
    mid,
    fold,
    t1("1t"),
    t3("3t"),
    t4("4t"),
    jank("J"),
    o("obs"),
    b("bobs");

    val strName = strName ?: name.splitCamelCase()

    override fun toString(): String = strName
}

private typealias B = RsSequenceVisitor<Move>

fun B.store() {
    add(t4, o, o)
}

fun B.retract2(obsOut: Boolean) {
    if (obsOut) {
        add(o, o, b, b)
    } else {
        add(o, b, b, o)
    }
    add(mid, t4, t1)
}

fun B.row2(obsOut: Boolean) {
    add(
        mid,
        t4, t1, t1,
    )
    retract2(obsOut)
}


val J = arrayOf(jank, t1, t3, o)

fun B.retract3(obsOut: Boolean) {
    add(*J, t1, t1)
    retract2(obsOut)
}

fun B.row3() {
    add(mid, t4)
    retract3(false)
}

fun B.retract4() {
    add(o, t4, t4, o)
    halfRetract4(false)
}

fun B.halfRetract4(obsOut: Boolean) {
    row2(obsOut)
    add(t1, bot, mid)
    retract3(obsOut)
}

fun B.row4() {
    add(bot, t4)
    retract4()
}

fun B.extend5() {
    add(o, mid)
    t1 * 4
    +b; o * 6; +b
    add(
        mid,
        t4, t1, t4,
        o,
        mid, t4,
        *J, t4, mid, t1, t1
    )
}

fun B.retract5() {
    extend5()
    retract4()
}

fun B.row5() {
    add(bot, t4)
    retract5()
}

fun B.row6() {
    add(bot, t4)
    retract6()
}

fun B.retract6() {
    add(
        o,
        mid,
        t4, b,
        *J, *J,
    )
    add(
        t1, t1,
        o, o, b, b,
        mid,
        t4, t1, t4
    )
    add(
        b, o,
        fold, bot,
        t4,
    )
    add(o, t4, t4, o)
    row2(false)
    +t4
    add(
        bot, mid, mid, t1, t1, fold
    )
    retract5()
}

fun B.row7() {
    add(
        bot,
        t4, o,
        fold, bot,
        t4, b,
        o, t4, t4, t4, t4, o,
    )
    halfRetract4(true); +t4
    add(b, o)

    add(bot, t4)
    extend5()

    row2(false); +t4
    add(bot, mid, mid, t1, t1, fold)
    retract6()
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


val encoding = mapOf(
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
