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
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.acceptAnnotations
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.isDartFactory
import org.dotlin.compiler.backend.util.isDartConst
import org.dotlin.compiler.dart.ast.declaration.classlike.DartEnumDeclaration
import org.dotlin.compiler.dart.ast.declaration.classlike.member.DartClassMember
import org.dotlin.compiler.dart.ast.declaration.classlike.member.DartFieldDeclaration
import org.dotlin.compiler.dart.ast.declaration.classlike.member.DartMethodDeclaration
import org.dotlin.compiler.dart.ast.declaration.classlike.member.constructor.*
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclaration
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclarationList
import org.dotlin.compiler.dart.ast.expression.DartArgumentList
import org.dotlin.compiler.dart.ast.expression.DartFunctionExpression
import org.dotlin.compiler.dart.ast.type.DartNamedType
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrEnumConstructorCall
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.parentAsClass

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object IrToDartClassMemberTransformer : IrDartAstTransformer<DartClassMember>() {
    override fun DartAstTransformContext.visitSimpleFunction(irFunction: IrSimpleFunction, context: DartAstTransformContext) =
        irFunction.transformBy(context) {
            DartMethodDeclaration(
                name,
                returnType,
                function,
                isGetter,
                isSetter,
                isOperator = irFunction.isOperator,
                isStatic = irFunction.isStatic,
                annotations = annotations,
                documentationComment = documentationComment,
            )
        }

    override fun DartAstTransformContext.visitConstructor(irConstructor: IrConstructor, context: DartAstTransformContext) =
        context.run {
            irConstructor.transformBy(context) {
                val initializers = (irConstructor.body as? IrBlockBody)?.run {
                    // Constructor parameters with complex default values are initialized in the body.
                    // We move them to the  Dart field initializer list, if possible.
                    statements
                        .filter { it.propertyItAssignsTo?.isInitializedInFieldInitializerList == true }
                        .also { statements.removeAll(it) }
                        .map {
                            DartConstructorFieldInitializer(
                                fieldName = it.propertyItAssignsTo!!.simpleDartName,
                                expression = it.rightHandOfSet!!.accept(context)
                            )
                        }
                        // Handle super/this constructor call.
                        .plus(
                            statements
                                .filterIsInstance<IrDelegatingConstructorCall>()
                                .singleOrNull()
                                ?.let { irDelegatingConstructorCall ->
                                    val delegateIrConstructor = irDelegatingConstructorCall.symbol.owner

                                    val name = delegateIrConstructor.dartNameOrNull
                                    val arguments = irDelegatingConstructorCall.acceptArguments(context)

                                    statements.remove(irDelegatingConstructorCall)

                                    val delegateIsOurs = irConstructor
                                        .parentAsClass
                                        .declarations
                                        .any { it.symbol == delegateIrConstructor.symbol }

                                    // If the delegate constructor is in our class, we call `this`
                                    // otherwise we call `super`.
                                    when {
                                        delegateIsOurs -> DartRedirectingConstructorInvocation(name, arguments)
                                        else -> DartSuperConstructorInvocation(name, arguments)
                                    }
                            }
                    )
            }?.filterNotNull() ?: emptyList()

            DartConstructorDeclaration(
                returnType = irConstructor.parentAsClass.defaultType.accept(context, isConstructorType = true).let {
                    it as DartNamedType
                    // Type arguments are cleared, they're not allowed in constructors.
                    it.copy(typeArguments = DartTypeArgumentList())
                },
                name = irConstructor.dartNameOrNull?.let {
                    // TODO: Do this for all names everywhere?
                    if (irConstructor.origin != IrDeclarationOrigin.DEFINED) it.asGenerated() else it
                },
                isConst = irConstructor.isDartConst(),
                annotations = annotations,
                documentationComment = documentationComment,
                isFactory = irConstructor.isDartFactory,
                initializers = initializers,
                function = DartFunctionExpression(
                    parameters = parameters,
                    body = irConstructor.body.accept(context, allowEmpty = true)
                )
            )
            }
        }

    override fun DartAstTransformContext.visitField(
        irField: IrField,
        context: DartAstTransformContext
    ): DartFieldDeclaration = context.run {
        val fieldName = irField.dartName
        val fieldType = irField.type.accept(context)

        val irProperty = irField.correspondingProperty

        val isFinal: Boolean
        val isConst: Boolean
        val isAbstract: Boolean
        val isLate: Boolean

        irProperty.let {
            isFinal = it?.isVar != true
            isConst = irField.isDartConst() || it?.isConst == true
            isAbstract = it?.modality == Modality.ABSTRACT
            isLate = it?.isLateinit == true || it?.isInitializedInConstructorBody == true
        }

        val initializer = when {
            // Only if the property is not initialized anywhere else will we add the initializer.
            irProperty == null || !irProperty.isInitializedByParameter && !irProperty.isInitializedSomewhereElse -> {
                irField.initializer?.accept(context)
            }
            else -> null
        }

        return DartFieldDeclaration(
            fields = DartVariableDeclarationList(
                DartVariableDeclaration(
                    name = fieldName,
                    expression = initializer,
                ),
                isFinal = isFinal,
                isConst = isConst,
                isLate = isLate,
                type = fieldType
            ),
            isStatic = irField.isStatic,
            isAbstract = isAbstract,
            annotations = irField.acceptAnnotations(context)
        )
    }

    override fun DartAstTransformContext.visitEnumEntry(
        declaration: IrEnumEntry,
        context: DartAstTransformContext
    ): DartClassMember {
        val irEnumConstructorCall = declaration.initializerExpression?.expression as? IrEnumConstructorCall
        val irEnumConstructor = irEnumConstructorCall?.symbol?.owner

        return DartEnumDeclaration.Constant(
            name = declaration.simpleDartName,
            constructorName = irEnumConstructor?.dartNameOrNull,
            arguments = DartArgumentList(
                irEnumConstructorCall?.valueArguments?.mapNotNull { it?.accept(context) }.orEmpty()
            )
        )
    }
}

fun IrDeclaration.acceptAsClassMember(context: DartAstTransformContext) = accept(IrToDartClassMemberTransformer, context)