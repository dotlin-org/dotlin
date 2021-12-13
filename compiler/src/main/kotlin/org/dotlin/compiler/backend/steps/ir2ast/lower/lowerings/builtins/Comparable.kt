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
import org.jetbrains.kotlin.backend.common.ir.createDispatchReceiverParameter
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrSymbol
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
    class PreOperatorsLowering(private val context: DartLoweringContext) : IrDeclarationTransformer {
        override fun transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
            if (declaration !is IrClass || !declaration.defaultType.isComparable()) return noChange()

            declaration.file.addChild(
                context.irFactory.buildFun {
                    name = Name.identifier("compareTo")
                    isOperator = true
                    returnType = context.irBuiltIns.intType
                    origin = IrDartDeclarationOrigin.COMPARABLE_TEMPORARY_COMPARE_TO
                }.apply {
                    val typeParameter = addTypeParameter {
                        name = Name.identifier("T")
                        superTypes += context.irBuiltIns.anyType.makeNullable()
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

    class PostOperatorsLowering(private val context: DartLoweringContext) : IrFileTransformer {
        override fun transform(file: IrFile) {
            file.declarations.removeIf {
                it.origin == IrDartDeclarationOrigin.COMPARABLE_TEMPORARY_COMPARE_TO
            }
        }
    }
}

