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

import org.dotlin.compiler.backend.steps.ir2ast.DartAstTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.*
import org.dotlin.compiler.backend.util.isDartConst
import org.dotlin.compiler.dart.ast.compilationunit.DartCompilationUnitMember
import org.dotlin.compiler.dart.ast.declaration.classlike.*
import org.dotlin.compiler.dart.ast.declaration.function.DartTopLevelFunctionDeclaration
import org.dotlin.compiler.dart.ast.declaration.variable.DartTopLevelVariableDeclaration
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclaration
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclarationList
import org.dotlin.compiler.dart.ast.type.DartFunctionType
import org.dotlin.compiler.dart.ast.type.DartNamedType
import org.dotlin.compiler.dart.ast.`typealias`.DartClassTypeAlias
import org.dotlin.compiler.dart.ast.`typealias`.DartFunctionTypeAlias
import org.jetbrains.kotlin.descriptors.ClassKind.*
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object IrToDartDeclarationTransformer : IrDartAstTransformer<DartCompilationUnitMember>() {
    override fun DartAstTransformContext.visitSimpleFunction(
        irFunction: IrSimpleFunction,
        context: DartAstTransformContext
    ) =
        irFunction.transformBy(context) {
            DartTopLevelFunctionDeclaration(
                name,
                returnType,
                function,
                isGetter,
                isSetter,
                annotations = annotations,
                documentationComment = documentationComment,
            )
        }

    override fun DartAstTransformContext.visitClass(
        irClass: IrClass,
        context: DartAstTransformContext
    ): DartCompilationUnitMember {
        val name = irClass.simpleDartName
        val isDefaultValueClass = irClass.origin == IrDotlinDeclarationOrigin.COMPLEX_PARAM_DEFAULT_VALUE

        val superTypes = irClass.superTypes()

        val typeParameters = irClass.typeParameters.accept(context)

        // If the class is a default value for a complex parameter, we want to implement
        // it and not extend it, so we don't extend anything.
        val extendsClause = when {
            !isDefaultValueClass -> superTypes
                .baseClass()
                ?.let { if (it.isAny()) null else it }
                ?.accept(context, useFunctionInterface = true)
                ?.let { DartExtendsClause(it as DartNamedType) }

            else -> null
        }

        val implementsClause = when {
            // If our class is a default value for a complex parameter, we want it to implement
            // the type is a default value for, even if that type is a class and not an interface.
            isDefaultValueClass -> irClass.superTypes
            else -> superTypes.interfaces()
        }
            .mapNotNull { it.accept(context, useFunctionInterface = true) as? DartNamedType }
            .let {
                when {
                    it.isNotEmpty() -> DartImplementsClause(it)
                    else -> null
                }
            }

        val withClause = superTypes.mixins()
            .mapNotNull { it.accept(context, useFunctionInterface = true) as? DartNamedType }
            .let {
                when {
                    it.isNotEmpty() -> DartWithClause(it)
                    else -> null
                }
            }

        val members = irClass.declarations
            .asSequence()
            .filter {
                // A constructor is added in the IR, which is used when an extension has a conflicting overload
                // with another one. We don't want to add the constructor to the extension container in Dart.
                !irClass.isDartExtensionContainer || it !is IrConstructor
            }
            // We handle fake overrides only from regular interfaces and if this itself is not a
            // a regular interface.
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
                    // already an implementation there, and we don't have to include it.
                    if (!overridable.isFakeOverride()) return false

                    return overridable.overriddenSymbols.all { symbol ->
                        val owner = symbol.owner as IrOverridableDeclaration<*>

                        if (owner.overriddenSymbols.isEmpty() && owner.parentAsClass.isInterface) {
                            return@all true
                        }

                        return isFakeOverrideOfInterface(owner)
                    }
                }

                val overridable = it as? IrOverridableDeclaration<*> ?: return@filter false

                return@filter isFakeOverrideOfInterface(overridable)
            }
            .map { it.acceptAsClassMember(context) }
            .toList()

        val annotations = irClass.dartAnnotations

        return when {
            irClass.isEnumClass -> DartEnumDeclaration(
                name,
                typeParameters,
                implementsClause,
                withClause,
                constants = members.filterIsInstance<DartEnumDeclaration.Constant>(),
                members.filter { it !is DartEnumDeclaration.Constant },
                annotations,
            )

            irClass.isDartExtensionContainer -> DartExtensionDeclaration(
                irClass.simpleDartName,
                typeParameters,
                extendedType = irClass.extensionTypeOrNull!!.accept(context),
                members,
                annotations
            )

            else -> DartClassDeclaration(
                isAbstract = irClass.modality == Modality.ABSTRACT || irClass.isInterface,
                name,
                typeParameters,
                extendsClause,
                implementsClause,
                withClause,
                members,
                annotations,
            )
        }
    }

    override fun DartAstTransformContext.visitField(irField: IrField, context: DartAstTransformContext) =
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

    override fun DartAstTransformContext.visitTypeAlias(
        irAlias: IrTypeAlias,
        context: DartAstTransformContext
    ) = irAlias.let {
        val name = it.dartNameAsSimple
        val typeParameters = it.typeParameters.accept(context)

        when {
            it.expandedType.isFunctionTypeOrSubtype() -> DartFunctionTypeAlias(
                name, typeParameters,
                aliased = it.expandedType.accept(context) as DartFunctionType,
                annotations = irAlias.dartAnnotations
            )

            else -> DartClassTypeAlias(
                name, typeParameters,
                aliased = it.expandedType.accept(context) as DartNamedType,
                annotations = irAlias.dartAnnotations
            )
        }
    }
}

fun IrDeclaration.accept(context: DartAstTransformContext) = accept(IrToDartDeclarationTransformer, context)