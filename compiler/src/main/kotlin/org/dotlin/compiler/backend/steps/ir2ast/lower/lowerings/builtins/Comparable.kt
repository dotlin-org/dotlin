/*
 * Copyright 2021 Wilko Manger
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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.builtins

import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.backend.common.ir.addChild
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance

/**
 * We add a `compareTo` extension so that [OperatorsLowering] generates the correct methods, after that we
 * remove the `compareTo` again, since that's alredy defined in Dart's `Comparable`.
 */
object Comparable {
    class PreOperatorsLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
        override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
            if (declaration !is IrClass || !declaration.defaultType.isComparable()) return noChange()

            declaration.file.addChild(
                irFactory.buildFun {
                    name = Name.identifier("compareTo")
                    isOperator = true
                    returnType = irBuiltIns.intType
                    origin = IrDartDeclarationOrigin.COMPARABLE_TEMPORARY_COMPARE_TO
                }.apply {
                    val typeParameter = addTypeParameter {
                        name = Name.identifier("T")
                        superTypes += irBuiltIns.anyType.makeNullable()
                    }

                    extensionReceiverParameter = buildReceiverParameter(
                        parent = this,
                        origin = IrDeclarationOrigin.DEFINED,
                        type = IrSimpleTypeImpl(
                            classifier = declaration.defaultType.classifier,
                            hasQuestionMark = false,
                            arguments = listOf(
                                makeTypeProjection(
                                    typeParameter.defaultType,
                                    variance = Variance.INVARIANT
                                )
                            ),
                            annotations = emptyList()
                        )
                    )

                    addValueParameter {
                        name = Name.identifier("other")
                        type = typeParameter.defaultType
                    }
                }
            )

            return noChange()
        }
    }

    class PostOperatorsLowering(override val context: DartLoweringContext) : IrFileLowering {
        override fun DartLoweringContext.transform(file: IrFile) {
            file.declarations.removeIf {
                it.origin == IrDartDeclarationOrigin.COMPARABLE_TEMPORARY_COMPARE_TO
            }
        }
    }
}

