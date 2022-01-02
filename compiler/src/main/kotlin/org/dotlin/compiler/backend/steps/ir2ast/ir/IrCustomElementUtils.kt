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

package org.dotlin.compiler.backend.steps.ir2ast.ir

import org.dotlin.compiler.backend.steps.ir2ast.ir.element.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

/**
 * Call [visitExpression] and [visitBody] in your own transformer.
 */
interface IrCustomElementVisitorHelper<out R, in D> {
    fun visitDartCodeExpression(expression: IrDartCodeExpression, data: D): R
    fun visitAnnotatedExpression(expression: IrAnnotatedExpression, data: D): R
    fun visitNullAwareExpression(expression: IrNullAwareExpression, data: D): R
    fun visitIfNullExpression(expression: IrIfNullExpression, data: D): R
    fun visitConjunctionExpression(expression: IrConjunctionExpression, data: D): R
    fun visitDisjunctionExpression(expression: IrDisjunctionExpression, data: D): R
    fun visitExpressionBodyWithOrigin(body: IrExpressionBodyWithOrigin, data: D): R
}

fun <R, D> IrCustomElementVisitorHelper<R, D>.visitCustomExpression(
    expression: IrExpression,
    data: D,
    fallback: () -> R
) = when (expression) {
    is IrDartCodeExpression -> visitDartCodeExpression(expression, data)
    is IrAnnotatedExpression -> visitAnnotatedExpression(expression, data)
    is IrNullAwareExpression -> visitNullAwareExpression(expression, data)
    is IrIfNullExpression -> visitIfNullExpression(expression, data)
    is IrConjunctionExpression -> visitConjunctionExpression(expression, data)
    is IrDisjunctionExpression -> visitDisjunctionExpression(expression, data)
    else -> fallback()
}

fun <R, D> IrCustomElementVisitorHelper<R, D>.visitCustomBody(
    body: IrBody,
    data: D,
    fallback: () -> R
) = when (body) {
    is IrExpressionBodyWithOrigin -> visitExpressionBodyWithOrigin(body, data)
    else -> fallback()
}

interface IrCustomElementVisitor<out R, in D> : IrElementVisitor<R, D>, IrCustomElementVisitorHelper<R, D> {
    override fun visitExpression(expression: IrExpression, data: D) =
        visitCustomExpression(expression, data, fallback = { super.visitExpression(expression, data) })

    override fun visitBody(body: IrBody, data: D) =
        visitCustomBody(body, data, fallback = { super.visitBody(body, data) })

    override fun visitDartCodeExpression(expression: IrDartCodeExpression, data: D) =
        super.visitExpression(expression, data)

    override fun visitAnnotatedExpression(expression: IrAnnotatedExpression, data: D) =
        super.visitExpression(expression, data)

    override fun visitNullAwareExpression(expression: IrNullAwareExpression, data: D) =
        super.visitExpression(expression, data)

    override fun visitExpressionBodyWithOrigin(body: IrExpressionBodyWithOrigin, data: D) = super.visitBody(body, data)

    fun visitBinaryInfixExpression(expression: IrBinaryInfixExpression, data: D) =
        super.visitExpression(expression, data)

    override fun visitIfNullExpression(expression: IrIfNullExpression, data: D) =
        visitBinaryInfixExpression(expression, data)

    override fun visitConjunctionExpression(expression: IrConjunctionExpression, data: D) =
        visitBinaryInfixExpression(expression, data)

    override fun visitDisjunctionExpression(expression: IrDisjunctionExpression, data: D) =
        visitBinaryInfixExpression(expression, data)
}

/**
 * Call [visitExpression] and [visitBody] in your own transformer.
 */
interface IrCustomElementTransformerHelper<in D> : IrElementTransformer<D>, IrCustomElementVisitorHelper<IrElement, D> {
    override fun visitDartCodeExpression(expression: IrDartCodeExpression, data: D) =
        super.visitExpression(expression, data)

    override fun visitAnnotatedExpression(expression: IrAnnotatedExpression, data: D) =
        super.visitExpression(expression, data)

    override fun visitNullAwareExpression(expression: IrNullAwareExpression, data: D) =
        super.visitExpression(expression, data)

    override fun visitIfNullExpression(expression: IrIfNullExpression, data: D) =
        super.visitExpression(expression, data)

    override fun visitConjunctionExpression(expression: IrConjunctionExpression, data: D) =
        super.visitExpression(expression, data)

    override fun visitDisjunctionExpression(expression: IrDisjunctionExpression, data: D) =
        super.visitExpression(expression, data)

    override fun visitExpressionBodyWithOrigin(body: IrExpressionBodyWithOrigin, data: D) = super.visitBody(body, data)
}

interface IrCustomElementTransformerHelperVoid : IrCustomElementTransformerHelper<Nothing?> {
    fun visitDartCodeExpression(expression: IrDartCodeExpression): IrExpression =
        super.visitDartCodeExpression(expression, null)

    override fun visitAnnotatedExpression(expression: IrAnnotatedExpression, data: Nothing?) =
        visitAnnotatedExpression(expression)

    fun visitAnnotatedExpression(expression: IrAnnotatedExpression): IrExpression =
        super.visitAnnotatedExpression(expression, null)

    override fun visitNullAwareExpression(expression: IrNullAwareExpression, data: Nothing?) =
        visitNullAwareExpression(expression)

    fun visitNullAwareExpression(expression: IrNullAwareExpression): IrExpression =
        super.visitNullAwareExpression(expression, null)

    fun visitIfNullExpression(expression: IrIfNullExpression): IrExpression =
        super.visitIfNullExpression(expression, null)

    override fun visitConjunctionExpression(expression: IrConjunctionExpression, data: Nothing?) =
        visitConjunctionExpression(expression)

    fun visitConjunctionExpression(expression: IrConjunctionExpression): IrExpression =
        super.visitConjunctionExpression(expression, null)

    override fun visitDisjunctionExpression(expression: IrDisjunctionExpression, data: Nothing?) =
        visitDisjunctionExpression(expression)

    fun visitDisjunctionExpression(expression: IrDisjunctionExpression): IrExpression =
        super.visitDisjunctionExpression(expression, null)

    override fun visitExpressionBodyWithOrigin(body: IrExpressionBodyWithOrigin, data: Nothing?) =
        visitExpressionBodyWithOrigin(body)

    fun visitExpressionBodyWithOrigin(body: IrExpressionBodyWithOrigin): IrBody =
        super.visitExpressionBodyWithOrigin(body, null)
}

abstract class IrCustomElementTransformerVoid : IrElementTransformerVoid(), IrCustomElementTransformerHelperVoid {
    override fun visitExpression(expression: IrExpression) =
        visitCustomExpression(
            expression,
            data = null,
            fallback = { super<IrElementTransformerVoid>.visitExpression(expression) }
        ) as IrExpression

    override fun visitBody(body: IrBody) = visitCustomBody(
        body,
        data = null,
        fallback = { super<IrElementTransformerVoid>.visitBody(body) }
    ) as IrBody
}

abstract class IrCustomElementTransformer<in D> : IrElementTransformer<D>, IrCustomElementTransformerHelper<D> {
    override fun visitExpression(expression: IrExpression, data: D) =
        visitCustomExpression(
            expression,
            data = data,
            fallback = { super<IrCustomElementTransformerHelper>.visitExpression(expression, data) }
        ) as IrExpression

    override fun visitBody(body: IrBody, data: D) = visitCustomBody(
        body,
        data = data,
        fallback = { super<IrCustomElementTransformerHelper>.visitBody(body, data) }
    ) as IrBody
}

interface IrCustomElementVisitorVoid : IrElementVisitorVoid, IrCustomElementVisitor<Unit, Nothing?> {
    override fun visitExpression(expression: IrExpression, data: Nothing?) =
        visitCustomExpression(
            expression,
            data,
            fallback = { visitElement(expression) }
        )

    override fun visitBody(body: IrBody, data: Nothing?) =
        visitCustomBody(body, data, fallback = { visitElement(body) })
}

interface IrCustomElementVisitorNothing<out R> : IrElementVisitor<R, Nothing?>,
    IrCustomElementVisitor<R, Nothing?> {
    fun visitElement(element: IrElement): R
    override fun visitElement(element: IrElement, data: Nothing?) = visitElement(element)

    fun visitModuleFragment(declaration: IrModuleFragment) = visitElement(declaration)
    override fun visitModuleFragment(declaration: IrModuleFragment, data: Nothing?) = visitModuleFragment(declaration)

    fun visitPackageFragment(declaration: IrPackageFragment) = visitElement(declaration)
    override fun visitPackageFragment(declaration: IrPackageFragment, data: Nothing?) =
        visitPackageFragment(declaration)

    fun visitExternalPackageFragment(declaration: IrExternalPackageFragment) = visitPackageFragment(declaration)
    override fun visitExternalPackageFragment(declaration: IrExternalPackageFragment, data: Nothing?) =
        visitExternalPackageFragment(declaration)

    fun visitFile(declaration: IrFile) = visitPackageFragment(declaration)
    override fun visitFile(declaration: IrFile, data: Nothing?) = visitFile(declaration)

    fun visitDeclaration(declaration: IrDeclarationBase) = visitElement(declaration)
    override fun visitDeclaration(declaration: IrDeclarationBase, data: Nothing?) = visitDeclaration(declaration)

    fun visitClass(declaration: IrClass) = visitDeclaration(declaration)
    override fun visitClass(declaration: IrClass, data: Nothing?) = visitClass(declaration)

    fun visitScript(declaration: IrScript) = visitDeclaration(declaration)
    override fun visitScript(declaration: IrScript, data: Nothing?) = visitScript(declaration)

    fun visitFunction(declaration: IrFunction) = visitDeclaration(declaration)
    override fun visitFunction(declaration: IrFunction, data: Nothing?) = visitFunction(declaration)

    fun visitSimpleFunction(declaration: IrSimpleFunction) = visitFunction(declaration)
    override fun visitSimpleFunction(declaration: IrSimpleFunction, data: Nothing?) = visitSimpleFunction(declaration)

    fun visitConstructor(declaration: IrConstructor) = visitFunction(declaration)
    override fun visitConstructor(declaration: IrConstructor, data: Nothing?) = visitConstructor(declaration)

    fun visitProperty(declaration: IrProperty) = visitDeclaration(declaration)
    override fun visitProperty(declaration: IrProperty, data: Nothing?) = visitProperty(declaration)

    fun visitField(declaration: IrField) = visitDeclaration(declaration)
    override fun visitField(declaration: IrField, data: Nothing?) = visitField(declaration)

    fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty) = visitDeclaration(declaration)
    override fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty, data: Nothing?) =
        visitLocalDelegatedProperty(declaration)

    fun visitVariable(declaration: IrVariable) = visitDeclaration(declaration)
    override fun visitVariable(declaration: IrVariable, data: Nothing?) = visitVariable(declaration)

    fun visitEnumEntry(declaration: IrEnumEntry) = visitDeclaration(declaration)
    override fun visitEnumEntry(declaration: IrEnumEntry, data: Nothing?) = visitEnumEntry(declaration)

    fun visitAnonymousInitializer(declaration: IrAnonymousInitializer) = visitDeclaration(declaration)
    override fun visitAnonymousInitializer(declaration: IrAnonymousInitializer, data: Nothing?) =
        visitAnonymousInitializer(declaration)

    fun visitTypeParameter(declaration: IrTypeParameter) = visitDeclaration(declaration)
    override fun visitTypeParameter(declaration: IrTypeParameter, data: Nothing?) = visitTypeParameter(declaration)

    fun visitValueParameter(declaration: IrValueParameter) = visitDeclaration(declaration)
    override fun visitValueParameter(declaration: IrValueParameter, data: Nothing?) = visitValueParameter(declaration)

    fun visitTypeAlias(declaration: IrTypeAlias) = visitDeclaration(declaration)
    override fun visitTypeAlias(declaration: IrTypeAlias, data: Nothing?) = visitTypeAlias(declaration)

    fun visitBody(body: IrBody) = visitCustomBody(body, data = null, fallback = { visitElement(body) })
    override fun visitBody(body: IrBody, data: Nothing?) = visitBody(body)

    fun visitExpressionBody(body: IrExpressionBody) = visitBody(body)
    override fun visitExpressionBody(body: IrExpressionBody, data: Nothing?) = visitExpressionBody(body)

    fun visitBlockBody(body: IrBlockBody) = visitBody(body)
    override fun visitBlockBody(body: IrBlockBody, data: Nothing?) = visitBlockBody(body)

    fun visitSyntheticBody(body: IrSyntheticBody) = visitBody(body)
    override fun visitSyntheticBody(body: IrSyntheticBody, data: Nothing?) = visitSyntheticBody(body)

    fun visitSuspendableExpression(expression: IrSuspendableExpression) = visitExpression(expression)
    override fun visitSuspendableExpression(expression: IrSuspendableExpression, data: Nothing?) =
        visitSuspendableExpression(expression)

    fun visitSuspensionPoint(expression: IrSuspensionPoint) = visitExpression(expression)
    override fun visitSuspensionPoint(expression: IrSuspensionPoint, data: Nothing?) = visitSuspensionPoint(expression)

    fun visitExpression(expression: IrExpression) =
        visitCustomExpression(expression, data = null, fallback = { visitElement(expression) })

    override fun visitExpression(expression: IrExpression, data: Nothing?) = visitExpression(expression)

    fun <T> visitConst(expression: IrConst<T>) = visitExpression(expression)
    override fun <T> visitConst(expression: IrConst<T>, data: Nothing?) = visitConst(expression)

    fun visitVararg(expression: IrVararg) = visitExpression(expression)
    override fun visitVararg(expression: IrVararg, data: Nothing?) = visitVararg(expression)

    fun visitSpreadElement(spread: IrSpreadElement) = visitElement(spread)
    override fun visitSpreadElement(spread: IrSpreadElement, data: Nothing?) = visitSpreadElement(spread)

    fun visitContainerExpression(expression: IrContainerExpression) = visitExpression(expression)
    override fun visitContainerExpression(expression: IrContainerExpression, data: Nothing?) =
        visitContainerExpression(expression)

    fun visitComposite(expression: IrComposite) = visitContainerExpression(expression)
    override fun visitComposite(expression: IrComposite, data: Nothing?) = visitComposite(expression)

    fun visitBlock(expression: IrBlock) = visitContainerExpression(expression)
    override fun visitBlock(expression: IrBlock, data: Nothing?) = visitBlock(expression)

    fun visitStringConcatenation(expression: IrStringConcatenation) = visitExpression(expression)
    override fun visitStringConcatenation(expression: IrStringConcatenation, data: Nothing?) =
        visitStringConcatenation(expression)

    fun visitDeclarationReference(expression: IrDeclarationReference) = visitExpression(expression)
    override fun visitDeclarationReference(expression: IrDeclarationReference, data: Nothing?) =
        visitDeclarationReference(expression)

    fun visitSingletonReference(expression: IrGetSingletonValue) = visitDeclarationReference(expression)
    override fun visitSingletonReference(expression: IrGetSingletonValue, data: Nothing?) =
        visitSingletonReference(expression)

    fun visitGetObjectValue(expression: IrGetObjectValue) = visitSingletonReference(expression)
    override fun visitGetObjectValue(expression: IrGetObjectValue, data: Nothing?) = visitGetObjectValue(expression)

    fun visitGetEnumValue(expression: IrGetEnumValue) = visitSingletonReference(expression)
    override fun visitGetEnumValue(expression: IrGetEnumValue, data: Nothing?) = visitGetEnumValue(expression)

    fun visitVariableAccess(expression: IrValueAccessExpression) = visitDeclarationReference(expression)
    override fun visitValueAccess(expression: IrValueAccessExpression, data: Nothing?) = visitVariableAccess(expression)

    fun visitGetValue(expression: IrGetValue) = visitVariableAccess(expression)
    override fun visitGetValue(expression: IrGetValue, data: Nothing?) = visitGetValue(expression)

    fun visitSetValue(expression: IrSetValue) = visitVariableAccess(expression)
    override fun visitSetValue(expression: IrSetValue, data: Nothing?) = visitSetValue(expression)

    fun visitFieldAccess(expression: IrFieldAccessExpression) = visitDeclarationReference(expression)
    override fun visitFieldAccess(expression: IrFieldAccessExpression, data: Nothing?) = visitFieldAccess(expression)

    fun visitGetField(expression: IrGetField) = visitFieldAccess(expression)
    override fun visitGetField(expression: IrGetField, data: Nothing?) = visitGetField(expression)

    fun visitSetField(expression: IrSetField) = visitFieldAccess(expression)
    override fun visitSetField(expression: IrSetField, data: Nothing?) = visitSetField(expression)

    fun visitMemberAccess(expression: IrMemberAccessExpression<*>) = visitExpression(expression)
    override fun visitMemberAccess(expression: IrMemberAccessExpression<*>, data: Nothing?) =
        visitMemberAccess(expression)

    fun visitFunctionAccess(expression: IrFunctionAccessExpression) = visitMemberAccess(expression)
    override fun visitFunctionAccess(expression: IrFunctionAccessExpression, data: Nothing?) =
        visitFunctionAccess(expression)

    fun visitCall(expression: IrCall) = visitFunctionAccess(expression)
    override fun visitCall(expression: IrCall, data: Nothing?) = visitCall(expression)

    fun visitConstructorCall(expression: IrConstructorCall) = visitFunctionAccess(expression)
    override fun visitConstructorCall(expression: IrConstructorCall, data: Nothing?) = visitConstructorCall(expression)

    fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall) = visitFunctionAccess(expression)
    override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall, data: Nothing?) =
        visitDelegatingConstructorCall(expression)

    fun visitEnumConstructorCall(expression: IrEnumConstructorCall) = visitFunctionAccess(expression)
    override fun visitEnumConstructorCall(expression: IrEnumConstructorCall, data: Nothing?) =
        visitEnumConstructorCall(expression)

    fun visitGetClass(expression: IrGetClass) = visitExpression(expression)
    override fun visitGetClass(expression: IrGetClass, data: Nothing?) = visitGetClass(expression)

    fun visitCallableReference(expression: IrCallableReference<*>) = visitMemberAccess(expression)
    override fun visitCallableReference(expression: IrCallableReference<*>, data: Nothing?) =
        visitCallableReference(expression)

    fun visitFunctionReference(expression: IrFunctionReference) = visitCallableReference(expression)
    override fun visitFunctionReference(expression: IrFunctionReference, data: Nothing?) =
        visitFunctionReference(expression)

    fun visitPropertyReference(expression: IrPropertyReference) = visitCallableReference(expression)
    override fun visitPropertyReference(expression: IrPropertyReference, data: Nothing?) =
        visitPropertyReference(expression)

    fun visitLocalDelegatedPropertyReference(expression: IrLocalDelegatedPropertyReference) =
        visitCallableReference(expression)

    override fun visitLocalDelegatedPropertyReference(expression: IrLocalDelegatedPropertyReference, data: Nothing?) =
        visitLocalDelegatedPropertyReference(expression)

    fun visitRawFunctionReference(expression: IrRawFunctionReference) = visitDeclarationReference(expression)
    override fun visitRawFunctionReference(expression: IrRawFunctionReference, data: Nothing?) =
        visitRawFunctionReference(expression)

    fun visitFunctionExpression(expression: IrFunctionExpression) = visitExpression(expression)
    override fun visitFunctionExpression(expression: IrFunctionExpression, data: Nothing?) =
        visitFunctionExpression(expression)

    fun visitClassReference(expression: IrClassReference) = visitDeclarationReference(expression)
    override fun visitClassReference(expression: IrClassReference, data: Nothing?) = visitClassReference(expression)

    fun visitInstanceInitializerCall(expression: IrInstanceInitializerCall) = visitExpression(expression)
    override fun visitInstanceInitializerCall(expression: IrInstanceInitializerCall, data: Nothing?) =
        visitInstanceInitializerCall(expression)

    fun visitTypeOperator(expression: IrTypeOperatorCall) = visitExpression(expression)
    override fun visitTypeOperator(expression: IrTypeOperatorCall, data: Nothing?) = visitTypeOperator(expression)

    fun visitWhen(expression: IrWhen) = visitExpression(expression)
    override fun visitWhen(expression: IrWhen, data: Nothing?) = visitWhen(expression)

    fun visitBranch(branch: IrBranch) = visitElement(branch)
    override fun visitBranch(branch: IrBranch, data: Nothing?) = visitBranch(branch)

    fun visitElseBranch(branch: IrElseBranch) = visitBranch(branch)
    override fun visitElseBranch(branch: IrElseBranch, data: Nothing?) = visitElseBranch(branch)

    fun visitLoop(loop: IrLoop) = visitExpression(loop)
    override fun visitLoop(loop: IrLoop, data: Nothing?) = visitLoop(loop)

    fun visitWhileLoop(loop: IrWhileLoop) = visitLoop(loop)
    override fun visitWhileLoop(loop: IrWhileLoop, data: Nothing?) = visitWhileLoop(loop)

    fun visitDoWhileLoop(loop: IrDoWhileLoop) = visitLoop(loop)
    override fun visitDoWhileLoop(loop: IrDoWhileLoop, data: Nothing?) = visitDoWhileLoop(loop)

    fun visitTry(aTry: IrTry) = visitExpression(aTry)
    override fun visitTry(aTry: IrTry, data: Nothing?) = visitTry(aTry)

    fun visitCatch(aCatch: IrCatch) = visitElement(aCatch)
    override fun visitCatch(aCatch: IrCatch, data: Nothing?) = visitCatch(aCatch)

    fun visitBreakContinue(jump: IrBreakContinue) = visitExpression(jump)
    override fun visitBreakContinue(jump: IrBreakContinue, data: Nothing?) = visitBreakContinue(jump)

    fun visitBreak(jump: IrBreak) = visitBreakContinue(jump)
    override fun visitBreak(jump: IrBreak, data: Nothing?) = visitBreak(jump)

    fun visitContinue(jump: IrContinue) = visitBreakContinue(jump)
    override fun visitContinue(jump: IrContinue, data: Nothing?) = visitContinue(jump)

    fun visitReturn(expression: IrReturn) = visitExpression(expression)
    override fun visitReturn(expression: IrReturn, data: Nothing?) = visitReturn(expression)

    fun visitThrow(expression: IrThrow) = visitExpression(expression)
    override fun visitThrow(expression: IrThrow, data: Nothing?) = visitThrow(expression)

    fun visitDynamicExpression(expression: IrDynamicExpression) = visitExpression(expression)
    override fun visitDynamicExpression(expression: IrDynamicExpression, data: Nothing?) =
        visitDynamicExpression(expression)

    fun visitDynamicOperatorExpression(expression: IrDynamicOperatorExpression) = visitDynamicExpression(expression)
    override fun visitDynamicOperatorExpression(expression: IrDynamicOperatorExpression, data: Nothing?) =
        visitDynamicOperatorExpression(expression)

    fun visitDynamicMemberExpression(expression: IrDynamicMemberExpression) = visitDynamicExpression(expression)
    override fun visitDynamicMemberExpression(expression: IrDynamicMemberExpression, data: Nothing?) =
        visitDynamicMemberExpression(expression)

    fun visitErrorDeclaration(declaration: IrErrorDeclaration) = visitDeclaration(declaration)
    override fun visitErrorDeclaration(declaration: IrErrorDeclaration, data: Nothing?) =
        visitErrorDeclaration(declaration)

    fun visitErrorExpression(expression: IrErrorExpression) = visitExpression(expression)
    override fun visitErrorExpression(expression: IrErrorExpression, data: Nothing?) = visitErrorExpression(expression)

    fun visitErrorCallExpression(expression: IrErrorCallExpression) = visitErrorExpression(expression)
    override fun visitErrorCallExpression(expression: IrErrorCallExpression, data: Nothing?) =
        visitErrorCallExpression(expression)
}

fun <R> IrElement.acceptNothing(visitor: IrCustomElementVisitorNothing<R>) = accept(visitor, null)