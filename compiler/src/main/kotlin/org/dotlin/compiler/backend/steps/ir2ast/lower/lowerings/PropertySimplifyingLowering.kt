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

import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.util.isSimple
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.name.Name

/**
 * Properties get simplified to either a field or their getter and setter.
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE", "UnnecessaryVariable")
class PropertySimplifyingLowering(override val context: DotlinLoweringContext) : IrDeclarationLowering {
    override fun DotlinLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrProperty) return noChange()

        val irProperty = declaration

        // If we have an implicit getter, and in the case of vars, also an implicit setter, we replace
        // the property with a field.
        if (irProperty.isSimple) {
            val irField = irFactory.buildField {
                name = irProperty.name
                type = irProperty.type
                isFinal = !irProperty.isVar
                isStatic = irProperty.backingField?.isStatic == true ||
                        (irProperty.getter?.dispatchReceiverParameter == null &&
                                irProperty.setter?.dispatchReceiverParameter == null)
                visibility = irProperty.visibility
                isExternal = irProperty.isExternal
                metadata = irProperty.metadata
            }.apply {
                parent = irProperty.parent
                correspondingPropertySymbol = irProperty.symbol
                initializer = irProperty.backingField?.initializer
                annotations = buildList {
                    addAll(irProperty.annotations)
                    irProperty.getter?.annotations?.let { addAll(it) }
                    irProperty.setter?.annotations?.let { addAll(it) }
                }
            }

            return add(irField) and remove(irProperty)
        }

        // Otherwise, we replace the property with the relevant getter and setter.
        return irProperty.run {
            val addBackingField = when {
                // If a property has an explicit backing field, add it.
                hasExplicitBackingField -> add(backingField!!)
                else -> null
            }

            val addGetter = when {
                !hasImplicitGetter || hasExplicitBackingField -> getter?.let {
                    val newGetter = it.deepCopyWith {
                        name = irProperty.name
                    }.apply {
                        correspondingPropertySymbol = irProperty.symbol
                    }

                    irProperty.getter = newGetter

                    add(newGetter)
                }
                else -> null
            }

            val addSetter = when {
                !hasImplicitSetter || hasExplicitBackingField -> setter?.let {
                    val newSetter = it.deepCopyWith {
                        name = irProperty.name
                    }.apply {
                        irProperty.setter = this

                        correspondingPropertySymbol = irProperty.symbol

                        valueParameters.single().let { param ->
                            if (param.name.asString() == "<set-?>") {
                                param.name = Name.identifier("\$value")
                            }
                        }
                    }

                    add(newSetter)
                }
                else -> null
            }

            sequenceOf(addBackingField, addGetter, addSetter)
                .filterNotNull() and remove(irProperty)
        }
    }
}