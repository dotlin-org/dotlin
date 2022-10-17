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
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrCustomElementVisitor
import org.dotlin.compiler.backend.steps.ir2ast.ir.element.*
import org.dotlin.compiler.backend.steps.ir2ast.ir.todo
import org.dotlin.compiler.dart.ast.DartAstNode
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
abstract class IrDartAstTransformer<N : DartAstNode?> : IrCustomElementVisitor<N, DartTransformContext> {
    final override fun visitElement(element: IrElement, context: DartTransformContext): N {
        todo(element)
    }

    final override fun visitAnonymousInitializer(
        declaration: IrAnonymousInitializer,
        context: DartTransformContext
    ): N = context.run { visitAnonymousInitializer(declaration, context) }

    open fun DartTransformContext.visitAnonymousInitializer(
        declaration: IrAnonymousInitializer,
        context: DartTransformContext
    ): N = super.visitAnonymousInitializer(declaration, context)

    final override fun visitBlock(expression: IrBlock, context: DartTransformContext): N =
        context.run { visitBlock(expression, context) }

    open fun DartTransformContext.visitBlock(expression: IrBlock, context: DartTransformContext): N =
        super.visitBlock(expression, context)

    final override fun visitBlockBody(body: IrBlockBody, context: DartTransformContext): N =
        context.run { visitBlockBody(body, context) }

    open fun DartTransformContext.visitBlockBody(body: IrBlockBody, context: DartTransformContext): N =
        super.visitBlockBody(body, context)

    final override fun visitBranch(branch: IrBranch, context: DartTransformContext): N =
        context.run { visitBranch(branch, context) }

    open fun DartTransformContext.visitBranch(branch: IrBranch, context: DartTransformContext): N =
        super.visitBranch(branch, context)

    final override fun visitBreak(jump: IrBreak, context: DartTransformContext): N =
        context.run { visitBreak(jump, context) }

    open fun DartTransformContext.visitBreak(jump: IrBreak, context: DartTransformContext): N =
        super.visitBreak(jump, context)

    final override fun visitBreakContinue(jump: IrBreakContinue, context: DartTransformContext): N =
        context.run { visitBreakContinue(jump, context) }

    open fun DartTransformContext.visitBreakContinue(jump: IrBreakContinue, context: DartTransformContext): N =
        super.visitBreakContinue(jump, context)

    final override fun visitCall(expression: IrCall, context: DartTransformContext): N =
        context.run { visitCall(expression, context) }

    open fun DartTransformContext.visitCall(expression: IrCall, context: DartTransformContext): N =
        super.visitCall(expression, context)

    final override fun visitCallableReference(expression: IrCallableReference<*>, context: DartTransformContext): N =
        context.run { visitCallableReference(expression, context) }

    open fun DartTransformContext.visitCallableReference(
        expression: IrCallableReference<*>,
        context: DartTransformContext
    ): N = super.visitCallableReference(expression, context)

    final override fun visitCatch(aCatch: IrCatch, context: DartTransformContext): N =
        context.run { visitCatch(aCatch, context) }

    open fun DartTransformContext.visitCatch(aCatch: IrCatch, context: DartTransformContext): N =
        super.visitCatch(aCatch, context)

    final override fun visitClass(declaration: IrClass, context: DartTransformContext): N =
        context.run { visitClass(declaration, context) }

    open fun DartTransformContext.visitClass(declaration: IrClass, context: DartTransformContext): N =
        super.visitClass(declaration, context)

    final override fun visitClassReference(expression: IrClassReference, context: DartTransformContext): N =
        context.run { visitClassReference(expression, context) }

    open fun DartTransformContext.visitClassReference(expression: IrClassReference, context: DartTransformContext): N =
        super.visitClassReference(expression, context)

    final override fun visitComposite(expression: IrComposite, context: DartTransformContext): N =
        context.run { visitComposite(expression, context) }

    open fun DartTransformContext.visitComposite(expression: IrComposite, context: DartTransformContext): N =
        super.visitComposite(expression, context)

    final override fun <T> visitConst(expression: IrConst<T>, context: DartTransformContext): N =
        context.run { visitConst(expression, context) }

    open fun <T> DartTransformContext.visitConst(expression: IrConst<T>, context: DartTransformContext): N =
        super.visitConst(expression, context)

    final override fun visitConstructor(declaration: IrConstructor, context: DartTransformContext): N =
        context.run { visitConstructor(declaration, context) }

    open fun DartTransformContext.visitConstructor(declaration: IrConstructor, context: DartTransformContext): N =
        super.visitConstructor(declaration, context)

    final override fun visitConstructorCall(expression: IrConstructorCall, context: DartTransformContext): N =
        context.run { visitConstructorCall(expression, context) }

    open fun DartTransformContext.visitConstructorCall(
        expression: IrConstructorCall,
        context: DartTransformContext
    ): N = super.visitConstructorCall(expression, context)

    final override fun visitContainerExpression(expression: IrContainerExpression, context: DartTransformContext): N =
        context.run { visitContainerExpression(expression, context) }

    open fun DartTransformContext.visitContainerExpression(
        expression: IrContainerExpression,
        context: DartTransformContext
    ): N = super.visitContainerExpression(expression, context)

    final override fun visitContinue(jump: IrContinue, context: DartTransformContext): N =
        context.run { visitContinue(jump, context) }

    open fun DartTransformContext.visitContinue(jump: IrContinue, context: DartTransformContext): N =
        super.visitContinue(jump, context)

    final override fun visitDeclaration(declaration: IrDeclarationBase, context: DartTransformContext): N =
        context.run { visitDeclaration(declaration, context) }

    open fun DartTransformContext.visitDeclaration(declaration: IrDeclarationBase, context: DartTransformContext): N =
        super.visitDeclaration(declaration, context)

    final override fun visitDeclarationReference(expression: IrDeclarationReference, context: DartTransformContext): N =
        context.run { visitDeclarationReference(expression, context) }

    open fun DartTransformContext.visitDeclarationReference(
        expression: IrDeclarationReference,
        context: DartTransformContext
    ): N = super.visitDeclarationReference(expression, context)

    final override fun visitDelegatingConstructorCall(
        expression: IrDelegatingConstructorCall,
        context: DartTransformContext
    ): N = context.run { visitDelegatingConstructorCall(expression, context) }

    open fun DartTransformContext.visitDelegatingConstructorCall(
        expression: IrDelegatingConstructorCall,
        context: DartTransformContext
    ): N = super.visitDelegatingConstructorCall(expression, context)

    final override fun visitDoWhileLoop(loop: IrDoWhileLoop, context: DartTransformContext): N =
        context.run { visitDoWhileLoop(loop, context) }

    open fun DartTransformContext.visitDoWhileLoop(loop: IrDoWhileLoop, context: DartTransformContext): N =
        super.visitDoWhileLoop(loop, context)

    final override fun visitDynamicExpression(expression: IrDynamicExpression, context: DartTransformContext): N =
        context.run { visitDynamicExpression(expression, context) }

    open fun DartTransformContext.visitDynamicExpression(
        expression: IrDynamicExpression,
        context: DartTransformContext
    ): N = super.visitDynamicExpression(expression, context)

    final override fun visitDynamicMemberExpression(
        expression: IrDynamicMemberExpression,
        context: DartTransformContext
    ): N = context.run { visitDynamicMemberExpression(expression, context) }

    open fun DartTransformContext.visitDynamicMemberExpression(
        expression: IrDynamicMemberExpression,
        context: DartTransformContext
    ): N = super.visitDynamicMemberExpression(expression, context)

    final override fun visitDynamicOperatorExpression(
        expression: IrDynamicOperatorExpression,
        context: DartTransformContext
    ): N = context.run { visitDynamicOperatorExpression(expression, context) }

    open fun DartTransformContext.visitDynamicOperatorExpression(
        expression: IrDynamicOperatorExpression,
        context: DartTransformContext
    ): N = super.visitDynamicOperatorExpression(expression, context)

    final override fun visitElseBranch(branch: IrElseBranch, context: DartTransformContext): N =
        context.run { visitElseBranch(branch, context) }

    open fun DartTransformContext.visitElseBranch(branch: IrElseBranch, context: DartTransformContext): N =
        super.visitElseBranch(branch, context)

    final override fun visitEnumConstructorCall(expression: IrEnumConstructorCall, context: DartTransformContext): N =
        context.run { visitEnumConstructorCall(expression, context) }

    open fun DartTransformContext.visitEnumConstructorCall(
        expression: IrEnumConstructorCall,
        context: DartTransformContext
    ): N = super.visitEnumConstructorCall(expression, context)

    final override fun visitEnumEntry(declaration: IrEnumEntry, context: DartTransformContext): N =
        context.run { visitEnumEntry(declaration, context) }

    open fun DartTransformContext.visitEnumEntry(declaration: IrEnumEntry, context: DartTransformContext): N =
        super.visitEnumEntry(declaration, context)

    final override fun visitErrorCallExpression(expression: IrErrorCallExpression, context: DartTransformContext): N =
        context.run { visitErrorCallExpression(expression, context) }

    open fun DartTransformContext.visitErrorCallExpression(
        expression: IrErrorCallExpression,
        context: DartTransformContext
    ): N = super.visitErrorCallExpression(expression, context)

    final override fun visitErrorDeclaration(declaration: IrErrorDeclaration, context: DartTransformContext): N =
        context.run { visitErrorDeclaration(declaration, context) }

    open fun DartTransformContext.visitErrorDeclaration(
        declaration: IrErrorDeclaration,
        context: DartTransformContext
    ): N =
        super.visitErrorDeclaration(declaration, context)

    final override fun visitErrorExpression(expression: IrErrorExpression, context: DartTransformContext): N =
        context.run { visitErrorExpression(expression, context) }

    open fun DartTransformContext.visitErrorExpression(
        expression: IrErrorExpression,
        context: DartTransformContext
    ): N = super.visitErrorExpression(expression, context)

    final override fun visitExpressionBody(body: IrExpressionBody, context: DartTransformContext): N =
        context.run { visitExpressionBody(body, context) }

    open fun DartTransformContext.visitExpressionBody(body: IrExpressionBody, context: DartTransformContext): N =
        super.visitExpressionBody(body, context)

    final override fun visitExternalPackageFragment(
        declaration: IrExternalPackageFragment,
        context: DartTransformContext
    ): N = context.run { visitExternalPackageFragment(declaration, context) }

    open fun DartTransformContext.visitExternalPackageFragment(
        declaration: IrExternalPackageFragment,
        context: DartTransformContext
    ): N = super.visitExternalPackageFragment(declaration, context)

    final override fun visitField(declaration: IrField, context: DartTransformContext): N =
        context.run { visitField(declaration, context) }

    open fun DartTransformContext.visitField(declaration: IrField, context: DartTransformContext): N =
        super.visitField(declaration, context)

    final override fun visitFieldAccess(expression: IrFieldAccessExpression, context: DartTransformContext): N =
        context.run { visitFieldAccess(expression, context) }

    open fun DartTransformContext.visitFieldAccess(
        expression: IrFieldAccessExpression,
        context: DartTransformContext
    ): N = super.visitFieldAccess(expression, context)

    final override fun visitFile(declaration: IrFile, context: DartTransformContext): N =
        context.run { visitFile(declaration, context) }

    open fun DartTransformContext.visitFile(declaration: IrFile, context: DartTransformContext): N =
        super.visitFile(declaration, context)

    final override fun visitFunction(declaration: IrFunction, context: DartTransformContext): N =
        context.run { visitFunction(declaration, context) }

    open fun DartTransformContext.visitFunction(declaration: IrFunction, context: DartTransformContext): N =
        super.visitFunction(declaration, context)

    final override fun visitFunctionAccess(expression: IrFunctionAccessExpression, context: DartTransformContext): N =
        context.run { visitFunctionAccess(expression, context) }

    open fun DartTransformContext.visitFunctionAccess(
        expression: IrFunctionAccessExpression,
        context: DartTransformContext
    ): N = super.visitFunctionAccess(expression, context)

    final override fun visitFunctionExpression(expression: IrFunctionExpression, context: DartTransformContext): N =
        context.run { visitFunctionExpression(expression, context) }

    open fun DartTransformContext.visitFunctionExpression(
        expression: IrFunctionExpression,
        context: DartTransformContext
    ): N =
        super.visitFunctionExpression(expression, context)

    final override fun visitFunctionReference(expression: IrFunctionReference, context: DartTransformContext): N =
        context.run { visitFunctionReference(expression, context) }

    open fun DartTransformContext.visitFunctionReference(
        expression: IrFunctionReference,
        context: DartTransformContext
    ): N = super.visitFunctionReference(expression, context)

    final override fun visitGetClass(expression: IrGetClass, context: DartTransformContext): N =
        context.run { visitGetClass(expression, context) }

    open fun DartTransformContext.visitGetClass(expression: IrGetClass, context: DartTransformContext): N =
        super.visitGetClass(expression, context)

    final override fun visitGetEnumValue(expression: IrGetEnumValue, context: DartTransformContext): N =
        context.run { visitGetEnumValue(expression, context) }

    open fun DartTransformContext.visitGetEnumValue(expression: IrGetEnumValue, context: DartTransformContext): N =
        super.visitGetEnumValue(expression, context)

    final override fun visitGetField(expression: IrGetField, context: DartTransformContext): N =
        context.run { visitGetField(expression, context) }

    open fun DartTransformContext.visitGetField(expression: IrGetField, context: DartTransformContext): N =
        super.visitGetField(expression, context)

    final override fun visitGetObjectValue(expression: IrGetObjectValue, context: DartTransformContext): N =
        context.run { visitGetObjectValue(expression, context) }

    open fun DartTransformContext.visitGetObjectValue(expression: IrGetObjectValue, context: DartTransformContext): N =
        super.visitGetObjectValue(expression, context)

    final override fun visitGetValue(expression: IrGetValue, context: DartTransformContext): N =
        context.run { visitGetValue(expression, context) }

    open fun DartTransformContext.visitGetValue(expression: IrGetValue, context: DartTransformContext): N =
        super.visitGetValue(expression, context)

    final override fun visitInstanceInitializerCall(
        expression: IrInstanceInitializerCall,
        context: DartTransformContext
    ): N = context.run { visitInstanceInitializerCall(expression, context) }

    open fun DartTransformContext.visitInstanceInitializerCall(
        expression: IrInstanceInitializerCall,
        context: DartTransformContext
    ): N = super.visitInstanceInitializerCall(expression, context)

    final override fun visitLocalDelegatedProperty(
        declaration: IrLocalDelegatedProperty,
        context: DartTransformContext
    ): N = context.run { visitLocalDelegatedProperty(declaration, context) }

    open fun DartTransformContext.visitLocalDelegatedProperty(
        declaration: IrLocalDelegatedProperty,
        context: DartTransformContext
    ): N = super.visitLocalDelegatedProperty(declaration, context)

    final override fun visitLocalDelegatedPropertyReference(
        expression: IrLocalDelegatedPropertyReference,
        context: DartTransformContext
    ): N = context.run { visitLocalDelegatedPropertyReference(expression, context) }

    open fun DartTransformContext.visitLocalDelegatedPropertyReference(
        expression: IrLocalDelegatedPropertyReference,
        context: DartTransformContext
    ): N = super.visitLocalDelegatedPropertyReference(expression, context)

    final override fun visitLoop(loop: IrLoop, context: DartTransformContext): N =
        context.run { visitLoop(loop, context) }

    open fun DartTransformContext.visitLoop(loop: IrLoop, context: DartTransformContext): N =
        super.visitLoop(loop, context)

    final override fun visitMemberAccess(expression: IrMemberAccessExpression<*>, context: DartTransformContext): N =
        context.run { visitMemberAccess(expression, context) }

    open fun DartTransformContext.visitMemberAccess(
        expression: IrMemberAccessExpression<*>,
        context: DartTransformContext
    ): N = super.visitMemberAccess(expression, context)

    final override fun visitModuleFragment(declaration: IrModuleFragment, context: DartTransformContext): N =
        context.run { visitModuleFragment(declaration, context) }

    open fun DartTransformContext.visitModuleFragment(declaration: IrModuleFragment, context: DartTransformContext): N =
        super.visitModuleFragment(declaration, context)

    final override fun visitPackageFragment(declaration: IrPackageFragment, context: DartTransformContext): N =
        context.run { visitPackageFragment(declaration, context) }

    open fun DartTransformContext.visitPackageFragment(
        declaration: IrPackageFragment,
        context: DartTransformContext
    ): N = super.visitPackageFragment(declaration, context)

    final override fun visitProperty(declaration: IrProperty, context: DartTransformContext): N =
        context.run { visitProperty(declaration, context) }

    open fun DartTransformContext.visitProperty(declaration: IrProperty, context: DartTransformContext): N =
        super.visitProperty(declaration, context)

    final override fun visitPropertyReference(expression: IrPropertyReference, context: DartTransformContext): N =
        context.run { visitPropertyReference(expression, context) }

    open fun DartTransformContext.visitPropertyReference(
        expression: IrPropertyReference,
        context: DartTransformContext
    ): N = super.visitPropertyReference(expression, context)

    final override fun visitRawFunctionReference(expression: IrRawFunctionReference, context: DartTransformContext): N =
        context.run { visitRawFunctionReference(expression, context) }

    open fun DartTransformContext.visitRawFunctionReference(
        expression: IrRawFunctionReference,
        context: DartTransformContext
    ): N = super.visitRawFunctionReference(expression, context)

    final override fun visitReturn(expression: IrReturn, context: DartTransformContext): N =
        context.run { visitReturn(expression, context) }

    open fun DartTransformContext.visitReturn(expression: IrReturn, context: DartTransformContext): N =
        super.visitReturn(expression, context)

    final override fun visitScript(declaration: IrScript, context: DartTransformContext): N =
        context.run { visitScript(declaration, context) }

    open fun DartTransformContext.visitScript(declaration: IrScript, context: DartTransformContext): N =
        super.visitScript(declaration, context)

    final override fun visitSetField(expression: IrSetField, context: DartTransformContext): N =
        context.run { visitSetField(expression, context) }

    open fun DartTransformContext.visitSetField(expression: IrSetField, context: DartTransformContext): N =
        super.visitSetField(expression, context)

    final override fun visitSetValue(expression: IrSetValue, context: DartTransformContext): N =
        context.run { visitSetValue(expression, context) }

    open fun DartTransformContext.visitSetValue(expression: IrSetValue, context: DartTransformContext): N =
        super.visitSetValue(expression, context)

    final override fun visitSimpleFunction(declaration: IrSimpleFunction, context: DartTransformContext): N =
        context.run { visitSimpleFunction(declaration, context) }

    open fun DartTransformContext.visitSimpleFunction(declaration: IrSimpleFunction, context: DartTransformContext): N =
        super.visitSimpleFunction(declaration, context)

    final override fun visitSingletonReference(expression: IrGetSingletonValue, context: DartTransformContext): N =
        context.run { visitSingletonReference(expression, context) }

    open fun DartTransformContext.visitSingletonReference(
        expression: IrGetSingletonValue,
        context: DartTransformContext
    ): N = super.visitSingletonReference(expression, context)

    final override fun visitSpreadElement(spread: IrSpreadElement, context: DartTransformContext): N =
        context.run { visitSpreadElement(spread, context) }

    open fun DartTransformContext.visitSpreadElement(spread: IrSpreadElement, context: DartTransformContext): N =
        super.visitSpreadElement(spread, context)

    final override fun visitStringConcatenation(expression: IrStringConcatenation, context: DartTransformContext): N =
        context.run { visitStringConcatenation(expression, context) }

    open fun DartTransformContext.visitStringConcatenation(
        expression: IrStringConcatenation,
        context: DartTransformContext
    ): N = super.visitStringConcatenation(expression, context)

    final override fun visitSuspendableExpression(
        expression: IrSuspendableExpression,
        context: DartTransformContext
    ): N = context.run { visitSuspendableExpression(expression, context) }

    open fun DartTransformContext.visitSuspendableExpression(
        expression: IrSuspendableExpression,
        context: DartTransformContext
    ): N = super.visitSuspendableExpression(expression, context)

    final override fun visitSuspensionPoint(expression: IrSuspensionPoint, context: DartTransformContext): N =
        context.run { visitSuspensionPoint(expression, context) }

    open fun DartTransformContext.visitSuspensionPoint(
        expression: IrSuspensionPoint,
        context: DartTransformContext
    ): N = super.visitSuspensionPoint(expression, context)

    final override fun visitSyntheticBody(body: IrSyntheticBody, context: DartTransformContext): N =
        context.run { visitSyntheticBody(body, context) }

    open fun DartTransformContext.visitSyntheticBody(body: IrSyntheticBody, context: DartTransformContext): N =
        super.visitSyntheticBody(body, context)

    final override fun visitThrow(expression: IrThrow, context: DartTransformContext): N =
        context.run { visitThrow(expression, context) }

    open fun DartTransformContext.visitThrow(expression: IrThrow, context: DartTransformContext): N =
        super.visitThrow(expression, context)

    final override fun visitTry(aTry: IrTry, context: DartTransformContext): N = context.run { visitTry(aTry, context) }

    open fun DartTransformContext.visitTry(aTry: IrTry, context: DartTransformContext): N =
        super.visitTry(aTry, context)

    final override fun visitTypeAlias(declaration: IrTypeAlias, context: DartTransformContext): N =
        context.run { visitTypeAlias(declaration, context) }

    open fun DartTransformContext.visitTypeAlias(declaration: IrTypeAlias, context: DartTransformContext): N =
        super.visitTypeAlias(declaration, context)

    final override fun visitTypeOperator(expression: IrTypeOperatorCall, context: DartTransformContext): N =
        context.run { visitTypeOperator(expression, context) }

    open fun DartTransformContext.visitTypeOperator(expression: IrTypeOperatorCall, context: DartTransformContext): N =
        super.visitTypeOperator(expression, context)

    final override fun visitTypeParameter(declaration: IrTypeParameter, context: DartTransformContext): N =
        context.run { visitTypeParameter(declaration, context) }

    open fun DartTransformContext.visitTypeParameter(declaration: IrTypeParameter, context: DartTransformContext): N =
        super.visitTypeParameter(declaration, context)

    final override fun visitValueAccess(expression: IrValueAccessExpression, context: DartTransformContext): N =
        context.run { visitValueAccess(expression, context) }

    open fun DartTransformContext.visitValueAccess(
        expression: IrValueAccessExpression,
        context: DartTransformContext
    ): N = super.visitValueAccess(expression, context)

    final override fun visitValueParameter(declaration: IrValueParameter, context: DartTransformContext): N =
        context.run { visitValueParameter(declaration, context) }

    open fun DartTransformContext.visitValueParameter(declaration: IrValueParameter, context: DartTransformContext): N =
        super.visitValueParameter(declaration, context)

    final override fun visitVararg(expression: IrVararg, context: DartTransformContext): N =
        context.run { visitVararg(expression, context) }

    open fun DartTransformContext.visitVararg(expression: IrVararg, context: DartTransformContext): N =
        super.visitVararg(expression, context)

    final override fun visitVariable(declaration: IrVariable, context: DartTransformContext): N =
        context.run { visitVariable(declaration, context) }

    open fun DartTransformContext.visitVariable(declaration: IrVariable, context: DartTransformContext): N =
        super.visitVariable(declaration, context)

    final override fun visitWhen(expression: IrWhen, context: DartTransformContext): N =
        context.run { visitWhen(expression, context) }

    open fun DartTransformContext.visitWhen(expression: IrWhen, context: DartTransformContext): N =
        super.visitWhen(expression, context)

    final override fun visitWhileLoop(loop: IrWhileLoop, context: DartTransformContext): N =
        context.run { visitWhileLoop(loop, context) }

    open fun DartTransformContext.visitWhileLoop(loop: IrWhileLoop, context: DartTransformContext): N =
        super.visitWhileLoop(loop, context)

    final override fun visitExpression(expression: IrExpression, context: DartTransformContext): N =
        context.run { visitExpression(expression, context) }

    open fun DartTransformContext.visitExpression(expression: IrExpression, context: DartTransformContext): N =
        super.visitExpression(expression, context)

    final override fun visitBody(body: IrBody, context: DartTransformContext): N =
        context.run { visitBody(body, context) }

    open fun DartTransformContext.visitBody(body: IrBody, context: DartTransformContext): N =
        super.visitBody(body, context)

    final override fun visitDartCodeExpression(expression: IrDartCodeExpression, context: DartTransformContext): N =
        context.run { visitDartCodeExpression(expression, context) }

    open fun DartTransformContext.visitDartCodeExpression(
        expression: IrDartCodeExpression,
        context: DartTransformContext
    ): N = super.visitDartCodeExpression(expression, context)

    final override fun visitNullAwareExpression(expression: IrNullAwareExpression, context: DartTransformContext): N =
        context.run { visitNullAwareExpression(expression, context) }

    open fun DartTransformContext.visitNullAwareExpression(
        expression: IrNullAwareExpression,
        context: DartTransformContext
    ): N = super.visitNullAwareExpression(expression, context)

    final override fun visitExpressionBodyWithOrigin(
        body: IrExpressionBodyWithOrigin,
        context: DartTransformContext
    ): N = context.run { visitExpressionBodyWithOrigin(body, context) }

    open fun DartTransformContext.visitExpressionBodyWithOrigin(
        body: IrExpressionBodyWithOrigin,
        context: DartTransformContext
    ): N = super.visitExpressionBodyWithOrigin(body, context)

    final override fun visitBinaryInfixExpression(
        expression: IrBinaryInfixExpression,
        context: DartTransformContext
    ): N = context.run { visitBinaryInfixExpression(expression, context) }

    open fun DartTransformContext.visitBinaryInfixExpression(
        expression: IrBinaryInfixExpression,
        context: DartTransformContext
    ): N = super.visitBinaryInfixExpression(expression, context)

    final override fun visitIfNullExpression(expression: IrIfNullExpression, context: DartTransformContext): N =
        context.run { visitIfNullExpression(expression, context) }

    open fun DartTransformContext.visitIfNullExpression(
        expression: IrIfNullExpression,
        context: DartTransformContext
    ): N = super.visitIfNullExpression(expression, context)

    final override fun visitConjunctionExpression(
        expression: IrConjunctionExpression,
        context: DartTransformContext
    ): N = context.run { visitConjunctionExpression(expression, context) }

    open fun DartTransformContext.visitConjunctionExpression(
        expression: IrConjunctionExpression,
        context: DartTransformContext
    ): N = super.visitConjunctionExpression(expression, context)

    final override fun visitDisjunctionExpression(
        expression: IrDisjunctionExpression,
        context: DartTransformContext
    ): N = context.run { visitDisjunctionExpression(expression, context) }

    open fun DartTransformContext.visitDisjunctionExpression(
        expression: IrDisjunctionExpression,
        context: DartTransformContext
    ): N = super.visitDisjunctionExpression(expression, context)
}