/*
 * Copyright 2023 Wilko Manger
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

@file:Suppress("ClassName")

package org.dotlin.compiler.backend.bin

import org.dotlin.compiler.backend.util.runCommand
import java.nio.file.Path

object dart {
    object pub {
        suspend fun get(workingDirectory: Path? = null) =
            runCommand("dart", "pub", "get", workingDirectory = workingDirectory)
    }

    suspend fun run(executable: String, vararg args: String, workingDirectory: Path? = null) = runCommand(
        "dart", "run", executable, *args,
        workingDirectory = workingDirectory
    )
}