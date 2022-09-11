/*
 * Copyright 2022 Wilko Manger
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

package org.dotlin.compiler

import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Severity

val Iterable<Diagnostic>.warnings
    get() = filter { it.severity == Severity.WARNING }

val Iterable<Diagnostic>.errors
    get() = filter { it.severity == Severity.ERROR }

val Iterable<Diagnostic>.hasWarnings
    get() = warnings.isNotEmpty()

val Iterable<Diagnostic>.hasErrors
    get() = errors.isNotEmpty()

val Iterable<Diagnostic>.factories
    get() = map { it.factory }