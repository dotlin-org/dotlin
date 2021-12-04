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

package compile.builtins

import AssertCompile
import BaseTest
import assertCompile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.*
import java.io.File

// TODO: Update once stdlib can be compiled.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BuiltInsTest(private val className: String) : BaseTest {
    private fun sourceOf(name: String) = File("../libraries/stdlib/builtins/kotlin/$name.kt").readText()

    lateinit var kotlin: String

    abstract val dart: String

    @BeforeAll
    fun beforeTest() {
        kotlin = sourceOf(className)
    }

    @Test
    fun declaration() = assertCompile {
        kotlin(kotlin)
        dart(dart.trimIndent())
    }

    protected fun AssertCompile.kotlinWithDeclaration(@Language("kotlin") kotlin: String) {
        kotlin(
            """
            ${this@BuiltInsTest.kotlin}

            $kotlin
            """
        )
    }

    protected fun AssertCompile.dartWithDeclaration(@Language("dart") dart: String) {
        dart(
            dart.trimIndent() +
                    "\n" + "\n" +
                    this@BuiltInsTest.dart.trimIndent()
        )
    }
}