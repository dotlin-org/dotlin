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

package org.dotlin.compiler.backend.util

import org.dotlin.compiler.backend.hasDartGetterAnnotation
import org.dotlin.compiler.backend.steps.ir2ast.ir.hasImplicitGetter
import org.dotlin.compiler.backend.steps.ir2ast.ir.hasImplicitSetter
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.util.isGetter
import org.jetbrains.kotlin.ir.util.isSetter

val IrProperty.isSimple: Boolean
    get() = hasImplicitGetter && (!isVar || hasImplicitSetter)

fun IrFunction.isDartGetter() = isGetter || hasDartGetterAnnotation()
fun IrFunction.isDartSetter() = isSetter