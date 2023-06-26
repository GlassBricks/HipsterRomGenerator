package notahipster

private typealias B = SeqGen

fun B.row1234() {
    // row 1
    tpe
    // row 2
    d
    dpe
    lb4t
    lb1t
    dpe
    d
    tpe
    // row 3
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
    // row 4
    e
    dpe
    lb1t
    tpe
    row4Retract()
}

fun B.row4Retract(andTape5: Boolean = false) {
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
    tape(4) {
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
    }
    row7retract()
}

fun B.row7retract() {
    tape(1)
    tape(2) {
        tpe
        d
        dpe
    }
    tape(3) {
        lb4t
        lb1t
        dpe
        d
        tpe
        lb1t
        dpe
    }
    tape(0) {
        e
        d
        f
        e
        g
        f * 3
    }
    row6Retract()
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
    tape(4) {
        dpe
        tpe
        lb4t
        lb1t
        dpe
        d
        tpe
        lb4t
        tpe
    }
    row7retract()
}

fun B.row9() {
    g
    dpe
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
    tape(6) {
        lb4t
        lb1t
        dpe
        d
        tpe
        d
        tpe
        worm
    }
    tape(3) {
        lb1t
        dpe
    }
    tape(0)
    row6Retract()
    f
    dpe
    lb1t
    e
    d
    dpe
    lb1t
    dpe
    tape(0)
    tape(4) {
        dpe
        tpe
        lb4t
        lb1t
        dpe
        d
        tpe
        lb1t
        tpe
    }
    row8retract()
}
