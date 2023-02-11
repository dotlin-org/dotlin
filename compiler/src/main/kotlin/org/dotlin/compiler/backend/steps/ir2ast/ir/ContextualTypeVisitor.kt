/*
 * Copyright 2023 Wilko Manger
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

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrTypeContext.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

fun IrElement?.visitTypes(block: (IrType, IrTypeContext) -> Unit) {
    this?.acceptVoid(ContextualTypeVisitor(block))
}

private class ContextualTypeVisitor(private val visit: (IrType, IrTypeContext) -> Unit) : IrElementVisitorVoid {
    private fun visitType(type: IrType, context: IrTypeContext) {
        if (type is IrSimpleType) {
            type.arguments.forEach {
                it.typeOrNull?.let { t -> visitType(t, TypeArgument) }
            }
        }

        visit(type, context)
    }

    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitClass(declaration: IrClass) {
        declaration.superTypes.forEach { visitType(it, Class.SuperTypes) }
        super.visitClass(declaration)
    }

    override fun visitValueParameter(declaration: IrValueParameter) {
        visitType(declaration.type, IrTypeContext.Function.ValueParameter.Type)
        declaration.varargElementType?.let { visitType(it, IrTypeContext.Function.ValueParameter.Vararg) }
        super.visitValueParameter(declaration)
    }

    override fun visitTypeParameter(declaration: IrTypeParameter) {
        declaration.superTypes.forEach { visitType(it, TypeParameter.SuperTypes) }
        super.visitTypeParameter(declaration)
    }

    override fun visitVariable(declaration: IrVariable) {
        visitType(declaration.type, Variable.Regular)
        super.visitVariable(declaration)
    }

    override fun visitFunction(declaration: IrFunction) {
        visitType(declaration.returnType, IrTypeContext.Function.ReturnType)
        super.visitFunction(declaration)
    }

    override fun visitField(declaration: IrField) {
        visitType(declaration.type, Field)
        super.visitField(declaration)
    }

    override fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty) {
        visitType(declaration.type, Variable.Delegated)
        super.visitLocalDelegatedProperty(declaration)
    }

    override fun visitTypeAlias(declaration: IrTypeAlias) {
        visitType(declaration.expandedType, TypeAlias)
        super.visitTypeAlias(declaration)
    }

    override fun visitExpression(expression: IrExpression) {
        visitType(expression.type, Expression.Type)
        super.visitExpression(expression)
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall) {
        visitType(expression.typeOperand, Expression.TypeOperatorOperand)
        super.visitTypeOperator(expression)
    }

    override fun visitVararg(expression: IrVararg) {
        visitType(expression.varargElementType, Expression.Vararg)
        super.visitVararg(expression)
    }

    override fun visitClassReference(expression: IrClassReference) {
        visitType(expression.classType, Expression.ClassReference)
        super.visitClassReference(expression)
    }
}

sealed interface IrTypeContext {
    sealed interface Class : IrTypeContext {
        object SuperTypes : Class, IrTypeContext.SuperTypes
    }

    sealed interface TypeParameter : IrTypeContext {
        object SuperTypes : TypeParameter, IrTypeContext.SuperTypes
    }

    sealed interface Variable : IrTypeContext {
        object Regular : Variable
        object Delegated : Variable
    }

    sealed interface Function : IrTypeContext {
        object ReturnType : Function

        sealed interface ValueParameter : Function {
            object Type : ValueParameter
            object Vararg : ValueParameter
        }
    }

    object Field : IrTypeContext
    object TypeAlias : IrTypeContext

    sealed interface Expression : IrTypeContext {
        object Type : Expression
        object TypeOperatorOperand : Expression
        object Vararg : Expression
        object ClassReference : Expression
    }

    sealed interface SuperTypes : IrTypeContext

    object TypeArgument : IrTypeContext
}