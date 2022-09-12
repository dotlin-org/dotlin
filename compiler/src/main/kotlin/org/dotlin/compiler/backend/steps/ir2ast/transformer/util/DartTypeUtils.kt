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

package org.dotlin.compiler.backend.steps.ir2ast.transformer.util

import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.jetbrains.kotlin.ir.types.*

fun IrType.isDartPrimitive(orNullable: Boolean = false) =
    isDartBool(orNullable) || isDartNumberPrimitive(orNullable) || isDartString(orNullable)

fun IrType.isDartBool(orNullable: Boolean = false) = isBoolean() || (orNullable && isNullableBoolean())

fun IrType.isDartString(orNullable: Boolean = false) = isString() || isChar() ||
        (orNullable && (isNullableString() || isNullableChar()))

fun IrType.isDartInt(orNullable: Boolean = false) = isInt() || isLong() || isByte() || isShort() ||
        (orNullable && (isNullableInt() || isNullableLong() || isNullableByte() || isNullableShort()))

fun IrType.isDartDouble(orNullable: Boolean) = isDouble() || isFloat() ||
        (orNullable && (isNullableDouble() || isNullableFloat()))

fun IrType.isDartNumberPrimitive(orNullable: Boolean = false) = isDartInt(orNullable) || isDartDouble(orNullable)