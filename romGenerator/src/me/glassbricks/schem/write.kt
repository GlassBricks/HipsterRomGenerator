package me.glassbricks.schem

import me.glassbricks.infinirom.nbt
import java.io.File
import java.util.zip.GZIPOutputStream


val outDir by lazy {
    var cur = File(".").absoluteFile
    while (true) {
        if (cur.resolve(".git").exists() && cur.resolve("romGenerator").isDirectory) {
            return@lazy cur.resolve("out")
        }
        cur = cur.parentFile ?: break
    }
    error("Could not find out dir")
}

fun File.resolveOut(): File =
    if (isAbsolute) this else outDir.resolve(this).absoluteFile

private val currentClass = Thread.currentThread().stackTrace[1].className

fun getCallingPackage(): String {
    val stack = Thread.currentThread().stackTrace
    var i = 1
    while (stack[i].className == currentClass) i++
    return stack[i].className.substringBeforeLast('.')
}

fun defaultSchemDir(): File {
    return outDir.resolve(getCallingPackage().replace('.', File.separatorChar))
}

fun SchemFile.writeTo(file: String) {
    val fileName = if (!file.endsWith(".schem")) "$file.schem" else file
    writeTo(defaultSchemDir().resolve(fileName))
}

fun SchemFile.writeTo(rfile: File) {
    val file = rfile.resolveOut()
    file.parentFile.mkdirs()
    val stream = file.outputStream().let(::GZIPOutputStream)
    stream.use {
        nbt.encodeToStream(it, SchemFile.serializer(), this)
    }
    println("wrote to ${file.absolutePath}")
}

fun tryTransfer(destDir: String) {
    val transferScript = outDir.parentFile.resolve("transfer.sh").absoluteFile
    if (!transferScript.exists()) {
        return println("transfer.sh not found, skipping transfer")
    }

    fun String.unix() = replace(File.separatorChar, '/')
    val cwd = File(".").absoluteFile
    ProcessBuilder(
        "bash",
        transferScript.relativeTo(cwd).path.unix(),
        defaultSchemDir().relativeTo(cwd).path.unix(),
        destDir
    )
        .inheritIO()
        .start()
        .waitFor()
}
