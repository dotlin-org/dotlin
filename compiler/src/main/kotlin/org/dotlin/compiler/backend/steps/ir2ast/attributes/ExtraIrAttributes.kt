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

package org.dotlin.compiler.backend.steps.ir2ast.attributes

import org.dotlin.compiler.backend.DotlinAnnotations
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrCustomElementVisitor
import org.dotlin.compiler.backend.steps.ir2ast.ir.element.IrIfNullExpression
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.isDartConst
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.util.hasEqualFqName
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.name.FqName

/**
 * All [IrElement]s referenced must be the `attributeOwnerId`s.
 */
interface ExtraIrAttributes {
    companion object {
        fun default() = object : ExtraIrAttributes {
            override val propertiesInitializedInConstructorBody = mutableSetOf<IrProperty>()
            override val propertiesInitializedInFieldInitializerList = mutableSetOf<IrProperty>()
            override val parameterPropertyReferencesInParameterDefaultValue = mutableSetOf<IrGetValue>()
            private val annotatedExpressions = mutableMapOf<IrExpression, MutableList<IrConstructorCall>>()
            private val dartImportsPerFile = mutableMapOf<IrFile, MutableSet<DartImport>>()

            override fun IrExpression.annotate(annotations: Iterable<IrConstructorCall>) {
                annotatedExpressions.compute(this) { _, currentAnnotations ->
                    (currentAnnotations ?: mutableListOf()).apply {
                        addAll(annotations)
                    }
                }
            }

            override val IrExpression.annotations: List<IrConstructorCall>
                get() = annotatedExpressions[attributeOwner()] ?: emptyList()


            override fun IrExpression.hasAnnotation(name: String) = annotations.any {
                it.symbol.owner.parentAsClass.hasEqualFqName(FqName(name))
            }

            override val IrFile.dartImports: Set<DartImport>
                get() = dartImportsPerFile[this] ?: emptySet()

            override fun IrFile.addDartImports(imports: Iterable<DartImport>) {
                dartImportsPerFile.compute(this) { _, currentDartImports ->
                    (currentDartImports ?: mutableSetOf()).apply {
                        addAll(imports)
                    }
                }
            }
        }
    }

    /**
     * A field initializer cannot reference `this` in Dart, and thus must be initialized in the
     * primary constructor body and be marked `late`.
     */
    val propertiesInitializedInConstructorBody: MutableSet<IrProperty>

    val IrProperty.isInitializedInConstructorBody: Boolean
        get() = attributeOwner() in propertiesInitializedInConstructorBody

    val propertiesInitializedInFieldInitializerList: MutableSet<IrProperty>

    val IrProperty.isInitializedInFieldInitializerList: Boolean
        get() = attributeOwner() in propertiesInitializedInFieldInitializerList

    /**
     * Whether the property is initialized somewhere else, e.g. in the constructor body or field initializer list.
     */
    val IrProperty.isInitializedSomewhereElse: Boolean
        get() = isInitializedInConstructorBody || isInitializedInFieldInitializerList

    /**
     * In complex parameters' default values, all `IrGetValue` are remapped to `IrGetField`s, because in the Dart
     * constructor we want to refer to the relevant properties of those references parameters (if there is one), not the
     * parameter itself (since the value of the parameter might be outdated, because of how Dart syntax works).
     */
    val parameterPropertyReferencesInParameterDefaultValue: MutableSet<IrGetValue>

    fun IrExpression.annotate(buildAnnotation: () -> IrConstructorCall) = annotate(buildAnnotation())
    fun IrExpression.annotate(annotation: IrConstructorCall) = annotate(listOf(annotation))
    fun IrExpression.annotate(annotations: Iterable<IrConstructorCall>)

    val IrExpression.annotations: List<IrConstructorCall>

    fun IrExpression.isDartConst(): Boolean = when (this) {
        // Enums are always constructed as const.
        is IrEnumConstructorCall -> true
        is IrConst<*> -> true
        is IrWhen, is IrIfNullExpression -> {
            var isConst = true

            acceptChildren(
                object : IrCustomElementVisitor<Unit, Nothing?> {
                    override fun visitElement(element: IrElement, data: Nothing?) {
                        if (element is IrExpression) {
                            isConst = isConst && element.isDartConst()
                        }

                        element.acceptChildren(this, null)
                    }

                },
                data = null
            )

            isConst
        }
        else -> when {
            this is IrConstructorCall && symbol.owner.parentAsClass.isDartConst() -> true
            else -> hasAnnotation(DotlinAnnotations.dartConst)
        }
    }

    fun IrExpression.hasAnnotation(name: String): Boolean

    val IrFile.dartImports: Set<DartImport>

    fun IrFile.addDartImport(import: DartImport) = addDartImports(listOf(import))
    fun IrFile.addDartImports(imports: Iterable<DartImport>)
}

data class DartImport(
    val library: String,
    val alias: String? = null,
    val hide: String? = null,
    val show: String? = null
)

/**
 * Assumes that [attributeOwnerId] is the same type as `this`.
 */
inline fun <reified T : IrAttributeContainer> T.attributeOwner(): T = attributeOwnerId as T