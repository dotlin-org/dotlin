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

import org.dotlin.compiler.backend.attributes.DartImport
import org.dotlin.compiler.backend.steps.ir2ast.lower.DotlinLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrFileLowering
import org.dotlin.compiler.backend.util.importAliasIn
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.*
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.util.TypeRemapper
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.remapTypes
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

/**
 * Dart import directives are added, based on `@DartLibrary` or `@DartHideFromCore` annotations,
 * or based on Kotlin import directives.
 */
class DartImportsLowering(override val context: DotlinLoweringContext) : IrFileLowering {
    override fun DotlinLoweringContext.transform(file: IrFile) {
        val imports = mutableSetOf<DartImport>()

        fun maybeAddComparableOperatorImport(
            declaration: IrDeclarationWithName,
            reference: IrDeclarationReference? = null
        ) {
            // In the stdlib, we need to import the file where the Comparable<T> '>', '<', '>=', '<=' extensions live.
            val isBuiltInsAndComparableOperatorCall =
                isCurrentModuleBuiltIns &&
                        reference is IrCall && reference.origin in listOf(GT, GTEQ, LT, LTEQ) &&
                        declaration.parentClassOrNull?.symbol == irBuiltIns.comparableClass

            if (isBuiltInsAndComparableOperatorCall) {
                imports += DartImport(
                    library = declaration.file.relativeDartPath.toString()
                )
            }
        }

        fun maybeAddDartImports(declaration: IrDeclarationWithName, reference: IrDeclarationReference? = null) {
            val unresolvedImport = declaration.dartUnresolvedImport
            val hiddenNameFromCore = declaration.dartHiddenNameFromCore
            val kotlinImportAlias = declaration.importAliasIn(currentFile)

            maybeAddComparableOperatorImport(declaration, reference)

            // We don't need to import "dart:core" if there's no alias or hidden names.
            if (kotlinImportAlias == null &&
                unresolvedImport?.library == "dart:core" &&
                unresolvedImport.alias == null &&
                !unresolvedImport.hidden
            ) {
                return
            }

            val hiddenName by lazy { declaration.simpleDartNameOrNull?.value }

            unresolvedImport?.let {
                when (kotlinImportAlias) {
                    null -> imports.addAll(
                        listOfNotNull(
                            DartImport(
                                library = it.library,
                                alias = it.alias
                            ),
                            when {
                                it.hidden -> DartImport(
                                    it.library,
                                    hide = hiddenName
                                )
                                else -> null
                            }
                        )
                    )
                    else -> {
                        val originalDartNameValue = declaration.simpleDartNameWithoutKotlinImportAlias.value

                        imports.addAll(
                            listOfNotNull(
                                DartImport(
                                    library = it.library,
                                    alias = kotlinImportAlias,
                                    show = originalDartNameValue
                                ),
                                DartImport(
                                    library = it.library,
                                    hide = originalDartNameValue
                                )
                            )
                        )
                    }
                }
            }

            hiddenNameFromCore?.let {
                imports += DartImport(
                    library = "dart:core",
                    alias = null,
                    hide = hiddenName
                )
            }
        }

        file.acceptChildrenVoid(
            object : IrElementVisitorVoid {
                override fun visitDeclarationReference(expression: IrDeclarationReference) {
                    super.visitDeclarationReference(expression)

                    val referenced = when (val owner = expression.symbol.owner) {
                        is IrConstructor -> owner.parentClassOrNull
                        else -> owner as? IrDeclarationWithName
                    } ?: return

                    maybeAddDartImports(referenced, expression)
                }

                override fun visitMemberAccess(expression: IrMemberAccessExpression<*>) =
                    visitDeclarationReference(expression)

                override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)
            }
        )

        file.remapTypes(
            object : TypeRemapper {
                override fun remapType(type: IrType): IrType {
                    fun maybeAddImport(type: IrType) {
                        type.classOrNull?.let { maybeAddDartImports(it.owner) }
                        if (type is IrSimpleType) {
                            type.arguments.forEach {
                                it.typeOrNull?.let { t -> maybeAddImport(t) }
                            }
                        }
                    }

                    maybeAddImport(type)

                    return type
                }

                override fun enterScope(irTypeParametersContainer: IrTypeParametersContainer) {}
                override fun leaveScope() {}

            }
        )

        file.dartImports.addAll(imports)
    }
}