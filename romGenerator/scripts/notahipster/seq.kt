package notahipster

private typealias B = SeqGen

fun B.row1234() {
    tpe
    d
    dpe
    lb4t
    lb1t
    dpe
    d
    tpe
    d
    dpe
    lb1t
    dpe
    tape(0)
    dpe
    tpe
    lb4t
    lb1t
    dpe
    d
    tpe
    e
    dpe
    lb1t
    tpe
    row4Retract()
}

fun B.row4Retract(
    andTape5: Boolean = false,
) {
    tape(1)

    val b: MoveBlock = {
        tpe
        d
        dpe
        lb4t
        lb1t
        dpe
        d
        tpe
        lb1t
        dpe
    }
    if (andTape5) {
        tape(5, b)
    } else {
        b()
    }

    tape(0) {
        e
        d
    }
    dpe
    tpe
    lb4t
    lb1t
    dpe
    d
    tpe
}

fun B.row5() {
    f
    e
    dpe
    lb1t
    tpe
    row5Retract()
}

fun B.row5Retract() {
    tape(2) {
        tpe
        d
        dpe
    }
    lb4t
    lb1t
    dpe
    d
    tpe
    lb1t
    dpe
    tape(0) {
        e
        d
        f
        e
    }
    dpe
    tpe
    d
    tpe
    lb1t
    tpe
    row4Retract()
}


fun B.row6() {
    f
    e
    dpe
    lb1t
    d
    dpe
    lb4t
    tape(3) {
        lb1t
        dpe
        d
        tpe
        d
        dpe
        lb1t
        dpe
    }
    tape(0)
    row6Retract()
}

fun B.row6Retract() {
    dpe
    tpe
    d
    tpe
    lb1t
    tpe
    tape(1)
    row5Retract()
}

fun B.row7() {
    g
    f
    dpe
    lb1t
    d
    dpe
    lb1t
    dpe
    tape(0)
    dpe
    tpe
    tape(4) {
        lb4t
        lb1t
        dpe
        d
        tpe
        e
        dpe
        lb1t
        tpe
    }
    tape(1)
    row5Retract()
}

fun B.row8() {
    g
    f
    dpe
    lb1t
    e
    dpe
    lb1t
    tpe
    row8retract()
}

private fun B.row8retract() {
    row4Retract(andTape5 = true)
    e
    dpe
    lb1t
    d
    dpe
    lb4t
    lb1t
    dpe
    tape(2)
    tape(3) {
        lb4t
        lb1t
        dpe
        d
        tpe
        d
        dpe
        lb1t
        dpe
    }
    tape(0)
    dpe
    tpe
    tape(4) {
        lb4t
        lb1t
        dpe
        d
        tpe
        lb4t
        tpe
    }
    tape(1)
    tape(2) {
        tpe
        d
        dpe
    }
    lb4t
    lb1t
    dpe
    d
    tpe
    tape(3) {
        lb1t
        dpe
    }
    tape(0) {
        e
        d
        f
        e
        g
        f
    }
    row6Retract()
}

fun B.row9() {
    g
    lb1t
    lb1t
    f
    e
    dpe
    worm
    lb1t
    lb1t
    tpe
    tape(2) {
        tpe
        d
        lb1t
    }
    lb4t
    lb1t
    dpe
    d
    tpe
    d
    tpe
    tape(6) {
        worm
        tpe
        d
        tpe
        lb1t
        d
        lb1t
        lb4t
    }
    tape(3) {
        lb1t
        dpe
        d
        tpe
        d
        dpe
        lb1t
        dpe
    }
    tape(0)
    row6Retract()
    f
    lb1t
    lb1t
    e
    d
    lb1t
    lb1t
    dpe
    tape(0)
    dpe
    tpe
    lb4t
    tape(4) {
        lb1t
        dpe
        d
        tpe
        lb1t
        tpe
    }
    row8retract()
    dpe // floor
}
