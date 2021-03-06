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

package org.dotlin.compiler.cli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.path
import org.dotlin.compiler.KotlinToDartCompiler
import java.nio.file.Path
import kotlin.io.path.extension

class Compile : CliktCommand(name = "dotlin") {
    private val sourceRoot: Path? by argument("SOURCE_ROOT")
        .help("Kotlin source root directories")
        .path(
            mustExist = true,
            mustBeReadable = true,
            canBeFile = false,
        )
        .optional()

    private val output: Path? by argument()
        .help(
            """
            Path of the directory to compile to.
            
            If the extension of the file is .dart, Dart source will be generated. If the
            extension of the file is .klib, a Klib will be generated.
            """.trimIndent()
        )
        .path(
            mustExist = false,
            canBeFile = false
        )
        .optional()

    private val format: Boolean by option()
        .help("Whether to format the output Dart code using dart format")
        .flag("--no-format", default = false)

    private val dependencies: Set<Path> by option("-d", "--dependency")
        .help("A .klib dependency necessary for compiling. Can be used multiple times.")
        .path(
            mustExist = true,
            mustBeReadable = true,
            canBeDir = false,
        )
        .multiple()
        .unique()

    override fun run() {
        if (sourceRoot == null && output == null) {
            println(KotlinToDartCompiler.compile(readLine()!!, format = format))
            return
        }

        // TODO: Handle null output (write to stdout)

        KotlinToDartCompiler.compile(
            sourceRoot!!,
            dependencies,
            format,
            isKlib = output!!.extension == "klib",
            output!!,
        )
    }
}