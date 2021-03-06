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

package org.dotlin.compiler.backend.steps.ir2ast.transformer

import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.*
import org.dotlin.compiler.dart.ast.compilationunit.DartCompilationUnitMember
import org.dotlin.compiler.dart.ast.declaration.classormixin.DartClassDeclaration
import org.dotlin.compiler.dart.ast.declaration.classormixin.DartExtendsClause
import org.dotlin.compiler.dart.ast.declaration.classormixin.DartImplementsClause
import org.dotlin.compiler.dart.ast.declaration.extension.DartExtensionDeclaration
import org.dotlin.compiler.dart.ast.declaration.function.DartTopLevelFunctionDeclaration
import org.dotlin.compiler.dart.ast.declaration.variable.DartTopLevelVariableDeclaration
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclaration
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclarationList
import org.dotlin.compiler.dart.ast.expression.DartFunctionExpression
import org.dotlin.compiler.dart.ast.type.DartNamedType
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.parentAsClass

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object IrToDartDeclarationTransformer : IrDartAstTransformer<DartCompilationUnitMember>() {
    override fun DartTransformContext.visitFunction(irFunction: IrFunction, context: DartTransformContext) =
        irFunction.transformBy(context) {
            DartTopLevelFunctionDeclaration(
                name = name!!,
                returnType = returnType,
                function = DartFunctionExpression(
                    typeParameters = typeParameters,
                    parameters = parameters,
                    body = irFunction.body.accept(context)
                ),
                annotations = annotations,
                documentationComment = documentationComment,
            )
        }

    override fun DartTransformContext.visitClass(
        irClass: IrClass,
        context: DartTransformContext
    ): DartCompilationUnitMember {
        // Extensions are handled differently.
        if (irClass.isDartExtensionContainer) {
            return visitExtension(irClass, context)
        }

        return when (irClass.kind) {
            ClassKind.CLASS, ClassKind.INTERFACE, ClassKind.OBJECT, ClassKind.ANNOTATION_CLASS, ClassKind.ENUM_CLASS -> {
                val name = irClass.simpleDartName
                val isDefaultValueClass = irClass.origin == IrDartDeclarationOrigin.COMPLEX_PARAM_DEFAULT_VALUE

                DartClassDeclaration(
                    name = name,
                    isAbstract = irClass.modality == Modality.ABSTRACT || irClass.isInterface,
                    typeParameters = irClass.typeParameters.accept(context),
                    // If the class is a default value for a complex parameter, we want to implement
                    // it and not extend it, so we don't extend anything.
                    extendsClause = when {
                        !isDefaultValueClass -> irClass.superTypes
                            .firstOrNull { it.getClass()?.isClass ?: false && !it.isAny() }
                            ?.accept(context)
                            ?.let { DartExtendsClause(it as DartNamedType) }
                        else -> null
                    },
                    implementsClause = irClass.superTypes
                        .let {
                            // If our class is a default value for a complex parameter, we want it to implement
                            // the type is a default value for, even if that type is a class and not an interface.
                            when {
                                !isDefaultValueClass -> it.filter { type -> type.getClass()?.isInterface ?: false }
                                else -> it
                            }
                        }
                        // TODO: This should never be a DartFunctionType
                        .mapNotNull { it.accept(context) as? DartNamedType }
                        .let {
                            when {
                                it.isNotEmpty() -> DartImplementsClause(it)
                                else -> null
                            }
                        },
                    members = irClass.declarations
                        .asSequence()
                        // We handle fake overrides only from interfaces and if this itself is not an interface.
                        .filter {
                            if (!it.isFakeOverride()) {
                                return@filter true
                            }

                            // We don't want any fake overrides if we are ourselves an interface.
                            if (irClass.isInterface) {
                                return@filter false
                            }

                            fun isFakeOverrideOfInterface(overridable: IrOverridableDeclaration<*>): Boolean {
                                if (overridable.overriddenSymbols.isEmpty()) return false

                                // If somewhere in a super type it is not fake overridden, there's
                                // already an implementation there and we don't have to include it.
                                if (!overridable.isFakeOverride()) return false

                                return overridable.overriddenSymbols.all { symbol ->
                                    val owner = symbol.owner as IrOverridableDeclaration<*>

                                    if (owner.overriddenSymbols.isEmpty() && owner.parentAsClass.isInterface) {
                                        return@all true
                                    }

                                    return isFakeOverrideOfInterface(owner)
                                }
                            }

                            val overridable =
                                it as? IrOverridableDeclaration<*> ?: when (it) {
                                    is IrProperty -> it.getter ?: it.setter ?: return@filter false
                                    else -> return@filter false
                                }

                            return@filter isFakeOverrideOfInterface(overridable)
                        }
                        .mapNotNull { it.accept(IrToDartClassMemberTransformer, context) }
                        .toList(),
                    annotations = irClass.dartAnnotations
                )
            }
            ClassKind.ENUM_ENTRY -> throw UnsupportedOperationException(
                "This should never throw: Enum entries should have been lowered into something else."
            )
        }
    }

    private fun DartTransformContext.visitExtension(
        irClass: IrClass,
        context: DartTransformContext
    ): DartCompilationUnitMember {
        return DartExtensionDeclaration(
            name = irClass.simpleDartName,
            extendedType = irClass.extensionTypeOrNull!!.accept(context),
            typeParameters = irClass.typeParameters.accept(context),
            members = irClass.declarations
                // A constructor is added in the IR, which is used when an extension has a conflicting overload
                // with another one. We don't want to add the constructor to the extension container in Dart.
                .filter { it !is IrConstructor }
                .mapNotNull { it.acceptAsClassMember(context) },
            annotations = irClass.dartAnnotations
        )
    }

    override fun DartTransformContext.visitField(irField: IrField, context: DartTransformContext) =
        DartTopLevelVariableDeclaration(
            variables = DartVariableDeclarationList(
                listOf(
                    DartVariableDeclaration(
                        name = irField.dartName,
                        expression = irField.initializer?.expression?.accept(context),
                    ),
                ),
                isConst = irField.isDartConst(),
                isFinal = irField.isFinal,
                isLate = irField.correspondingProperty?.isLateinit == true,
                type = irField.type.accept(context)
            ),
            annotations = irField.dartAnnotations,
        )
}

fun IrDeclaration.accept(context: DartTransformContext) = accept(IrToDartDeclarationTransformer, context)