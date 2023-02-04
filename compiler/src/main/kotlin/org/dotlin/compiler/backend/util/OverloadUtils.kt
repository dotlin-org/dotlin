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

import org.dotlin.compiler.backend.annotatedDartName
import org.jetbrains.kotlin.backend.common.lower.parents
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.isGetter
import org.jetbrains.kotlin.ir.util.isSetter
import org.jetbrains.kotlin.util.collectionUtils.filterIsInstanceAnd
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance

val IrSimpleFunction.isOverloaded: Boolean
    get() = overloads.isNotEmpty()

/**
 * Whether this function is an overload. False if this is the first occurrence (source-wise) of overloads, the
 * "original" of the overloads. For example:
 * ```kotlin
 * // isOverload = false, isOverloaded = true
 * fun execute(): Int = ..
 *
 * // isOverload = true, isOverloaded = true
 * fun execute(arg: Int): Int = ..
 * ```
 */
val IrSimpleFunction.isOverload: Boolean
    get() = overloadsWithSelf.let { it.isNotEmpty() && it.first().attributeOwnerId != attributeOwnerId }

// TODO?: Handle local functions
// TODO?: Handle extensions
val IrSimpleFunction.overloadsWithSelf: Collection<IrSimpleFunction>
    get() {
        // We only care about names the user has given us. This is why we don't use dartNameOrNull.
        fun IrSimpleFunction.relevantName() = annotatedDartName ?: name.identifierOrNullIfSpecial

        return parents.firstIsInstance<IrDeclarationContainer>()
            .declarations
            .filterIsInstanceAnd {
                val ourName = this.relevantName()
                val theirName = it.relevantName()
                !it.isGetter && !it.isSetter &&
                        ourName != null && theirName != null && ourName == theirName
            }
    }


val IrSimpleFunction.overloads: Collection<IrSimpleFunction>
    get() = overloadsWithSelf - (this.attributeOwnerId as? IrSimpleFunction ?: this)