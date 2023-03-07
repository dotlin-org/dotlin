package org.dotlin.compiler.backend.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.nio.file.Path

suspend fun runCommand(vararg command: String, workingDirectory: Path? = null): Int {
    return withContext(Dispatchers.IO) {
        val resolvedCommand = buildList {
            add(findBinary(command[0]).toString())
            addAll(command.drop(1))
        }.toTypedArray()

        val process = ProcessBuilder(*resolvedCommand)
            .directory(workingDirectory?.toFile())
            .start()

        val printInput = launch { process.inputStream.printAll() }
        val printError = launch { process.errorStream.printAll() }

        joinAll(printInput, printError)

        process.waitFor()
    }
}

private fun findBinary(name: String): Path {
    val home = homePathString?.let {
        when {
            !it.endsWith(File.separator) -> it + File.separator
            else -> it
        }
    }
    val paths = System.getenv("PATH")
        .split(File.pathSeparator)
        .let {
            when {
                home != null -> it.map { p -> p.replaceFirst(Regex("^~" + File.separator), home) }
                else -> it
            }
        }

    return paths
        .let {
            when {
                onWindows() -> {
                    val extensions by lazy { listOf("bat", "exe") }
                    it.flatMap { p -> extensions.map { ext -> File(p, "$name.$ext") } }
                }
                else -> it.map { p -> File(p, name) }
            }
        }
        .firstOrNull { it.exists() && it.isFile && it.canExecute() }
        ?.toPath()
        ?: throw IllegalStateException("binary for $name could not be found or cannot be executed")
}

private fun InputStream.printAll() {
    bufferedReader().useLines { it.forEach(::println) }
}