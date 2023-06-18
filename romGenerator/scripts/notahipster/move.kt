package notahipster


@Suppress("EnumEntryName")
enum class Move {
    e,
    d,
    dpe,
    tpe,
    g,
    lb1t,
    lb4t,
    f,
    worm,
    wait,
    purple,
    pink,
}

val encoding = mapOf(
    Move.e to 2,
    Move.d to 3,
    Move.dpe to 4,
    Move.tpe to 5,
    Move.g to 6,
    Move.lb1t to 8,
    Move.lb4t to 9,
    Move.f to 10,
    Move.worm to 11,
    Move.wait to 12,
    Move.purple to 13,
    Move.pink to 15,
)
