package org.dotlin.compiler.backend.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.file.Path

suspend fun runCommand(vararg command: String, workingDirectory: Path?): Int {
    return withContext(Dispatchers.IO) {
        val process = ProcessBuilder(*command)
            .directory(workingDirectory?.toFile())
            .start()

        val printInput = launch { process.inputStream.printAll() }
        val printError = launch { process.errorStream.printAll() }

        joinAll(printInput, printError)

        process.waitFor()
    }
}

private fun InputStream.printAll() {
    bufferedReader().useLines { it.forEach(::println) }
}