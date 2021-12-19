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
import org.dotlin.compiler.dart.ast.directive.DartCombinator
import org.dotlin.compiler.dart.ast.directive.DartImportDirective
import org.dotlin.compiler.dart.ast.expression.*
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier
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

    fun visitAstNode(node: DartAstNode, context: C): R = throwUnsupported()

    fun visitCompilationUnit(unit: DartCompilationUnit, context: C): R = visitAstNode(unit, context)

    // Annotation
    fun visitAnnotation(annotation: DartAnnotation, context: C): R = visitAstNode(annotation, context)

    // Declarations
    fun visitTopLevelFunctionDeclaration(functionDeclaration: DartTopLevelFunctionDeclaration, context: C): R =
        visitAstNode(functionDeclaration, context)

    fun visitClassDeclaration(classDeclaration: DartClassDeclaration, context: C): R =
        visitAstNode(classDeclaration, context)

    fun visitExtensionDeclaration(extensionDeclaration: DartExtensionDeclaration, context: C): R =
        visitAstNode(extensionDeclaration, context)

    fun visitMethodDeclaration(methodDeclaration: DartMethodDeclaration, context: C): R =
        visitAstNode(methodDeclaration, context)

    fun visitConstructorDeclaration(constructorDeclaration: DartConstructorDeclaration, context: C): R =
        visitAstNode(constructorDeclaration, context)

    fun visitFieldDeclaration(fieldDeclaration: DartFieldDeclaration, context: C): R =
        visitAstNode(fieldDeclaration, context)

    fun visitVariableDeclaration(variableDeclaration: DartVariableDeclaration, context: C): R =
        visitAstNode(variableDeclaration, context)

    fun visitVariableDeclarationList(variables: DartVariableDeclarationList, context: C): R =
        visitAstNode(variables, context)

    // Declarations: Clauses
    fun visitExtendsClause(extendsClause: DartExtendsClause, context: C): R = visitAstNode(extendsClause, context)
    fun visitImplementsClause(implementsClause: DartImplementsClause, context: C): R =
        visitAstNode(implementsClause, context)

    // Constructor initializers
    fun visitConstructorInvocation(invocation: DartConstructorInvocation, context: C): R =
        visitAstNode(invocation, context)

    fun visitConstructorFieldInitializer(initializer: DartConstructorFieldInitializer, context: C): R =
        visitAstNode(initializer, context)

    // Directives
    fun visitImportDirective(directive: DartImportDirective, context: C): R = visitAstNode(directive, context)
    fun visitCombinator(combinator: DartCombinator, context: C): R = visitAstNode(combinator, context)

    // Expressions
    fun visitArgumentList(arguments: DartArgumentList, context: C): R = visitAstNode(arguments, context)
    fun visitFunctionExpression(functionExpression: DartFunctionExpression, context: C): R =
        visitAstNode(functionExpression, context)

    fun visitIdentifier(identifier: DartIdentifier, context: C): R = visitAstNode(identifier, context)
    fun visitInvocationExpression(invocation: DartInvocationExpression, context: C): R =
        visitAstNode(invocation, context)

    fun visitAssignmentExpression(assignment: DartAssignmentExpression, context: C): R =
        visitAstNode(assignment, context)

    fun visitNamedExpression(namedExpression: DartNamedExpression, context: C): R =
        visitAstNode(namedExpression, context)

    fun visitParenthesizedExpression(parenthesizedExpression: DartParenthesizedExpression, context: C): R =
        visitAstNode(parenthesizedExpression, context)

    fun visitNegatedExpressionExpression(negatedExpression: DartNegatedExpression, context: C): R =
        visitAstNode(negatedExpression, context)

    fun visitInstanceCreationExpression(instanceCreation: DartInstanceCreationExpression, context: C): R =
        visitAstNode(instanceCreation, context)

    fun visitPropertyAccess(propertyAccess: DartPropertyAccessExpression, context: C): R =
        visitAstNode(propertyAccess, context)

    fun visitConditionalExpression(conditional: DartConditionalExpression, context: C): R =
        visitAstNode(conditional, context)

    fun visitIsExpression(isExpression: DartIsExpression, context: C): R = visitAstNode(isExpression, context)
    fun visitAsExpression(asExpression: DartAsExpression, context: C): R = visitAstNode(asExpression, context)
    fun visitThisExpression(thisExpression: DartThisExpression, context: C): R = visitAstNode(thisExpression, context)
    fun visitBinaryInfixExpression(binaryInfix: DartBinaryInfixExpression, context: C): R =
        visitAstNode(binaryInfix, context)

    fun visitThrowExpression(throwExpression: DartThrowExpression, context: C): R =
        visitAstNode(throwExpression, context)

    // Expressions: Literals
    fun visitSimpleStringLiteral(literal: DartSimpleStringLiteral, context: C): R = visitAstNode(literal, context)

    fun visitStringInterpolation(literal: DartStringInterpolation, context: C): R = visitAstNode(literal, context)
    fun visitInterpolationString(interpolationString: DartInterpolationString, context: C): R =
        visitAstNode(interpolationString, context)

    fun visitInterpolationExpression(element: DartInterpolationExpression, context: C): R =
        visitAstNode(element, context)

    fun visitNullLiteral(literal: DartNullLiteral, context: C): R = visitAstNode(literal, context)
    fun visitBooleanLiteral(literal: DartBooleanLiteral, context: C): R = visitAstNode(literal, context)
    fun visitIntegerLiteral(literal: DartIntegerLiteral, context: C): R = visitAstNode(literal, context)
    fun visitDoubleLiteral(literal: DartDoubleLiteral, context: C): R = visitAstNode(literal, context)
    fun visitListLiteral(literal: DartListLiteral, context: C): R = visitAstNode(literal, context)
    fun visitCollectionElementList(collectionElementList: DartCollectionElementList, context: C): R =
        visitAstNode(collectionElementList, context)

    // Parameters
    fun visitFormalParameterList(parameters: DartFormalParameterList, context: C): R = visitAstNode(parameters, context)
    fun visitSimpleFormalParameter(parameter: DartSimpleFormalParameter, context: C): R =
        visitAstNode(parameter, context)

    fun visitFieldFormalParameter(parameter: DartFieldFormalParameter, context: C): R = visitAstNode(parameter, context)
    fun visitDefaultFormalParameter(defaultParameter: DartDefaultFormalParameter, context: C): R =
        visitAstNode(defaultParameter, context)

    // Statements
    fun visitBlock(block: DartBlock, context: C): R = visitAstNode(block, context)
    fun visitExpressionStatement(statement: DartExpressionStatement, context: C): R = visitAstNode(statement, context)
    fun visitVariableDeclarationStatement(statement: DartVariableDeclarationStatement, context: C): R =
        visitAstNode(statement, context)

    fun visitReturnStatement(statement: DartReturnStatement, context: C): R = visitAstNode(statement, context)
    fun visitIfStatement(statement: DartIfStatement, context: C): R = visitAstNode(statement, context)

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

    fun visitCode(code: DartCode, context: C): R = throwUnsupported()
}