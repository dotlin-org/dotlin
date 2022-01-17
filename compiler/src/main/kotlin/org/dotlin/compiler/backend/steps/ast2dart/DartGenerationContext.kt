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

package org.dotlin.compiler.backend.steps.ast2dart

class DartGenerationContext {
    private val flags = mutableSetOf<Flag>()

    fun <T> withFlag(flag: Flag, block: () -> T): T {
        flags.add(flag)
        val result = block()
        flags.remove(flag)
        return result
    }

    /**
     * Returns true if the flag was present. Consumes the flag, meaning that a next
     * call of [consume] (without an additional [withFlag] in between) would return
     * false for the same flag.
     */
    fun consume(flag: Flag): Boolean = flags.remove(flag)

    enum class Flag {
        GETTER
    }
}