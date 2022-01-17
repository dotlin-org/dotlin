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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings

import org.dotlin.compiler.backend.steps.ir2ast.ir.deepCopyWith
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.isEffectivelyExternal

class ExternalDeclarationsLowering(override val context: DartLoweringContext) : IrFileLowering {
    override fun DartLoweringContext.transform(file: IrFile) {
        file.declarations.apply {
            toList().forEach remove@{
                if (!it.isEffectivelyExternal()) return@remove

                // Don't remove companion objects, they are not considered external in Dotlin.
                if (it is IrClass) {
                    it.companionObject()?.let { obj ->
                        // We don't use addChild on purpose, we want to keep the parent information.
                        add(obj.deepCopyWith { isExternal = false })
                    }
                }

                remove(it)
            }
        }
    }
}