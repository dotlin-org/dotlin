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
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.path
import org.dotlin.compiler.KotlinToDartCompiler
import org.dotlin.compiler.backend.DartProject
import java.nio.file.Path
import kotlin.io.path.Path

class Compile : CliktCommand(name = "dotlin") {
    private val format: Boolean by option()
        .help("Whether to format the output Dart code using dart format")
        .flag("--no-format", default = false)

    private val dependencies: Set<Path> by option("-d", "--dependency")
        .help("A path to a dependency Dart project")
        .path(
            mustExist = true,
            mustBeReadable = true,
            canBeDir = false,
        )
        .multiple()
        .unique()

    override fun run() {
        // TODO: Support compiling non-project Dart files and STDIN

        KotlinToDartCompiler.compile(
            DartProject(
                name = "TODO", // TODO
                path = Path(""), // TODO: Find nearest pubspec,
                isLibrary = false, // TODO
                dependencies = emptySet() // TODO
            ),
            format,
        )
    }
}