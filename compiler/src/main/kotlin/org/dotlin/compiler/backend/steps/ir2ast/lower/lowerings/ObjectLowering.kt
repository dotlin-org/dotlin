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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings

import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.backend.common.ir.addChild
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name

class ObjectLowering(private val context: DartLoweringContext) : IrDeclarationTransformer {
    override fun transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass || !declaration.isObject) return noChange()

        val obj = declaration.deepCopyWith {
            name = when {
                declaration.isCompanion -> Name.identifier("$" + declaration.parentAsClass.name.identifier + "Companion")
                else -> declaration.name
            }

            origin = IrDartDeclarationOrigin.OBJECT
        }.apply {
            val newObj = this

            primaryConstructor!!.visibility = DescriptorVisibilities.PRIVATE

            addChild(
                context.irFactory.buildField {
                    isStatic = true
                    type = defaultType
                    name = Name.identifier("\$instance")
                }.apply {
                    parent = newObj

                    initializer = IrExpressionBodyImpl(
                        context.buildStatement(symbol) {
                            IrConstructorCallImpl(
                                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                                type = defaultType,
                                symbol = primaryConstructor!!.symbol,
                                typeArgumentsCount = 0,
                                constructorTypeArgumentsCount = 0,
                                valueArgumentsCount = 0,
                                origin = IrDartStatementOrigin.OBJECT_CONSTRUCTOR
                            )
                        }
                    )
                }
            )
        }

        if (obj.isCompanion) {
            declaration.file.addChild(obj)

            return remove() and add(
                context.irFactory.buildField {
                    isStatic = true
                    type = obj.defaultType
                    name = Name.identifier("\$companion")
                }.apply {
                    parent = obj

                    initializer = IrExpressionBodyImpl(
                        context.createIrBuilder(symbol).buildStatement {
                            irGetField(
                                receiver = irGetObject(obj.symbol),
                                field = obj.fieldWithName("\$instance"),
                            )
                        }
                    )
                }
            )
        }

        return just { replaceWith(obj) }
    }
}