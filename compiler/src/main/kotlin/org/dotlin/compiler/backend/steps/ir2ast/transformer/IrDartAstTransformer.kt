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

import org.dotlin.compiler.backend.runAndReportTransformerError
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
    ): N = context.run { runAndReportTransformerError(declaration) { visitAnonymousInitializer(it, context) } }

    open fun DartAstTransformContext.visitAnonymousInitializer(
        declaration: IrAnonymousInitializer,
        context: DartAstTransformContext
    ): N = super.visitAnonymousInitializer(declaration, context)

    final override fun visitBlock(expression: IrBlock, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitBlock(it, context) } }

    open fun DartAstTransformContext.visitBlock(expression: IrBlock, context: DartAstTransformContext): N =
        super.visitBlock(expression, context)

    final override fun visitBlockBody(body: IrBlockBody, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(body) { visitBlockBody(it, context) } }

    open fun DartAstTransformContext.visitBlockBody(body: IrBlockBody, context: DartAstTransformContext): N =
        super.visitBlockBody(body, context)

    final override fun visitBranch(branch: IrBranch, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(branch) { visitBranch(it, context) } }

    open fun DartAstTransformContext.visitBranch(branch: IrBranch, context: DartAstTransformContext): N =
        super.visitBranch(branch, context)

    final override fun visitBreak(jump: IrBreak, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(jump) { visitBreak(it, context) } }

    open fun DartAstTransformContext.visitBreak(jump: IrBreak, context: DartAstTransformContext): N =
        super.visitBreak(jump, context)

    final override fun visitBreakContinue(jump: IrBreakContinue, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(jump) { visitBreakContinue(it, context) } }

    open fun DartAstTransformContext.visitBreakContinue(jump: IrBreakContinue, context: DartAstTransformContext): N =
        super.visitBreakContinue(jump, context)

    final override fun visitCall(expression: IrCall, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitCall(it, context) } }

    open fun DartAstTransformContext.visitCall(expression: IrCall, context: DartAstTransformContext): N =
        super.visitCall(expression, context)

    final override fun visitCallableReference(expression: IrCallableReference<*>, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitCallableReference(it, context) } }

    open fun DartAstTransformContext.visitCallableReference(
        expression: IrCallableReference<*>,
        context: DartAstTransformContext
    ): N = super.visitCallableReference(expression, context)

    final override fun visitCatch(aCatch: IrCatch, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(aCatch) { visitCatch(it, context) } }

    open fun DartAstTransformContext.visitCatch(aCatch: IrCatch, context: DartAstTransformContext): N =
        super.visitCatch(aCatch, context)

    final override fun visitClass(declaration: IrClass, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitClass(it, context) } }

    open fun DartAstTransformContext.visitClass(declaration: IrClass, context: DartAstTransformContext): N =
        super.visitClass(declaration, context)

    final override fun visitClassReference(expression: IrClassReference, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitClassReference(it, context) } }

    open fun DartAstTransformContext.visitClassReference(expression: IrClassReference, context: DartAstTransformContext): N =
        super.visitClassReference(expression, context)

    final override fun visitComposite(expression: IrComposite, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitComposite(it, context) } }

    open fun DartAstTransformContext.visitComposite(expression: IrComposite, context: DartAstTransformContext): N =
        super.visitComposite(expression, context)

    final override fun visitConst(expression: IrConst<*>, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitConst(it, context) } }

    open fun DartAstTransformContext.visitConst(expression: IrConst<*>, context: DartAstTransformContext): N =
        super.visitConst(expression, context)

    final override fun visitConstructor(declaration: IrConstructor, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitConstructor(it, context) } }

    open fun DartAstTransformContext.visitConstructor(declaration: IrConstructor, context: DartAstTransformContext): N =
        super.visitConstructor(declaration, context)

    final override fun visitConstructorCall(expression: IrConstructorCall, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitConstructorCall(it, context) } }

    open fun DartAstTransformContext.visitConstructorCall(
        expression: IrConstructorCall,
        context: DartAstTransformContext
    ): N = super.visitConstructorCall(expression, context)

    final override fun visitContainerExpression(expression: IrContainerExpression, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitContainerExpression(it, context) } }

    open fun DartAstTransformContext.visitContainerExpression(
        expression: IrContainerExpression,
        context: DartAstTransformContext
    ): N = super.visitContainerExpression(expression, context)

    final override fun visitContinue(jump: IrContinue, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(jump) { visitContinue(it, context) } }

    open fun DartAstTransformContext.visitContinue(jump: IrContinue, context: DartAstTransformContext): N =
        super.visitContinue(jump, context)

    final override fun visitDeclaration(declaration: IrDeclarationBase, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitDeclaration(it, context) } }

    open fun DartAstTransformContext.visitDeclaration(declaration: IrDeclarationBase, context: DartAstTransformContext): N =
        super.visitDeclaration(declaration, context)

    final override fun visitDeclarationReference(expression: IrDeclarationReference, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitDeclarationReference(it, context) } }

    open fun DartAstTransformContext.visitDeclarationReference(
        expression: IrDeclarationReference,
        context: DartAstTransformContext
    ): N = super.visitDeclarationReference(expression, context)

    final override fun visitDelegatingConstructorCall(
        expression: IrDelegatingConstructorCall,
        context: DartAstTransformContext
    ): N = context.run { runAndReportTransformerError(expression) { visitDelegatingConstructorCall(it, context) } }

    open fun DartAstTransformContext.visitDelegatingConstructorCall(
        expression: IrDelegatingConstructorCall,
        context: DartAstTransformContext
    ): N = super.visitDelegatingConstructorCall(expression, context)

    final override fun visitDoWhileLoop(loop: IrDoWhileLoop, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(loop) { visitDoWhileLoop(it, context) } }

    open fun DartAstTransformContext.visitDoWhileLoop(loop: IrDoWhileLoop, context: DartAstTransformContext): N =
        super.visitDoWhileLoop(loop, context)

    final override fun visitDynamicExpression(expression: IrDynamicExpression, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitDynamicExpression(it, context) } }

    open fun DartAstTransformContext.visitDynamicExpression(
        expression: IrDynamicExpression,
        context: DartAstTransformContext
    ): N = super.visitDynamicExpression(expression, context)

    final override fun visitDynamicMemberExpression(
        expression: IrDynamicMemberExpression,
        context: DartAstTransformContext
    ): N = context.run { runAndReportTransformerError(expression) { visitDynamicMemberExpression(it, context) } }

    open fun DartAstTransformContext.visitDynamicMemberExpression(
        expression: IrDynamicMemberExpression,
        context: DartAstTransformContext
    ): N = super.visitDynamicMemberExpression(expression, context)

    final override fun visitDynamicOperatorExpression(
        expression: IrDynamicOperatorExpression,
        context: DartAstTransformContext
    ): N = context.run { runAndReportTransformerError(expression) { visitDynamicOperatorExpression(it, context) } }

    open fun DartAstTransformContext.visitDynamicOperatorExpression(
        expression: IrDynamicOperatorExpression,
        context: DartAstTransformContext
    ): N = super.visitDynamicOperatorExpression(expression, context)

    final override fun visitElseBranch(branch: IrElseBranch, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(branch) { visitElseBranch(it, context) } }

    open fun DartAstTransformContext.visitElseBranch(branch: IrElseBranch, context: DartAstTransformContext): N =
        super.visitElseBranch(branch, context)

    final override fun visitEnumConstructorCall(expression: IrEnumConstructorCall, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitEnumConstructorCall(it, context) } }

    open fun DartAstTransformContext.visitEnumConstructorCall(
        expression: IrEnumConstructorCall,
        context: DartAstTransformContext
    ): N = super.visitEnumConstructorCall(expression, context)

    final override fun visitEnumEntry(declaration: IrEnumEntry, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitEnumEntry(it, context) } }

    open fun DartAstTransformContext.visitEnumEntry(declaration: IrEnumEntry, context: DartAstTransformContext): N =
        super.visitEnumEntry(declaration, context)

    final override fun visitErrorCallExpression(expression: IrErrorCallExpression, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitErrorCallExpression(it, context) } }

    open fun DartAstTransformContext.visitErrorCallExpression(
        expression: IrErrorCallExpression,
        context: DartAstTransformContext
    ): N = super.visitErrorCallExpression(expression, context)

    final override fun visitErrorDeclaration(declaration: IrErrorDeclaration, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitErrorDeclaration(it, context) } }

    open fun DartAstTransformContext.visitErrorDeclaration(
        declaration: IrErrorDeclaration,
        context: DartAstTransformContext
    ): N =
        super.visitErrorDeclaration(declaration, context)

    final override fun visitErrorExpression(expression: IrErrorExpression, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitErrorExpression(it, context) } }

    open fun DartAstTransformContext.visitErrorExpression(
        expression: IrErrorExpression,
        context: DartAstTransformContext
    ): N = super.visitErrorExpression(expression, context)

    final override fun visitExpressionBody(body: IrExpressionBody, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(body) { visitExpressionBody(it, context) } }

    open fun DartAstTransformContext.visitExpressionBody(body: IrExpressionBody, context: DartAstTransformContext): N =
        super.visitExpressionBody(body, context)

    final override fun visitExternalPackageFragment(
        declaration: IrExternalPackageFragment,
        context: DartAstTransformContext
    ): N = context.run { runAndReportTransformerError(declaration) { visitExternalPackageFragment(it, context) } }

    open fun DartAstTransformContext.visitExternalPackageFragment(
        declaration: IrExternalPackageFragment,
        context: DartAstTransformContext
    ): N = super.visitExternalPackageFragment(declaration, context)

    final override fun visitField(declaration: IrField, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitField(it, context) } }

    open fun DartAstTransformContext.visitField(declaration: IrField, context: DartAstTransformContext): N =
        super.visitField(declaration, context)

    final override fun visitFieldAccess(expression: IrFieldAccessExpression, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitFieldAccess(it, context) } }

    open fun DartAstTransformContext.visitFieldAccess(
        expression: IrFieldAccessExpression,
        context: DartAstTransformContext
    ): N = super.visitFieldAccess(expression, context)

    final override fun visitFile(declaration: IrFile, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitFile(it, context) } }

    open fun DartAstTransformContext.visitFile(declaration: IrFile, context: DartAstTransformContext): N =
        super.visitFile(declaration, context)

    final override fun visitFunction(declaration: IrFunction, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitFunction(it, context) } }

    open fun DartAstTransformContext.visitFunction(declaration: IrFunction, context: DartAstTransformContext): N =
        super.visitFunction(declaration, context)

    final override fun visitFunctionAccess(expression: IrFunctionAccessExpression, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitFunctionAccess(it, context) } }

    open fun DartAstTransformContext.visitFunctionAccess(
        expression: IrFunctionAccessExpression,
        context: DartAstTransformContext
    ): N = super.visitFunctionAccess(expression, context)

    final override fun visitFunctionExpression(expression: IrFunctionExpression, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitFunctionExpression(it, context) } }

    open fun DartAstTransformContext.visitFunctionExpression(
        expression: IrFunctionExpression,
        context: DartAstTransformContext
    ): N =
        super.visitFunctionExpression(expression, context)

    final override fun visitFunctionReference(expression: IrFunctionReference, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitFunctionReference(it, context) } }

    open fun DartAstTransformContext.visitFunctionReference(
        expression: IrFunctionReference,
        context: DartAstTransformContext
    ): N = super.visitFunctionReference(expression, context)

    final override fun visitGetClass(expression: IrGetClass, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitGetClass(it, context) } }

    open fun DartAstTransformContext.visitGetClass(expression: IrGetClass, context: DartAstTransformContext): N =
        super.visitGetClass(expression, context)

    final override fun visitGetEnumValue(expression: IrGetEnumValue, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitGetEnumValue(it, context) } }

    open fun DartAstTransformContext.visitGetEnumValue(expression: IrGetEnumValue, context: DartAstTransformContext): N =
        super.visitGetEnumValue(expression, context)

    final override fun visitGetField(expression: IrGetField, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitGetField(it, context) } }

    open fun DartAstTransformContext.visitGetField(expression: IrGetField, context: DartAstTransformContext): N =
        super.visitGetField(expression, context)

    final override fun visitGetObjectValue(expression: IrGetObjectValue, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitGetObjectValue(it, context) } }

    open fun DartAstTransformContext.visitGetObjectValue(expression: IrGetObjectValue, context: DartAstTransformContext): N =
        super.visitGetObjectValue(expression, context)

    final override fun visitGetValue(expression: IrGetValue, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitGetValue(it, context) } }

    open fun DartAstTransformContext.visitGetValue(expression: IrGetValue, context: DartAstTransformContext): N =
        super.visitGetValue(expression, context)

    final override fun visitInstanceInitializerCall(
        expression: IrInstanceInitializerCall,
        context: DartAstTransformContext
    ): N = context.run { runAndReportTransformerError(expression) { visitInstanceInitializerCall(it, context) } }

    open fun DartAstTransformContext.visitInstanceInitializerCall(
        expression: IrInstanceInitializerCall,
        context: DartAstTransformContext
    ): N = super.visitInstanceInitializerCall(expression, context)

    final override fun visitLocalDelegatedProperty(
        declaration: IrLocalDelegatedProperty,
        context: DartAstTransformContext
    ): N = context.run { runAndReportTransformerError(declaration) { visitLocalDelegatedProperty(it, context) } }

    open fun DartAstTransformContext.visitLocalDelegatedProperty(
        declaration: IrLocalDelegatedProperty,
        context: DartAstTransformContext
    ): N = super.visitLocalDelegatedProperty(declaration, context)

    final override fun visitLocalDelegatedPropertyReference(
        expression: IrLocalDelegatedPropertyReference,
        context: DartAstTransformContext
    ): N = context.run { runAndReportTransformerError(expression) { visitLocalDelegatedPropertyReference(it, context) } }

    open fun DartAstTransformContext.visitLocalDelegatedPropertyReference(
        expression: IrLocalDelegatedPropertyReference,
        context: DartAstTransformContext
    ): N = super.visitLocalDelegatedPropertyReference(expression, context)

    final override fun visitLoop(loop: IrLoop, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(loop) { visitLoop(it, context) } }

    open fun DartAstTransformContext.visitLoop(loop: IrLoop, context: DartAstTransformContext): N =
        super.visitLoop(loop, context)

    final override fun visitMemberAccess(expression: IrMemberAccessExpression<*>, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitMemberAccess(it, context) } }

    open fun DartAstTransformContext.visitMemberAccess(
        expression: IrMemberAccessExpression<*>,
        context: DartAstTransformContext
    ): N = super.visitMemberAccess(expression, context)

    final override fun visitModuleFragment(declaration: IrModuleFragment, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitModuleFragment(it, context) } }

    open fun DartAstTransformContext.visitModuleFragment(declaration: IrModuleFragment, context: DartAstTransformContext): N =
        super.visitModuleFragment(declaration, context)

    final override fun visitPackageFragment(declaration: IrPackageFragment, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitPackageFragment(it, context) } }

    open fun DartAstTransformContext.visitPackageFragment(
        declaration: IrPackageFragment,
        context: DartAstTransformContext
    ): N = super.visitPackageFragment(declaration, context)

    final override fun visitProperty(declaration: IrProperty, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitProperty(it, context) } }

    open fun DartAstTransformContext.visitProperty(declaration: IrProperty, context: DartAstTransformContext): N =
        super.visitProperty(declaration, context)

    final override fun visitPropertyReference(expression: IrPropertyReference, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitPropertyReference(it, context) } }

    open fun DartAstTransformContext.visitPropertyReference(
        expression: IrPropertyReference,
        context: DartAstTransformContext
    ): N = super.visitPropertyReference(expression, context)

    final override fun visitRawFunctionReference(expression: IrRawFunctionReference, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitRawFunctionReference(it, context) } }

    open fun DartAstTransformContext.visitRawFunctionReference(
        expression: IrRawFunctionReference,
        context: DartAstTransformContext
    ): N = super.visitRawFunctionReference(expression, context)

    final override fun visitReturn(expression: IrReturn, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitReturn(it, context) } }

    open fun DartAstTransformContext.visitReturn(expression: IrReturn, context: DartAstTransformContext): N =
        super.visitReturn(expression, context)

    final override fun visitScript(declaration: IrScript, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitScript(it, context) } }

    open fun DartAstTransformContext.visitScript(declaration: IrScript, context: DartAstTransformContext): N =
        super.visitScript(declaration, context)

    final override fun visitSetField(expression: IrSetField, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitSetField(it, context) } }

    open fun DartAstTransformContext.visitSetField(expression: IrSetField, context: DartAstTransformContext): N =
        super.visitSetField(expression, context)

    final override fun visitSetValue(expression: IrSetValue, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitSetValue(it, context) } }

    open fun DartAstTransformContext.visitSetValue(expression: IrSetValue, context: DartAstTransformContext): N =
        super.visitSetValue(expression, context)

    final override fun visitSimpleFunction(declaration: IrSimpleFunction, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitSimpleFunction(it, context) } }

    open fun DartAstTransformContext.visitSimpleFunction(declaration: IrSimpleFunction, context: DartAstTransformContext): N =
        super.visitSimpleFunction(declaration, context)

    final override fun visitSingletonReference(expression: IrGetSingletonValue, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitSingletonReference(it, context) } }

    open fun DartAstTransformContext.visitSingletonReference(
        expression: IrGetSingletonValue,
        context: DartAstTransformContext
    ): N = super.visitSingletonReference(expression, context)

    final override fun visitSpreadElement(spread: IrSpreadElement, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(spread) { visitSpreadElement(it, context) } }

    open fun DartAstTransformContext.visitSpreadElement(spread: IrSpreadElement, context: DartAstTransformContext): N =
        super.visitSpreadElement(spread, context)

    final override fun visitStringConcatenation(expression: IrStringConcatenation, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitStringConcatenation(it, context) } }

    open fun DartAstTransformContext.visitStringConcatenation(
        expression: IrStringConcatenation,
        context: DartAstTransformContext
    ): N = super.visitStringConcatenation(expression, context)

    final override fun visitSuspendableExpression(
        expression: IrSuspendableExpression,
        context: DartAstTransformContext
    ): N = context.run { runAndReportTransformerError(expression) { visitSuspendableExpression(it, context) } }

    open fun DartAstTransformContext.visitSuspendableExpression(
        expression: IrSuspendableExpression,
        context: DartAstTransformContext
    ): N = super.visitSuspendableExpression(expression, context)

    final override fun visitSuspensionPoint(expression: IrSuspensionPoint, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitSuspensionPoint(it, context) } }

    open fun DartAstTransformContext.visitSuspensionPoint(
        expression: IrSuspensionPoint,
        context: DartAstTransformContext
    ): N = super.visitSuspensionPoint(expression, context)

    final override fun visitSyntheticBody(body: IrSyntheticBody, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(body) { visitSyntheticBody(it, context) } }

    open fun DartAstTransformContext.visitSyntheticBody(body: IrSyntheticBody, context: DartAstTransformContext): N =
        super.visitSyntheticBody(body, context)

    final override fun visitThrow(expression: IrThrow, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitThrow(it, context) } }

    open fun DartAstTransformContext.visitThrow(expression: IrThrow, context: DartAstTransformContext): N =
        super.visitThrow(expression, context)

    final override fun visitTry(aTry: IrTry, context: DartAstTransformContext): N = context.run { runAndReportTransformerError(aTry) { visitTry(it, context) } }

    open fun DartAstTransformContext.visitTry(aTry: IrTry, context: DartAstTransformContext): N =
        super.visitTry(aTry, context)

    final override fun visitTypeAlias(declaration: IrTypeAlias, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitTypeAlias(it, context) } }

    open fun DartAstTransformContext.visitTypeAlias(declaration: IrTypeAlias, context: DartAstTransformContext): N =
        super.visitTypeAlias(declaration, context)

    final override fun visitTypeOperator(expression: IrTypeOperatorCall, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitTypeOperator(it, context) } }

    open fun DartAstTransformContext.visitTypeOperator(expression: IrTypeOperatorCall, context: DartAstTransformContext): N =
        super.visitTypeOperator(expression, context)

    final override fun visitTypeParameter(declaration: IrTypeParameter, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitTypeParameter(it, context) } }

    open fun DartAstTransformContext.visitTypeParameter(declaration: IrTypeParameter, context: DartAstTransformContext): N =
        super.visitTypeParameter(declaration, context)

    final override fun visitValueAccess(expression: IrValueAccessExpression, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitValueAccess(it, context) } }

    open fun DartAstTransformContext.visitValueAccess(
        expression: IrValueAccessExpression,
        context: DartAstTransformContext
    ): N = super.visitValueAccess(expression, context)

    final override fun visitValueParameter(declaration: IrValueParameter, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitValueParameter(it, context) } }

    open fun DartAstTransformContext.visitValueParameter(declaration: IrValueParameter, context: DartAstTransformContext): N =
        super.visitValueParameter(declaration, context)

    final override fun visitVararg(expression: IrVararg, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitVararg(it, context) } }

    open fun DartAstTransformContext.visitVararg(expression: IrVararg, context: DartAstTransformContext): N =
        super.visitVararg(expression, context)

    final override fun visitVariable(declaration: IrVariable, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(declaration) { visitVariable(it, context) } }

    open fun DartAstTransformContext.visitVariable(declaration: IrVariable, context: DartAstTransformContext): N =
        super.visitVariable(declaration, context)

    final override fun visitWhen(expression: IrWhen, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitWhen(it, context) } }

    open fun DartAstTransformContext.visitWhen(expression: IrWhen, context: DartAstTransformContext): N =
        super.visitWhen(expression, context)

    final override fun visitWhileLoop(loop: IrWhileLoop, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(loop) { visitWhileLoop(it, context) } }

    open fun DartAstTransformContext.visitWhileLoop(loop: IrWhileLoop, context: DartAstTransformContext): N =
        super.visitWhileLoop(loop, context)

    final override fun visitExpression(expression: IrExpression, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(expression) { visitExpression(it, context) } }

    open fun DartAstTransformContext.visitExpression(expression: IrExpression, context: DartAstTransformContext): N =
        super.visitExpression(expression, context)

    final override fun visitBody(body: IrBody, context: DartAstTransformContext): N =
        context.run { runAndReportTransformerError(body) { visitBody(it, context) } }

    open fun DartAstTransformContext.visitBody(body: IrBody, context: DartAstTransformContext): N =
        super.visitBody(body, context)
}