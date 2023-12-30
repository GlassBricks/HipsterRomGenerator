package mini5x5

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.infinirom.*
import me.glassbricks.schem.writeSchematic
import mini5x5.Move.*
import java.io.File

enum class Move {
    empty,
    t1,
    obs,
    wait,
    bot,
    dpe,
    store;

}

val seq = listOf(
    wait,
    dpe,
    obs, obs,

    bot, t1, obs, obs, t1, bot, dpe, dpe,
    obs, obs,

    bot, t1, obs, t1, t1, obs, dpe,
    t1, obs, obs, bot, t1, dpe, dpe,
    obs, obs,

    bot, t1, obs, dpe, dpe, obs,
    bot, t1, obs, obs, t1, bot, dpe, t1,
    obs, t1, t1, obs, dpe,
    t1, obs, obs, t1, bot, dpe, dpe,
    obs, obs,

    bot, t1, store, wait, obs, dpe, dpe, obs,
    bot, t1, obs, obs, t1, bot, dpe, dpe,
    obs, obs,
    bot, t1, obs, t1, t1, obs, dpe,
    t1, obs, obs, t1, bot, dpe, t1,
    obs, dpe, dpe, obs, t1,
    bot, t1, obs, obs, t1, bot, dpe, t1,
    obs, t1, t1, obs, dpe,
    t1, obs, obs, t1, bot, dpe, dpe,
    t1,
    empty,
)

val encoding = ordinalEncoding<Move>()


class Gen : StringSpec({
    "gen" {
        val ssSeq = encoding.encode(seq)
        val schem = ssSeq.toInifinirom1()

        val file = File("mini5x5.schem")
        file.writeSchematic(schem)
    }
})
