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
import org.dotlin.compiler.backend.steps.ir2ast.ir.todo
import org.dotlin.compiler.dart.ast.DartAstNode
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
abstract class IrDartAstTransformer<N : DartAstNode?> : IrElementVisitor<N, DartAstTransformContext> {
    final override fun visitElement(element: IrElement, context: DartAstTransformContext): N {
        todo(element)
    }

    final override fun visitAnonymousInitializer(
        declaration: IrAnonymousInitializer,
        context: DartAstTransformContext
    ): N = context.run { visitAnonymousInitializer(declaration, context) }

    open fun DartAstTransformContext.visitAnonymousInitializer(
        declaration: IrAnonymousInitializer,
        context: DartAstTransformContext
    ): N = super.visitAnonymousInitializer(declaration, context)

    final override fun visitBlock(expression: IrBlock, context: DartAstTransformContext): N =
        context.run { visitBlock(expression, context) }

    open fun DartAstTransformContext.visitBlock(expression: IrBlock, context: DartAstTransformContext): N =
        super.visitBlock(expression, context)

    final override fun visitBlockBody(body: IrBlockBody, context: DartAstTransformContext): N =
        context.run { visitBlockBody(body, context) }

    open fun DartAstTransformContext.visitBlockBody(body: IrBlockBody, context: DartAstTransformContext): N =
        super.visitBlockBody(body, context)

    final override fun visitBranch(branch: IrBranch, context: DartAstTransformContext): N =
        context.run { visitBranch(branch, context) }

    open fun DartAstTransformContext.visitBranch(branch: IrBranch, context: DartAstTransformContext): N =
        super.visitBranch(branch, context)

    final override fun visitBreak(jump: IrBreak, context: DartAstTransformContext): N =
        context.run { visitBreak(jump, context) }

    open fun DartAstTransformContext.visitBreak(jump: IrBreak, context: DartAstTransformContext): N =
        super.visitBreak(jump, context)

    final override fun visitBreakContinue(jump: IrBreakContinue, context: DartAstTransformContext): N =
        context.run { visitBreakContinue(jump, context) }

    open fun DartAstTransformContext.visitBreakContinue(jump: IrBreakContinue, context: DartAstTransformContext): N =
        super.visitBreakContinue(jump, context)

    final override fun visitCall(expression: IrCall, context: DartAstTransformContext): N =
        context.run { visitCall(expression, context) }

    open fun DartAstTransformContext.visitCall(expression: IrCall, context: DartAstTransformContext): N =
        super.visitCall(expression, context)

    final override fun visitCallableReference(expression: IrCallableReference<*>, context: DartAstTransformContext): N =
        context.run { visitCallableReference(expression, context) }

    open fun DartAstTransformContext.visitCallableReference(
        expression: IrCallableReference<*>,
        context: DartAstTransformContext
    ): N = super.visitCallableReference(expression, context)

    final override fun visitCatch(aCatch: IrCatch, context: DartAstTransformContext): N =
        context.run { visitCatch(aCatch, context) }

    open fun DartAstTransformContext.visitCatch(aCatch: IrCatch, context: DartAstTransformContext): N =
        super.visitCatch(aCatch, context)

    final override fun visitClass(declaration: IrClass, context: DartAstTransformContext): N =
        context.run { visitClass(declaration, context) }

    open fun DartAstTransformContext.visitClass(declaration: IrClass, context: DartAstTransformContext): N =
        super.visitClass(declaration, context)

    final override fun visitClassReference(expression: IrClassReference, context: DartAstTransformContext): N =
        context.run { visitClassReference(expression, context) }

    open fun DartAstTransformContext.visitClassReference(expression: IrClassReference, context: DartAstTransformContext): N =
        super.visitClassReference(expression, context)

    final override fun visitComposite(expression: IrComposite, context: DartAstTransformContext): N =
        context.run { visitComposite(expression, context) }

    open fun DartAstTransformContext.visitComposite(expression: IrComposite, context: DartAstTransformContext): N =
        super.visitComposite(expression, context)

    final override fun visitConst(expression: IrConst<*>, context: DartAstTransformContext): N =
        context.run { visitConst(expression, context) }

    open fun DartAstTransformContext.visitConst(expression: IrConst<*>, context: DartAstTransformContext): N =
        super.visitConst(expression, context)

    final override fun visitConstructor(declaration: IrConstructor, context: DartAstTransformContext): N =
        context.run { visitConstructor(declaration, context) }

    open fun DartAstTransformContext.visitConstructor(declaration: IrConstructor, context: DartAstTransformContext): N =
        super.visitConstructor(declaration, context)

    final override fun visitConstructorCall(expression: IrConstructorCall, context: DartAstTransformContext): N =
        context.run { visitConstructorCall(expression, context) }

    open fun DartAstTransformContext.visitConstructorCall(
        expression: IrConstructorCall,
        context: DartAstTransformContext
    ): N = super.visitConstructorCall(expression, context)

    final override fun visitContainerExpression(expression: IrContainerExpression, context: DartAstTransformContext): N =
        context.run { visitContainerExpression(expression, context) }

    open fun DartAstTransformContext.visitContainerExpression(
        expression: IrContainerExpression,
        context: DartAstTransformContext
    ): N = super.visitContainerExpression(expression, context)

    final override fun visitContinue(jump: IrContinue, context: DartAstTransformContext): N =
        context.run { visitContinue(jump, context) }

    open fun DartAstTransformContext.visitContinue(jump: IrContinue, context: DartAstTransformContext): N =
        super.visitContinue(jump, context)

    final override fun visitDeclaration(declaration: IrDeclarationBase, context: DartAstTransformContext): N =
        context.run { visitDeclaration(declaration, context) }

    open fun DartAstTransformContext.visitDeclaration(declaration: IrDeclarationBase, context: DartAstTransformContext): N =
        super.visitDeclaration(declaration, context)

    final override fun visitDeclarationReference(expression: IrDeclarationReference, context: DartAstTransformContext): N =
        context.run { visitDeclarationReference(expression, context) }

    open fun DartAstTransformContext.visitDeclarationReference(
        expression: IrDeclarationReference,
        context: DartAstTransformContext
    ): N = super.visitDeclarationReference(expression, context)

    final override fun visitDelegatingConstructorCall(
        expression: IrDelegatingConstructorCall,
        context: DartAstTransformContext
    ): N = context.run { visitDelegatingConstructorCall(expression, context) }

    open fun DartAstTransformContext.visitDelegatingConstructorCall(
        expression: IrDelegatingConstructorCall,
        context: DartAstTransformContext
    ): N = super.visitDelegatingConstructorCall(expression, context)

    final override fun visitDoWhileLoop(loop: IrDoWhileLoop, context: DartAstTransformContext): N =
        context.run { visitDoWhileLoop(loop, context) }

    open fun DartAstTransformContext.visitDoWhileLoop(loop: IrDoWhileLoop, context: DartAstTransformContext): N =
        super.visitDoWhileLoop(loop, context)

    final override fun visitDynamicExpression(expression: IrDynamicExpression, context: DartAstTransformContext): N =
        context.run { visitDynamicExpression(expression, context) }

    open fun DartAstTransformContext.visitDynamicExpression(
        expression: IrDynamicExpression,
        context: DartAstTransformContext
    ): N = super.visitDynamicExpression(expression, context)

    final override fun visitDynamicMemberExpression(
        expression: IrDynamicMemberExpression,
        context: DartAstTransformContext
    ): N = context.run { visitDynamicMemberExpression(expression, context) }

    open fun DartAstTransformContext.visitDynamicMemberExpression(
        expression: IrDynamicMemberExpression,
        context: DartAstTransformContext
    ): N = super.visitDynamicMemberExpression(expression, context)

    final override fun visitDynamicOperatorExpression(
        expression: IrDynamicOperatorExpression,
        context: DartAstTransformContext
    ): N = context.run { visitDynamicOperatorExpression(expression, context) }

    open fun DartAstTransformContext.visitDynamicOperatorExpression(
        expression: IrDynamicOperatorExpression,
        context: DartAstTransformContext
    ): N = super.visitDynamicOperatorExpression(expression, context)

    final override fun visitElseBranch(branch: IrElseBranch, context: DartAstTransformContext): N =
        context.run { visitElseBranch(branch, context) }

    open fun DartAstTransformContext.visitElseBranch(branch: IrElseBranch, context: DartAstTransformContext): N =
        super.visitElseBranch(branch, context)

    final override fun visitEnumConstructorCall(expression: IrEnumConstructorCall, context: DartAstTransformContext): N =
        context.run { visitEnumConstructorCall(expression, context) }

    open fun DartAstTransformContext.visitEnumConstructorCall(
        expression: IrEnumConstructorCall,
        context: DartAstTransformContext
    ): N = super.visitEnumConstructorCall(expression, context)

    final override fun visitEnumEntry(declaration: IrEnumEntry, context: DartAstTransformContext): N =
        context.run { visitEnumEntry(declaration, context) }

    open fun DartAstTransformContext.visitEnumEntry(declaration: IrEnumEntry, context: DartAstTransformContext): N =
        super.visitEnumEntry(declaration, context)

    final override fun visitErrorCallExpression(expression: IrErrorCallExpression, context: DartAstTransformContext): N =
        context.run { visitErrorCallExpression(expression, context) }

    open fun DartAstTransformContext.visitErrorCallExpression(
        expression: IrErrorCallExpression,
        context: DartAstTransformContext
    ): N = super.visitErrorCallExpression(expression, context)

    final override fun visitErrorDeclaration(declaration: IrErrorDeclaration, context: DartAstTransformContext): N =
        context.run { visitErrorDeclaration(declaration, context) }

    open fun DartAstTransformContext.visitErrorDeclaration(
        declaration: IrErrorDeclaration,
        context: DartAstTransformContext
    ): N =
        super.visitErrorDeclaration(declaration, context)

    final override fun visitErrorExpression(expression: IrErrorExpression, context: DartAstTransformContext): N =
        context.run { visitErrorExpression(expression, context) }

    open fun DartAstTransformContext.visitErrorExpression(
        expression: IrErrorExpression,
        context: DartAstTransformContext
    ): N = super.visitErrorExpression(expression, context)

    final override fun visitExpressionBody(body: IrExpressionBody, context: DartAstTransformContext): N =
        context.run { visitExpressionBody(body, context) }

    open fun DartAstTransformContext.visitExpressionBody(body: IrExpressionBody, context: DartAstTransformContext): N =
        super.visitExpressionBody(body, context)

    final override fun visitExternalPackageFragment(
        declaration: IrExternalPackageFragment,
        context: DartAstTransformContext
    ): N = context.run { visitExternalPackageFragment(declaration, context) }

    open fun DartAstTransformContext.visitExternalPackageFragment(
        declaration: IrExternalPackageFragment,
        context: DartAstTransformContext
    ): N = super.visitExternalPackageFragment(declaration, context)

    final override fun visitField(declaration: IrField, context: DartAstTransformContext): N =
        context.run { visitField(declaration, context) }

    open fun DartAstTransformContext.visitField(declaration: IrField, context: DartAstTransformContext): N =
        super.visitField(declaration, context)

    final override fun visitFieldAccess(expression: IrFieldAccessExpression, context: DartAstTransformContext): N =
        context.run { visitFieldAccess(expression, context) }

    open fun DartAstTransformContext.visitFieldAccess(
        expression: IrFieldAccessExpression,
        context: DartAstTransformContext
    ): N = super.visitFieldAccess(expression, context)

    final override fun visitFile(declaration: IrFile, context: DartAstTransformContext): N =
        context.run { visitFile(declaration, context) }

    open fun DartAstTransformContext.visitFile(declaration: IrFile, context: DartAstTransformContext): N =
        super.visitFile(declaration, context)

    final override fun visitFunction(declaration: IrFunction, context: DartAstTransformContext): N =
        context.run { visitFunction(declaration, context) }

    open fun DartAstTransformContext.visitFunction(declaration: IrFunction, context: DartAstTransformContext): N =
        super.visitFunction(declaration, context)

    final override fun visitFunctionAccess(expression: IrFunctionAccessExpression, context: DartAstTransformContext): N =
        context.run { visitFunctionAccess(expression, context) }

    open fun DartAstTransformContext.visitFunctionAccess(
        expression: IrFunctionAccessExpression,
        context: DartAstTransformContext
    ): N = super.visitFunctionAccess(expression, context)

    final override fun visitFunctionExpression(expression: IrFunctionExpression, context: DartAstTransformContext): N =
        context.run { visitFunctionExpression(expression, context) }

    open fun DartAstTransformContext.visitFunctionExpression(
        expression: IrFunctionExpression,
        context: DartAstTransformContext
    ): N =
        super.visitFunctionExpression(expression, context)

    final override fun visitFunctionReference(expression: IrFunctionReference, context: DartAstTransformContext): N =
        context.run { visitFunctionReference(expression, context) }

    open fun DartAstTransformContext.visitFunctionReference(
        expression: IrFunctionReference,
        context: DartAstTransformContext
    ): N = super.visitFunctionReference(expression, context)

    final override fun visitGetClass(expression: IrGetClass, context: DartAstTransformContext): N =
        context.run { visitGetClass(expression, context) }

    open fun DartAstTransformContext.visitGetClass(expression: IrGetClass, context: DartAstTransformContext): N =
        super.visitGetClass(expression, context)

    final override fun visitGetEnumValue(expression: IrGetEnumValue, context: DartAstTransformContext): N =
        context.run { visitGetEnumValue(expression, context) }

    open fun DartAstTransformContext.visitGetEnumValue(expression: IrGetEnumValue, context: DartAstTransformContext): N =
        super.visitGetEnumValue(expression, context)

    final override fun visitGetField(expression: IrGetField, context: DartAstTransformContext): N =
        context.run { visitGetField(expression, context) }

    open fun DartAstTransformContext.visitGetField(expression: IrGetField, context: DartAstTransformContext): N =
        super.visitGetField(expression, context)

    final override fun visitGetObjectValue(expression: IrGetObjectValue, context: DartAstTransformContext): N =
        context.run { visitGetObjectValue(expression, context) }

    open fun DartAstTransformContext.visitGetObjectValue(expression: IrGetObjectValue, context: DartAstTransformContext): N =
        super.visitGetObjectValue(expression, context)

    final override fun visitGetValue(expression: IrGetValue, context: DartAstTransformContext): N =
        context.run { visitGetValue(expression, context) }

    open fun DartAstTransformContext.visitGetValue(expression: IrGetValue, context: DartAstTransformContext): N =
        super.visitGetValue(expression, context)

    final override fun visitInstanceInitializerCall(
        expression: IrInstanceInitializerCall,
        context: DartAstTransformContext
    ): N = context.run { visitInstanceInitializerCall(expression, context) }

    open fun DartAstTransformContext.visitInstanceInitializerCall(
        expression: IrInstanceInitializerCall,
        context: DartAstTransformContext
    ): N = super.visitInstanceInitializerCall(expression, context)

    final override fun visitLocalDelegatedProperty(
        declaration: IrLocalDelegatedProperty,
        context: DartAstTransformContext
    ): N = context.run { visitLocalDelegatedProperty(declaration, context) }

    open fun DartAstTransformContext.visitLocalDelegatedProperty(
        declaration: IrLocalDelegatedProperty,
        context: DartAstTransformContext
    ): N = super.visitLocalDelegatedProperty(declaration, context)

    final override fun visitLocalDelegatedPropertyReference(
        expression: IrLocalDelegatedPropertyReference,
        context: DartAstTransformContext
    ): N = context.run { visitLocalDelegatedPropertyReference(expression, context) }

    open fun DartAstTransformContext.visitLocalDelegatedPropertyReference(
        expression: IrLocalDelegatedPropertyReference,
        context: DartAstTransformContext
    ): N = super.visitLocalDelegatedPropertyReference(expression, context)

    final override fun visitLoop(loop: IrLoop, context: DartAstTransformContext): N =
        context.run { visitLoop(loop, context) }

    open fun DartAstTransformContext.visitLoop(loop: IrLoop, context: DartAstTransformContext): N =
        super.visitLoop(loop, context)

    final override fun visitMemberAccess(expression: IrMemberAccessExpression<*>, context: DartAstTransformContext): N =
        context.run { visitMemberAccess(expression, context) }

    open fun DartAstTransformContext.visitMemberAccess(
        expression: IrMemberAccessExpression<*>,
        context: DartAstTransformContext
    ): N = super.visitMemberAccess(expression, context)

    final override fun visitModuleFragment(declaration: IrModuleFragment, context: DartAstTransformContext): N =
        context.run { visitModuleFragment(declaration, context) }

    open fun DartAstTransformContext.visitModuleFragment(declaration: IrModuleFragment, context: DartAstTransformContext): N =
        super.visitModuleFragment(declaration, context)

    final override fun visitPackageFragment(declaration: IrPackageFragment, context: DartAstTransformContext): N =
        context.run { visitPackageFragment(declaration, context) }

    open fun DartAstTransformContext.visitPackageFragment(
        declaration: IrPackageFragment,
        context: DartAstTransformContext
    ): N = super.visitPackageFragment(declaration, context)

    final override fun visitProperty(declaration: IrProperty, context: DartAstTransformContext): N =
        context.run { visitProperty(declaration, context) }

    open fun DartAstTransformContext.visitProperty(declaration: IrProperty, context: DartAstTransformContext): N =
        super.visitProperty(declaration, context)

    final override fun visitPropertyReference(expression: IrPropertyReference, context: DartAstTransformContext): N =
        context.run { visitPropertyReference(expression, context) }

    open fun DartAstTransformContext.visitPropertyReference(
        expression: IrPropertyReference,
        context: DartAstTransformContext
    ): N = super.visitPropertyReference(expression, context)

    final override fun visitRawFunctionReference(expression: IrRawFunctionReference, context: DartAstTransformContext): N =
        context.run { visitRawFunctionReference(expression, context) }

    open fun DartAstTransformContext.visitRawFunctionReference(
        expression: IrRawFunctionReference,
        context: DartAstTransformContext
    ): N = super.visitRawFunctionReference(expression, context)

    final override fun visitReturn(expression: IrReturn, context: DartAstTransformContext): N =
        context.run { visitReturn(expression, context) }

    open fun DartAstTransformContext.visitReturn(expression: IrReturn, context: DartAstTransformContext): N =
        super.visitReturn(expression, context)

    final override fun visitScript(declaration: IrScript, context: DartAstTransformContext): N =
        context.run { visitScript(declaration, context) }

    open fun DartAstTransformContext.visitScript(declaration: IrScript, context: DartAstTransformContext): N =
        super.visitScript(declaration, context)

    final override fun visitSetField(expression: IrSetField, context: DartAstTransformContext): N =
        context.run { visitSetField(expression, context) }

    open fun DartAstTransformContext.visitSetField(expression: IrSetField, context: DartAstTransformContext): N =
        super.visitSetField(expression, context)

    final override fun visitSetValue(expression: IrSetValue, context: DartAstTransformContext): N =
        context.run { visitSetValue(expression, context) }

    open fun DartAstTransformContext.visitSetValue(expression: IrSetValue, context: DartAstTransformContext): N =
        super.visitSetValue(expression, context)

    final override fun visitSimpleFunction(declaration: IrSimpleFunction, context: DartAstTransformContext): N =
        context.run { visitSimpleFunction(declaration, context) }

    open fun DartAstTransformContext.visitSimpleFunction(declaration: IrSimpleFunction, context: DartAstTransformContext): N =
        super.visitSimpleFunction(declaration, context)

    final override fun visitSingletonReference(expression: IrGetSingletonValue, context: DartAstTransformContext): N =
        context.run { visitSingletonReference(expression, context) }

    open fun DartAstTransformContext.visitSingletonReference(
        expression: IrGetSingletonValue,
        context: DartAstTransformContext
    ): N = super.visitSingletonReference(expression, context)

    final override fun visitSpreadElement(spread: IrSpreadElement, context: DartAstTransformContext): N =
        context.run { visitSpreadElement(spread, context) }

    open fun DartAstTransformContext.visitSpreadElement(spread: IrSpreadElement, context: DartAstTransformContext): N =
        super.visitSpreadElement(spread, context)

    final override fun visitStringConcatenation(expression: IrStringConcatenation, context: DartAstTransformContext): N =
        context.run { visitStringConcatenation(expression, context) }

    open fun DartAstTransformContext.visitStringConcatenation(
        expression: IrStringConcatenation,
        context: DartAstTransformContext
    ): N = super.visitStringConcatenation(expression, context)

    final override fun visitSuspendableExpression(
        expression: IrSuspendableExpression,
        context: DartAstTransformContext
    ): N = context.run { visitSuspendableExpression(expression, context) }

    open fun DartAstTransformContext.visitSuspendableExpression(
        expression: IrSuspendableExpression,
        context: DartAstTransformContext
    ): N = super.visitSuspendableExpression(expression, context)

    final override fun visitSuspensionPoint(expression: IrSuspensionPoint, context: DartAstTransformContext): N =
        context.run { visitSuspensionPoint(expression, context) }

    open fun DartAstTransformContext.visitSuspensionPoint(
        expression: IrSuspensionPoint,
        context: DartAstTransformContext
    ): N = super.visitSuspensionPoint(expression, context)

    final override fun visitSyntheticBody(body: IrSyntheticBody, context: DartAstTransformContext): N =
        context.run { visitSyntheticBody(body, context) }

    open fun DartAstTransformContext.visitSyntheticBody(body: IrSyntheticBody, context: DartAstTransformContext): N =
        super.visitSyntheticBody(body, context)

    final override fun visitThrow(expression: IrThrow, context: DartAstTransformContext): N =
        context.run { visitThrow(expression, context) }

    open fun DartAstTransformContext.visitThrow(expression: IrThrow, context: DartAstTransformContext): N =
        super.visitThrow(expression, context)

    final override fun visitTry(aTry: IrTry, context: DartAstTransformContext): N = context.run { visitTry(aTry, context) }

    open fun DartAstTransformContext.visitTry(aTry: IrTry, context: DartAstTransformContext): N =
        super.visitTry(aTry, context)

    final override fun visitTypeAlias(declaration: IrTypeAlias, context: DartAstTransformContext): N =
        context.run { visitTypeAlias(declaration, context) }

    open fun DartAstTransformContext.visitTypeAlias(declaration: IrTypeAlias, context: DartAstTransformContext): N =
        super.visitTypeAlias(declaration, context)

    final override fun visitTypeOperator(expression: IrTypeOperatorCall, context: DartAstTransformContext): N =
        context.run { visitTypeOperator(expression, context) }

    open fun DartAstTransformContext.visitTypeOperator(expression: IrTypeOperatorCall, context: DartAstTransformContext): N =
        super.visitTypeOperator(expression, context)

    final override fun visitTypeParameter(declaration: IrTypeParameter, context: DartAstTransformContext): N =
        context.run { visitTypeParameter(declaration, context) }

    open fun DartAstTransformContext.visitTypeParameter(declaration: IrTypeParameter, context: DartAstTransformContext): N =
        super.visitTypeParameter(declaration, context)

    final override fun visitValueAccess(expression: IrValueAccessExpression, context: DartAstTransformContext): N =
        context.run { visitValueAccess(expression, context) }

    open fun DartAstTransformContext.visitValueAccess(
        expression: IrValueAccessExpression,
        context: DartAstTransformContext
    ): N = super.visitValueAccess(expression, context)

    final override fun visitValueParameter(declaration: IrValueParameter, context: DartAstTransformContext): N =
        context.run { visitValueParameter(declaration, context) }

    open fun DartAstTransformContext.visitValueParameter(declaration: IrValueParameter, context: DartAstTransformContext): N =
        super.visitValueParameter(declaration, context)

    final override fun visitVararg(expression: IrVararg, context: DartAstTransformContext): N =
        context.run { visitVararg(expression, context) }

    open fun DartAstTransformContext.visitVararg(expression: IrVararg, context: DartAstTransformContext): N =
        super.visitVararg(expression, context)

    final override fun visitVariable(declaration: IrVariable, context: DartAstTransformContext): N =
        context.run { visitVariable(declaration, context) }

    open fun DartAstTransformContext.visitVariable(declaration: IrVariable, context: DartAstTransformContext): N =
        super.visitVariable(declaration, context)

    final override fun visitWhen(expression: IrWhen, context: DartAstTransformContext): N =
        context.run { visitWhen(expression, context) }

    open fun DartAstTransformContext.visitWhen(expression: IrWhen, context: DartAstTransformContext): N =
        super.visitWhen(expression, context)

    final override fun visitWhileLoop(loop: IrWhileLoop, context: DartAstTransformContext): N =
        context.run { visitWhileLoop(loop, context) }

    open fun DartAstTransformContext.visitWhileLoop(loop: IrWhileLoop, context: DartAstTransformContext): N =
        super.visitWhileLoop(loop, context)

    final override fun visitExpression(expression: IrExpression, context: DartAstTransformContext): N =
        context.run { visitExpression(expression, context) }

    open fun DartAstTransformContext.visitExpression(expression: IrExpression, context: DartAstTransformContext): N =
        super.visitExpression(expression, context)

    final override fun visitBody(body: IrBody, context: DartAstTransformContext): N =
        context.run { visitBody(body, context) }

    open fun DartAstTransformContext.visitBody(body: IrBody, context: DartAstTransformContext): N =
        super.visitBody(body, context)
}