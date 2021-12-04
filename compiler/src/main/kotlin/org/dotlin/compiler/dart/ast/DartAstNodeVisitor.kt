/*
 * Copyright 2021 Wilko Manger
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

package org.dotlin.compiler.dart.ast

import org.dotlin.compiler.dart.ast.annotation.DartAnnotation
import org.dotlin.compiler.dart.ast.collection.DartCollectionElementList
import org.dotlin.compiler.dart.ast.compilationunit.DartCompilationUnit
import org.dotlin.compiler.dart.ast.declaration.classormixin.DartClassDeclaration
import org.dotlin.compiler.dart.ast.declaration.classormixin.DartExtendsClause
import org.dotlin.compiler.dart.ast.declaration.classormixin.DartImplementsClause
import org.dotlin.compiler.dart.ast.declaration.classormixin.member.DartMethodDeclaration
import org.dotlin.compiler.dart.ast.declaration.classormixin.member.constructor.DartConstructorDeclaration
import org.dotlin.compiler.dart.ast.declaration.classormixin.member.constructor.DartConstructorFieldInitializer
import org.dotlin.compiler.dart.ast.declaration.classormixin.member.constructor.DartConstructorInvocation
import org.dotlin.compiler.dart.ast.declaration.classormixin.member.constructor.DartFieldDeclaration
import org.dotlin.compiler.dart.ast.declaration.extension.DartExtensionDeclaration
import org.dotlin.compiler.dart.ast.declaration.function.DartTopLevelFunctionDeclaration
import org.dotlin.compiler.dart.ast.declaration.function.body.DartBlockFunctionBody
import org.dotlin.compiler.dart.ast.declaration.function.body.DartEmptyFunctionBody
import org.dotlin.compiler.dart.ast.declaration.function.body.DartExpressionFunctionBody
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclaration
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclarationList
import org.dotlin.compiler.dart.ast.expression.*
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.expression.invocation.DartInvocationExpression
import org.dotlin.compiler.dart.ast.expression.literal.*
import org.dotlin.compiler.dart.ast.parameter.DartDefaultFormalParameter
import org.dotlin.compiler.dart.ast.parameter.DartFieldFormalParameter
import org.dotlin.compiler.dart.ast.parameter.DartFormalParameterList
import org.dotlin.compiler.dart.ast.parameter.DartSimpleFormalParameter
import org.dotlin.compiler.dart.ast.statement.*
import org.dotlin.compiler.dart.ast.type.DartNamedType
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameter
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameterList

interface DartAstNodeVisitor<R, C> {
    private fun throwUnsupported(): Nothing = throw UnsupportedOperationException()

    fun visitCompilationUnit(unit: DartCompilationUnit, context: C): R = throwUnsupported()

    // Annotation
    fun visitAnnotation(annotation: DartAnnotation, context: C): R = throwUnsupported()

    // Declarations
    fun visitTopLevelFunctionDeclaration(functionDeclaration: DartTopLevelFunctionDeclaration, context: C): R =
        throwUnsupported()

    fun visitClassDeclaration(classDeclaration: DartClassDeclaration, context: C): R = throwUnsupported()
    fun visitExtensionDeclaration(extensionDeclaration: DartExtensionDeclaration, context: C): R = throwUnsupported()
    fun visitMethodDeclaration(methodDeclaration: DartMethodDeclaration, context: C): R = throwUnsupported()
    fun visitConstructorDeclaration(constructorDeclaration: DartConstructorDeclaration, context: C): R =
        throwUnsupported()

    fun visitFieldDeclaration(fieldDeclaration: DartFieldDeclaration, context: C): R =
        throwUnsupported()

    fun visitVariableDeclaration(variableDeclaration: DartVariableDeclaration, context: C): R = throwUnsupported()
    fun visitVariableDeclarationList(variables: DartVariableDeclarationList, context: C): R = throwUnsupported()

    // Declarations: Clauses
    fun visitExtendsClause(extendsClause: DartExtendsClause, context: C): R = throwUnsupported()
    fun visitImplementsClause(implementsClause: DartImplementsClause, context: C): R = throwUnsupported()

    // Constructor initializers
    fun visitConstructorInvocation(invocation: DartConstructorInvocation, context: C): R =
        throwUnsupported()

    fun visitConstructorFieldInitializer(initializer: DartConstructorFieldInitializer, context: C): R =
        throwUnsupported()

    // Directives
    fun visitDirective(directive: DartDirective, context: C): R = throwUnsupported()

    // Expressions
    fun visitArgumentList(arguments: DartArgumentList, context: C): R = throwUnsupported()
    fun visitFunctionExpression(functionExpression: DartFunctionExpression, context: C): R = throwUnsupported()
    fun visitSimpleIdentifier(identifier: DartSimpleIdentifier, context: C): R = throwUnsupported()
    fun visitInvocationExpression(invocation: DartInvocationExpression, context: C): R = throwUnsupported()

    fun visitAssignmentExpression(assignment: DartAssignmentExpression, context: C): R = throwUnsupported()
    fun visitNamedExpression(namedExpression: DartNamedExpression, context: C): R = throwUnsupported()
    fun visitParenthesizedExpression(parenthesizedExpression: DartParenthesizedExpression, context: C): R =
        throwUnsupported()

    fun visitInstanceCreationExpression(instanceCreation: DartInstanceCreationExpression, context: C): R =
        throwUnsupported()

    fun visitPropertyAccess(propertyAccess: DartPropertyAccessExpression, context: C): R = throwUnsupported()
    fun visitConditionalExpression(conditional: DartConditionalExpression, context: C): R = throwUnsupported()
    fun visitIsExpression(isExpression: DartIsExpression, context: C): R = throwUnsupported()
    fun visitAsExpression(asExpression: DartAsExpression, context: C): R = throwUnsupported()
    fun visitThisExpression(thisExpression: DartThisExpression, context: C): R = throwUnsupported()
    fun visitBinaryInfixExpression(binaryInfix: DartBinaryInfixExpression, context: C): R = throwUnsupported()
    fun visitThrowExpression(throwExpression: DartThrowExpression, context: C): R = throwUnsupported()

    // Expressions: Literals
    fun visitSimpleStringLiteral(literal: DartSimpleStringLiteral, context: C): R = throwUnsupported()
    fun visitNullLiteral(literal: DartNullLiteral, context: C): R = throwUnsupported()
    fun visitBooleanLiteral(literal: DartBooleanLiteral, context: C): R = throwUnsupported()
    fun visitIntegerLiteral(literal: DartIntegerLiteral, context: C): R = throwUnsupported()
    fun visitDoubleLiteral(literal: DartDoubleLiteral, context: C): R = throwUnsupported()
    fun visitListLiteral(literal: DartListLiteral, context: C): R = throwUnsupported()
    fun visitCollectionElementList(collectionElementList: DartCollectionElementList, context: C): R = throwUnsupported()

    // Parameters
    fun visitFormalParameterList(parameters: DartFormalParameterList, context: C): R = throwUnsupported()
    fun visitSimpleFormalParameter(parameter: DartSimpleFormalParameter, context: C): R = throwUnsupported()
    fun visitFieldFormalParameter(parameter: DartFieldFormalParameter, context: C): R = throwUnsupported()
    fun visitDefaultFormalParameter(defaultParameter: DartDefaultFormalParameter, context: C): R = throwUnsupported()

    // Statements
    fun visitBlock(block: DartBlock, context: C): R = throwUnsupported()
    fun visitExpressionStatement(statement: DartExpressionStatement, context: C): R = throwUnsupported()
    fun visitVariableDeclarationStatement(statement: DartVariableDeclarationStatement, context: C): R =
        throwUnsupported()

    fun visitReturnStatement(statement: DartReturnStatement, context: C): R = throwUnsupported()
    fun visitIfStatement(statement: DartIfStatement, context: C): R = throwUnsupported()

    // Type

    fun visitTypeArgumentList(typeArguments: DartTypeArgumentList, context: C): R = throwUnsupported()
    fun visitNamedType(type: DartNamedType, context: C): R = throwUnsupported()
    fun visitTypeParameterList(typeParameters: DartTypeParameterList, context: C): R = throwUnsupported()
    fun visitTypeParameter(typeParameter: DartTypeParameter, context: C): R = throwUnsupported()

    // Function body
    fun visitEmptyFunctionBody(body: DartEmptyFunctionBody, context: C): R = throwUnsupported()
    fun visitBlockFunctionBody(body: DartBlockFunctionBody, context: C): R = throwUnsupported()
    fun visitExpressionFunctionBody(body: DartExpressionFunctionBody, context: C): R = throwUnsupported()

    fun visitLabel(label: DartLabel, context: C): R = throwUnsupported()
}