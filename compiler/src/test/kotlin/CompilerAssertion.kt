/*
 * Copyright 2021-2022 Wilko Manger
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
import org.dotlin.compiler.backend.DotlinCompilerError
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.test.assertContains
import kotlin.test.assertEquals

abstract class CompilerAssertion {
    abstract val sourceRoot: Path
    protected open val dependencies: Set<Path> = setOf(stdlibKlib)
    protected open val format: Boolean = true
    protected open val isKlib: Boolean = false
    protected open val isPublicPackage: Boolean = false

    protected fun compile(): Path {
        return KotlinToDartCompiler.compile(
            sourceRoot,
            dependencies,
            format,
            isKlib,
            isPublicPackage = isPublicPackage
        )
    }

    abstract fun assert()
}

abstract class AssertCompileFiles : CompilerAssertion() {
    private val kotlinSources = mutableListOf<String>()

    fun kotlin(@Language("kotlin") kotlin: String) {
        kotlinSources += kotlin.trimIndent()
    }

    override val sourceRoot: Path
        get() = createTempDirectory().apply {
            kotlinSources.forEachIndexed { index, source ->
                resolve("$index.kt").createFile().writeText(source)
            }
        }
}

class AssertCompilesTo : AssertCompileFiles() {
    private val dartSources = mutableListOf<String>()

    fun dart(@Language("dart") dart: String) {
        dartSources += dart.trimIndent()
    }

    override fun assert() {
        val compiledDartSources =
            Files.walk(assertDoesNotThrow { compile() })
                .filter { it.isRegularFile() }
                .toList()
                .sortedBy { it.nameWithoutExtension }
                .map { it.readText().removeSuffix("\n") }

        compiledDartSources.forEachIndexed { index, source ->
            assertEquals(dartSources[index], source)
        }
    }
}

class AssertCompilesWithError : AssertCompileFiles() {
    lateinit var diagnostics: List<String>

    override fun assert() {
        val error = assertThrows<DotlinCompilerError> { compile() }
        diagnostics.forEach {
            assertContains(error.diagnosticNames, it)
        }
    }
}

class AssertCanCompile : AssertCompileFiles() {
    override fun assert() {
        assertDoesNotThrow { compile() }
    }
}

abstract class AssertCompileLibrary<O> : CompilerAssertion() {
    public override var dependencies = setOf(stdlibKlib)
    override val isKlib = true
    public override var isPublicPackage: Boolean = false
}

class AssertCanCompileLibraryFromPath : AssertCompileLibrary<Unit>() {
    lateinit var path: Path

    override val sourceRoot: Path
        get() = path

    override fun assert() {
        assertDoesNotThrow { compile() }
    }
}

inline fun assertCompile(block: AssertCompilesTo.() -> Unit) = AssertCompilesTo().let {
    block(it)
    it.assert()
}

inline fun assertCanCompile(block: AssertCanCompile.() -> Unit) = AssertCanCompile().let {
    block(it)
    it.assert()
}

inline fun assertCompilesWithError(name: String, block: AssertCompilesWithError.() -> Unit) =
    assertCompilesWithErrors(name, block = block)

inline fun assertCompilesWithErrors(vararg names: String, block: AssertCompilesWithError.() -> Unit) =
    AssertCompilesWithError().let {
        require(names.isNotEmpty())
        it.diagnostics = names.toList()
        block(it)
    }

inline fun assertCanCompileLib(block: AssertCanCompileLibraryFromPath.() -> Unit) =
    AssertCanCompileLibraryFromPath().let {
        block(it)
        it.assert()
    }
