/*
 * Copyright 2021 Wilko Manger
 *
 * This file is part of Dotlin.
 *
 * Dotlin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dotlin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Dotlin.  If not, see <https://www.gnu.org/licenses/>.
 */

import org.dotlin.compiler.KotlinToDartCompiler
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.test.assertEquals

abstract class CompilerAssertion<I, O> {
    protected open lateinit var kotlin: String

    open fun kotlin(@Language("kotlin") kotlin: String) {
        this.kotlin = kotlin.trimIndent()
    }

    abstract val input: I
    abstract val output: O
}

abstract class AssertCompile<I, O> : CompilerAssertion<I, O>() {
    protected lateinit var dart: String

    fun dart(@Language("dart") dart: String) {
        this.dart = dart.trimIndent()
    }


    class Simple : AssertCompile<String, String>() {
        override val input: String
            get() = kotlin

        override val output: String
            get() = dart
    }
}

class AssertCompileFiles : AssertCompile<List<String>, String>() {
    private val kotlinSources = mutableListOf<String>()

    override fun kotlin(@Language("kotlin") kotlin: String) {
        kotlinSources += kotlin
    }

    override val input = kotlinSources

    override val output: String
        get() = dart
}

class AssertCompileThrows : CompilerAssertion<String, Nothing?>() {
    override val input
        get() = kotlin

    override val output: Nothing? = null
}

inline fun assertCompile(block: AssertCompile.Simple.() -> Unit) {
    val args = AssertCompile.Simple()
    block(args)

    val compiledDart = assertDoesNotThrow {
        KotlinToDartCompiler.compile(
            args.input,
            dependencies = setOf(stdlibKlib),
            format = true
        )
    }

    assertEquals(args.output, compiledDart)
}

inline fun assertCompileFiles(block: AssertCompileFiles.() -> Unit) {
    val args = AssertCompileFiles()
    block(args)

    val tempDir = createTempDirectory()

    args.input.forEachIndexed { index, source ->
        tempDir.resolve("$index.kt").also { it.writeText(source) }
    }

    val compiledDart = assertDoesNotThrow {
        KotlinToDartCompiler.compile(
            setOf(tempDir),
            dependencies = setOf(stdlibKlib),
            format = true
        )
    }

    assertEquals(args.output, compiledDart)
}

inline fun <reified E : Throwable> assertCompileThrows(block: AssertCompileThrows.() -> Unit) {
    val args = AssertCompileThrows()
    block(args)

    val kotlin = args.input

    assertThrows<E> {
        KotlinToDartCompiler.compile(kotlin)
    }
}


abstract class AssertCompileLibrary<I, O> : CompilerAssertion<I, O>() {
    var dependencies = setOf(stdlibKlib)
}

class AssertCompileLibraryFromPath : AssertCompileLibrary<Path, Nothing?>() {
    lateinit var path: Path

    override val input: Path
        get() = path

    override val output = null
}

@OptIn(ExperimentalPathApi::class)
inline fun assertCanCompileLib(block: AssertCompileLibraryFromPath.() -> Unit) {
    val args = AssertCompileLibraryFromPath()
    block(args)

    val output = File("build/output.klib")

    assertDoesNotThrow {
        KotlinToDartCompiler.compile(
            sourceRoots = setOf(args.input),
            outputFile = output,
            dependencies = args.dependencies,
            klib = true
        )
    }
}

@OptIn(ExperimentalPathApi::class)
val stdlibSrc = Path("../libraries/stdlib/src")
val stdlibKlib = File("build/stdlib.klib")

/**
 * Used to reference the `_$DefaultValue` Dart type in multiline
 * code strings. Note that you have to add the `_` prefix yourself.
 */
const val DefaultValue = "\$DefaultValue"

