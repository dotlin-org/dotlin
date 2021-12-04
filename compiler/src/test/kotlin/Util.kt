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
import kotlin.io.path.ExperimentalPathApi
import kotlin.test.assertEquals

abstract class CompilerAssertion {
    protected lateinit var kotlin: String

    fun kotlin(@Language("kotlin") kotlin: String) {
        this.kotlin = kotlin.trimIndent()
    }
}

class AssertCompile : CompilerAssertion() {
    private lateinit var dart: String

    fun dart(@Language("dart") dart: String) {
        this.dart = dart.trimIndent()
    }

    val result
        get() = kotlin to dart
}

class AssertCompileThrows : CompilerAssertion() {
    val result
        get() = kotlin
}

inline fun assertCompile(block: AssertCompile.() -> Unit) {
    val args = AssertCompile()
    block(args)

    val (kotlin, dart) = args.result

    val compiledDart = assertDoesNotThrow {
        KotlinToDartCompiler.compile(
            kotlin,
            // TODO: Use our own compiled stdlib when we can.
            dependencies = setOf(File("kotlin-stdlib-js-1.5.0-M1.klib")),
            format = true
        )
    }

    assertEquals(dart, compiledDart)
}

inline fun <reified E : Throwable> assertCompileThrows(block: AssertCompileThrows.() -> Unit) {
    val args = AssertCompileThrows()
    block(args)

    val kotlin = args.result

    assertThrows<E> {
        KotlinToDartCompiler.compile(kotlin)
    }
}


class AssertCompileLibrary {
    lateinit var path: Path
}

@OptIn(ExperimentalPathApi::class)
inline fun assertCanCompileLib(block: AssertCompileLibrary.() -> Unit) {
    val args = AssertCompileLibrary()
    block(args)

    assertDoesNotThrow {
        KotlinToDartCompiler.compile(
            sourceRoots = setOf(args.path),
            outputFile = File("output.klib"),
            dependencies = emptySet(),
            klib = true
        )
    }
}

/**
 * Used to reference the `_$DefaultValue` Dart type in multiline
 * code strings. Note that you have to add the `_` prefix yourself.
 */
const val DefaultValue = "\$DefaultValue"

