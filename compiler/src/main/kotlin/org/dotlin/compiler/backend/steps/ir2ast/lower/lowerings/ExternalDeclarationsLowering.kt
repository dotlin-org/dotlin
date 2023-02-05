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
import org.dotlin.compiler.backend.steps.ir2ast.lower.DotlinLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrFileLowering
import org.dotlin.compiler.backend.util.isExplicitlyExternal
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrPossiblyExternalDeclaration
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.isEffectivelyExternal

class ExternalDeclarationsLowering(override val context: DotlinLoweringContext) : IrFileLowering {
    override fun DotlinLoweringContext.transform(file: IrFile) {
        file.declarations.apply {
            toList().forEach remove@{
                if (!it.isEffectivelyExternal()) return@remove

                // Companion objects are considered non-external by default in Dotlin, even if they're inside an
                // external class. Only if they are explicitly marked external are they consider external. The same
                // goes for its members, if the companion object itself is not external.
                if (it is IrClass) {
                    it.companionObject()?.let { obj ->
                        if (!obj.isExplicitlyExternal) {
                            obj.declarations.apply {
                                removeIf { child -> child.isExplicitlyExternal }

                                replaceAll { child ->
                                    (child as IrPossiblyExternalDeclaration).deepCopyWith(isExternal = false)
                                }
                            }

                            // We don't use addChild on purpose, we want to keep the parent information.
                            add(obj.deepCopyWith { isExternal = false })
                            it.declarations.remove(obj)
                        }
                    }
                }

                remove(it)
            }
        }
    }
}