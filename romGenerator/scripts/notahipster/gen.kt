package notahipster

import io.kotest.core.spec.style.StringSpec
import me.glassbricks.schem.waitOptimizedRecordRomSchem
import me.glassbricks.schem.writeSchematic
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


private val versionFile = File("9x9fs moves/cur_version")

private fun getVersion(): Int {
    if (!versionFile.exists()) {
        versionFile.writeText("0")
    }
    return versionFile.readText().toInt()
}

private fun incVersion() {
    val version = getVersion()
    versionFile.writeText((version + 1).toString())
}

val fileNames = listOf(
    "row1-3.txt",
    "row4.txt",
    "row5.txt",
    "row6.txt",
    "row7.txt",
    "row8.txt",
    "row9.txt",
)
val moves = fileNames.map {
    lazy {
        File("9x9fs moves/$it")
            .takeIf { it.exists() }
            ?.let(::parseFile)
    }
}

class GenSchem : StringSpec({
    beforeSpec {
        File("9x9fs out").mkdirs()
        // clear out dir
        File("9x9fs out").listFiles()?.forEach { it.delete() }
    }
    afterSpec {
        val version = getVersion()
        incVersion()
        // zip all files in out dir
        val files = File("9x9fs out/").listFiles() ?: error("no files")
        println(files.map { it.name })
        val zipFile = File("9x9fs out/9x9fs-all-v$version.zip")
        zipFile.delete()
        zipFile.createNewFile()
        ZipOutputStream(FileOutputStream(zipFile)).use {
            for (file in files) {
                if (!file.name.endsWith(".schem")) continue
                println("writing ${file.name}... ")
                it.putNextEntry(ZipEntry(file.name))
                it.write(file.readBytes())
                it.closeEntry()
            }
        }
        println("wrote to ${zipFile.absolutePath}")

    }
    "generate cumulative" {
        val movesCumulative = mutableListOf<Move>()
        for ((i, fileName) in fileNames.withIndex()) {
            val elements = moves[i].value ?: break
            movesCumulative.addAll(elements)

            if (i == 0) continue

            val newFileName =
                "upTo-${fileName.substringBefore('.')}.schem"


            val schemFile = waitOptimizedRecordRomSchem(movesCumulative.let(::dispersePinks), encoding, Move.wait)


            writeSchematic(schemFile, "9x9fs out/$newFileName")
        }
    }
    "generate each" {
        for ((i, fileName) in fileNames.withIndex()) {
            val elements = moves[i].value ?: continue
            val newFileName = fileName.substringBefore('.') + ".schem"

            val schemFile = waitOptimizedRecordRomSchem(elements.let(::dispersePinks), encoding, Move.wait)
            writeSchematic(schemFile, "9x9fs out/$newFileName")
        }
    }
})
