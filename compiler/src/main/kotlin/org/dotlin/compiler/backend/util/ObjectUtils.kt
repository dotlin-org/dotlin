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

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrDartDeclarationOrigin
import org.dotlin.compiler.backend.steps.ir2ast.ir.isStatic
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.ir.util.isObject
import org.jetbrains.kotlin.ir.util.parentClassOrNull

val IrDeclaration.isFromObjectAndStaticallyAvailable
    get() = (this is IrProperty || (this is IrField && !isStatic) || (this is IrSimpleFunction && !isStatic)) &&
            this !is IrConstructor &&
            !isFakeOverride &&
            origin != IrDartDeclarationOrigin.OBJECT_INSTANCE_FIELD &&
            (parentClassOrNull?.origin == IrDartDeclarationOrigin.OBJECT || parentClassOrNull?.isObject == true)