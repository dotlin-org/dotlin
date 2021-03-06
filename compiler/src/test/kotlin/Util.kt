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

val stdlibSrc = Path("../libraries/stdlib/src")
val stdlibKlib = Path("build/stdlib.klib")

/**
 * Used to reference the `_$DefaultValue` Dart type in multiline
 * code strings. Note that you have to add the `_` prefix yourself.
 */
const val DefaultValue = "\$DefaultValue"

fun Path.singleChildFile(): Path = Files.newDirectoryStream(this).single()
