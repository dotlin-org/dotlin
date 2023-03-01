/*
 * Copyright 2021-2022 Wilko Manger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dotlin.compiler.backend.steps.ast2dart.transformer

import org.dotlin.compiler.backend.runAndReportCodeGenerationError
import org.dotlin.compiler.backend.steps.ast2dart.DartGenerationContext
import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.DartLabel
import org.dotlin.compiler.dart.ast.annotation.DartAnnotation
import org.dotlin.compiler.dart.ast.collection.DartCollectionElementList
import org.dotlin.compiler.dart.ast.compilationunit.DartCompilationUnit
import org.dotlin.compiler.dart.ast.declaration.classlike.*
import org.dotlin.compiler.dart.ast.declaration.classlike.member.DartFieldDeclaration
import org.dotlin.compiler.dart.ast.declaration.classlike.member.DartMethodDeclaration
import org.dotlin.compiler.dart.ast.declaration.classlike.member.constructor.DartConstructorDeclaration
import org.dotlin.compiler.dart.ast.declaration.classlike.member.constructor.DartConstructorFieldInitializer
import org.dotlin.compiler.dart.ast.declaration.classlike.member.constructor.DartConstructorInvocation
import org.dotlin.compiler.dart.ast.declaration.function.DartNamedFunctionDeclaration
import org.dotlin.compiler.dart.ast.declaration.function.DartTopLevelFunctionDeclaration
import org.dotlin.compiler.dart.ast.declaration.function.body.DartBlockFunctionBody
import org.dotlin.compiler.dart.ast.declaration.function.body.DartEmptyFunctionBody
import org.dotlin.compiler.dart.ast.declaration.function.body.DartExpressionFunctionBody
import org.dotlin.compiler.dart.ast.declaration.variable.DartTopLevelVariableDeclaration
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclaration
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclarationList
import org.dotlin.compiler.dart.ast.directive.DartCombinator
import org.dotlin.compiler.dart.ast.directive.DartNamespaceDirective
import org.dotlin.compiler.dart.ast.expression.*
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier
import org.dotlin.compiler.dart.ast.expression.invocation.DartInvocationExpression
import org.dotlin.compiler.dart.ast.expression.literal.*
import org.dotlin.compiler.dart.ast.parameter.DartDefaultFormalParameter
import org.dotlin.compiler.dart.ast.parameter.DartFieldFormalParameter
import org.dotlin.compiler.dart.ast.parameter.DartFormalParameterList
import org.dotlin.compiler.dart.ast.parameter.DartSimpleFormalParameter
import org.dotlin.compiler.dart.ast.statement.*
import org.dotlin.compiler.dart.ast.statement.declaration.DartVariableDeclarationStatement
import org.dotlin.compiler.dart.ast.statement.trycatch.DartCatchClause
import org.dotlin.compiler.dart.ast.statement.trycatch.DartTryStatement
import org.dotlin.compiler.dart.ast.type.DartFunctionType
import org.dotlin.compiler.dart.ast.type.DartNamedType
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameter
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameterList
import org.dotlin.compiler.dart.ast.`typealias`.DartTypeAlias

abstract class DartAstNodeTransformer : DartAstNodeVisitor<String, DartGenerationContext> {
    final override fun visitCompilationUnit(unit: DartCompilationUnit, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(unit) { visitCompilationUnit(it) } }

    open fun DartGenerationContext.visitCompilationUnit(unit: DartCompilationUnit) =
        super.visitCompilationUnit(unit, this)

    // Annotation
    final override fun visitAnnotation(annotation: DartAnnotation, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(annotation) { visitAnnotation(it) } }

    open fun DartGenerationContext.visitAnnotation(annotation: DartAnnotation) = super.visitAnnotation(annotation, this)

    // Type alias
    final override fun visitTypeAlias(typeAlias: DartTypeAlias, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(typeAlias) { visitTypeAlias(it) } }

    open fun DartGenerationContext.visitTypeAlias(typeAlias: DartTypeAlias) = super.visitTypeAlias(typeAlias, this)

    // Declarations
    final override fun visitNamedFunctionDeclaration(
        functionDeclaration: DartNamedFunctionDeclaration,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(functionDeclaration) { visitNamedFunctionDeclaration(it) } }

    open fun DartGenerationContext.visitNamedFunctionDeclaration(functionDeclaration: DartNamedFunctionDeclaration) =
        super.visitNamedFunctionDeclaration(functionDeclaration, this)

    final override fun visitTopLevelFunctionDeclaration(
        functionDeclaration: DartTopLevelFunctionDeclaration,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(functionDeclaration) { visitTopLevelFunctionDeclaration(it) } }

    open fun DartGenerationContext.visitTopLevelFunctionDeclaration(functionDeclaration: DartTopLevelFunctionDeclaration) =
        super.visitTopLevelFunctionDeclaration(functionDeclaration, this)

    final override fun visitClassLikeDeclaration(
        classLikeDeclaration: DartClassLikeDeclaration,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(classLikeDeclaration) { visitClassLikeDeclaration(it) } }

    open fun DartGenerationContext.visitClassLikeDeclaration(classLikeDeclaration: DartClassLikeDeclaration) =
        super.visitClassLikeDeclaration(classLikeDeclaration, this)

    final override fun visitClassDeclaration(
        classDeclaration: DartClassDeclaration,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(classDeclaration) { visitClassDeclaration(it) } }

    open fun DartGenerationContext.visitClassDeclaration(classDeclaration: DartClassDeclaration) =
        super.visitClassDeclaration(classDeclaration, this)

    final override fun visitEnumDeclaration(
        enumDeclaration: DartEnumDeclaration,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(enumDeclaration) { visitEnumDeclaration(it) } }

    open fun DartGenerationContext.visitEnumDeclaration(enumDeclaration: DartEnumDeclaration) =
        super.visitEnumDeclaration(enumDeclaration, this)

    final override fun visitEnumConstantDeclaration(
        enumConstant: DartEnumDeclaration.Constant,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(enumConstant) { visitEnumConstantDeclaration(it) } }

    open fun DartGenerationContext.visitEnumConstantDeclaration(enumConstant: DartEnumDeclaration.Constant) =
        super.visitEnumConstantDeclaration(enumConstant, this)

    final override fun visitExtensionDeclaration(
        extensionDeclaration: DartExtensionDeclaration,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(extensionDeclaration) { visitExtensionDeclaration(it) } }

    open fun DartGenerationContext.visitExtensionDeclaration(extensionDeclaration: DartExtensionDeclaration) =
        super.visitExtensionDeclaration(extensionDeclaration, this)

    final override fun visitMethodDeclaration(
        methodDeclaration: DartMethodDeclaration,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(methodDeclaration) { visitMethodDeclaration(it) } }

    open fun DartGenerationContext.visitMethodDeclaration(methodDeclaration: DartMethodDeclaration) =
        super.visitMethodDeclaration(methodDeclaration, this)

    final override fun visitConstructorDeclaration(
        constructorDeclaration: DartConstructorDeclaration,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(constructorDeclaration) { visitConstructorDeclaration(it) } }

    open fun DartGenerationContext.visitConstructorDeclaration(constructorDeclaration: DartConstructorDeclaration) =
        super.visitConstructorDeclaration(constructorDeclaration, this)

    final override fun visitFieldDeclaration(
        fieldDeclaration: DartFieldDeclaration,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(fieldDeclaration) { visitFieldDeclaration(it) } }

    open fun DartGenerationContext.visitFieldDeclaration(fieldDeclaration: DartFieldDeclaration) =
        super.visitFieldDeclaration(fieldDeclaration, this)

    final override fun visitTopLevelVariableDeclaration(
        variableDeclaration: DartTopLevelVariableDeclaration,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(variableDeclaration) { visitTopLevelVariableDeclaration(it) } }

    open fun DartGenerationContext.visitTopLevelVariableDeclaration(variableDeclaration: DartTopLevelVariableDeclaration) =
        super.visitTopLevelVariableDeclaration(variableDeclaration, this)

    final override fun visitVariableDeclaration(
        variableDeclaration: DartVariableDeclaration,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(variableDeclaration) { visitVariableDeclaration(it) } }

    open fun DartGenerationContext.visitVariableDeclaration(variableDeclaration: DartVariableDeclaration) =
        super.visitVariableDeclaration(variableDeclaration, this)

    final override fun visitVariableDeclarationList(
        variables: DartVariableDeclarationList,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(variables) { visitVariableDeclarationList(it) } }

    open fun DartGenerationContext.visitVariableDeclarationList(variables: DartVariableDeclarationList) =
        super.visitVariableDeclarationList(variables, this)

    // Declarations: Clauses
    final override fun visitExtendsClause(extendsClause: DartExtendsClause, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(extendsClause) { visitExtendsClause(it) } }

    open fun DartGenerationContext.visitExtendsClause(extendsClause: DartExtendsClause) =
        super.visitExtendsClause(extendsClause, this)

    final override fun visitImplementsClause(
        implementsClause: DartImplementsClause,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(implementsClause) { visitImplementsClause(it) } }

    open fun DartGenerationContext.visitImplementsClause(implementsClause: DartImplementsClause) =
        super.visitImplementsClause(implementsClause, this)

    final override fun visitWithClause(withClause: DartWithClause, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(withClause) { visitWithClause(it) } }

    open fun DartGenerationContext.visitWithClause(withClause: DartWithClause) = super.visitWithClause(withClause, this)

    final override fun visitCatchClause(catchClause: DartCatchClause, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(catchClause) { visitCatchClause(it) } }

    open fun DartGenerationContext.visitCatchClause(catchClause: DartCatchClause) =
        super.visitCatchClause(catchClause, this)

    // Constructor initializers
    final override fun visitConstructorInvocation(
        invocation: DartConstructorInvocation,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(invocation) { visitConstructorInvocation(it) } }

    open fun DartGenerationContext.visitConstructorInvocation(invocation: DartConstructorInvocation) =
        super.visitConstructorInvocation(invocation, this)

    final override fun visitConstructorFieldInitializer(
        initializer: DartConstructorFieldInitializer,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(initializer) { visitConstructorFieldInitializer(it) } }

    open fun DartGenerationContext.visitConstructorFieldInitializer(initializer: DartConstructorFieldInitializer) =
        super.visitConstructorFieldInitializer(initializer, this)

    // Directives
    final override fun visitNamespaceDirective(
        directive: DartNamespaceDirective,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(directive) { visitNamespaceDirective(it) } }

    open fun DartGenerationContext.visitNamespaceDirective(directive: DartNamespaceDirective) =
        super.visitNamespaceDirective(directive, this)

    final override fun visitCombinator(combinator: DartCombinator, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(combinator) { visitCombinator(it) } }

    open fun DartGenerationContext.visitCombinator(combinator: DartCombinator) = super.visitCombinator(combinator, this)

    // Expressions
    final override fun visitArgumentList(arguments: DartArgumentList, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(arguments) { visitArgumentList(it) } }

    open fun DartGenerationContext.visitArgumentList(arguments: DartArgumentList) =
        super.visitArgumentList(arguments, this)

    final override fun visitFunctionExpression(
        functionExpression: DartFunctionExpression,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(functionExpression) { visitFunctionExpression(it) } }

    open fun DartGenerationContext.visitFunctionExpression(functionExpression: DartFunctionExpression) =
        super.visitFunctionExpression(functionExpression, this)

    final override fun visitFunctionReference(
        functionReference: DartFunctionReference,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(functionReference) { visitFunctionReference(it) } }

    open fun DartGenerationContext.visitFunctionReference(functionReference: DartFunctionReference) =
        super.visitFunctionReference(functionReference, this)

    final override fun visitIdentifier(identifier: DartIdentifier, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(identifier) { visitIdentifier(it) } }

    open fun DartGenerationContext.visitIdentifier(identifier: DartIdentifier) = super.visitIdentifier(identifier, this)

    final override fun visitInvocationExpression(
        invocation: DartInvocationExpression,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(invocation) { visitInvocationExpression(it) } }

    open fun DartGenerationContext.visitInvocationExpression(invocation: DartInvocationExpression) =
        super.visitInvocationExpression(invocation, this)

    final override fun visitAssignmentExpression(
        assignment: DartAssignmentExpression,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(assignment) { visitAssignmentExpression(it) } }

    open fun DartGenerationContext.visitAssignmentExpression(assignment: DartAssignmentExpression) =
        super.visitAssignmentExpression(assignment, this)

    final override fun visitNamedExpression(
        namedExpression: DartNamedExpression,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(namedExpression) { visitNamedExpression(it) } }

    open fun DartGenerationContext.visitNamedExpression(namedExpression: DartNamedExpression) =
        super.visitNamedExpression(namedExpression, this)

    final override fun visitIndexExpression(
        indexExpression: DartIndexExpression,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(indexExpression) { visitIndexExpression(it) } }

    open fun DartGenerationContext.visitIndexExpression(indexExpression: DartIndexExpression) =
        super.visitIndexExpression(indexExpression, this)

    final override fun visitParenthesizedExpression(
        parenthesizedExpression: DartParenthesizedExpression,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(parenthesizedExpression) { visitParenthesizedExpression(it) } }

    open fun DartGenerationContext.visitParenthesizedExpression(parenthesizedExpression: DartParenthesizedExpression) =
        super.visitParenthesizedExpression(parenthesizedExpression, this)

    final override fun visitInstanceCreationExpression(
        instanceCreation: DartInstanceCreationExpression,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(instanceCreation) { visitInstanceCreationExpression(it) } }

    open fun DartGenerationContext.visitInstanceCreationExpression(instanceCreation: DartInstanceCreationExpression) =
        super.visitInstanceCreationExpression(instanceCreation, this)

    final override fun visitPropertyAccess(
        propertyAccess: DartPropertyAccessExpression,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(propertyAccess) { visitPropertyAccess(it) } }

    open fun DartGenerationContext.visitPropertyAccess(propertyAccess: DartPropertyAccessExpression) =
        super.visitPropertyAccess(propertyAccess, this)

    final override fun visitConditionalExpression(
        conditional: DartConditionalExpression,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(conditional) { visitConditionalExpression(it) } }

    open fun DartGenerationContext.visitConditionalExpression(conditional: DartConditionalExpression) =
        super.visitConditionalExpression(conditional, this)

    final override fun visitIsExpression(isExpression: DartIsExpression, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(isExpression) { visitIsExpression(it) } }

    open fun DartGenerationContext.visitIsExpression(isExpression: DartIsExpression) =
        super.visitIsExpression(isExpression, this)

    final override fun visitAsExpression(asExpression: DartAsExpression, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(asExpression) { visitAsExpression(it) } }

    open fun DartGenerationContext.visitAsExpression(asExpression: DartAsExpression) =
        super.visitAsExpression(asExpression, this)

    final override fun visitThisExpression(thisExpression: DartThisExpression, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(thisExpression) { visitThisExpression(it) } }

    open fun DartGenerationContext.visitThisExpression(thisExpression: DartThisExpression) =
        super.visitThisExpression(thisExpression, this)

    final override fun visitSuperExpression(
        superExpression: DartSuperExpression,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(superExpression) { visitSuperExpression(it) } }

    open fun DartGenerationContext.visitSuperExpression(superExpression: DartSuperExpression) =
        super.visitSuperExpression(superExpression, this)

    final override fun visitBinaryInfixExpression(
        binaryInfix: DartBinaryInfixExpression,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(binaryInfix) { visitBinaryInfixExpression(it) } }

    open fun DartGenerationContext.visitBinaryInfixExpression(binaryInfix: DartBinaryInfixExpression) =
        super.visitBinaryInfixExpression(binaryInfix, this)

    final override fun visitPrefixExpression(
        prefixExpression: DartPrefixExpression,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(prefixExpression) { visitPrefixExpression(it) } }

    open fun DartGenerationContext.visitPrefixExpression(prefixExpression: DartPrefixExpression) =
        super.visitPrefixExpression(prefixExpression, this)

    final override fun visitPostfixExpression(
        postfixExpression: DartPostfixExpression,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(postfixExpression) { visitPostfixExpression(it) } }

    open fun DartGenerationContext.visitPostfixExpression(postfixExpression: DartPostfixExpression) =
        super.visitPostfixExpression(postfixExpression, this)

    final override fun visitThrowExpression(
        throwExpression: DartThrowExpression,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(throwExpression) { visitThrowExpression(it) } }

    open fun DartGenerationContext.visitThrowExpression(throwExpression: DartThrowExpression) =
        super.visitThrowExpression(throwExpression, this)

    // Expressions: Literals
    final override fun visitSimpleStringLiteral(
        literal: DartSimpleStringLiteral,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(literal) { visitSimpleStringLiteral(it) } }

    open fun DartGenerationContext.visitSimpleStringLiteral(literal: DartSimpleStringLiteral) =
        super.visitSimpleStringLiteral(literal, this)

    final override fun visitStringInterpolation(
        literal: DartStringInterpolation,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(literal) { visitStringInterpolation(it) } }

    open fun DartGenerationContext.visitStringInterpolation(literal: DartStringInterpolation) =
        super.visitStringInterpolation(literal, this)

    final override fun visitInterpolationString(
        interpolationString: DartInterpolationString,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(interpolationString) { visitInterpolationString(it) } }

    open fun DartGenerationContext.visitInterpolationString(interpolationString: DartInterpolationString) =
        super.visitInterpolationString(interpolationString, this)

    final override fun visitInterpolationExpression(
        element: DartInterpolationExpression,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(element) { visitInterpolationExpression(it) } }

    open fun DartGenerationContext.visitInterpolationExpression(element: DartInterpolationExpression) =
        super.visitInterpolationExpression(element, this)

    final override fun visitNullLiteral(literal: DartNullLiteral, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(literal) { visitNullLiteral(it) } }

    open fun DartGenerationContext.visitNullLiteral(literal: DartNullLiteral) = super.visitNullLiteral(literal, this)

    final override fun visitTypeLiteral(literal: DartTypeLiteral, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(literal) { visitTypeLiteral(it) } }

    open fun DartGenerationContext.visitTypeLiteral(literal: DartTypeLiteral) = super.visitTypeLiteral(literal, this)

    final override fun visitBooleanLiteral(literal: DartBooleanLiteral, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(literal) { visitBooleanLiteral(it) } }

    open fun DartGenerationContext.visitBooleanLiteral(literal: DartBooleanLiteral) =
        super.visitBooleanLiteral(literal, this)

    final override fun visitIntegerLiteral(literal: DartIntegerLiteral, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(literal) { visitIntegerLiteral(it) } }

    open fun DartGenerationContext.visitIntegerLiteral(literal: DartIntegerLiteral) =
        super.visitIntegerLiteral(literal, this)

    final override fun visitDoubleLiteral(literal: DartDoubleLiteral, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(literal) { visitDoubleLiteral(it) } }

    open fun DartGenerationContext.visitDoubleLiteral(literal: DartDoubleLiteral) =
        super.visitDoubleLiteral(literal, this)

    final override fun visitCollectionLiteral(literal: DartCollectionLiteral, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(literal) { visitCollectionLiteral(it) } }

    open fun DartGenerationContext.visitCollectionLiteral(literal: DartCollectionLiteral) =
        super.visitCollectionLiteral(literal, this)

    final override fun visitCollectionElementList(
        collectionElementList: DartCollectionElementList,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(collectionElementList) { visitCollectionElementList(it) } }

    open fun DartGenerationContext.visitCollectionElementList(collectionElementList: DartCollectionElementList) =
        super.visitCollectionElementList(collectionElementList, this)

    // Parameters
    final override fun visitFormalParameterList(
        parameters: DartFormalParameterList,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(parameters) { visitFormalParameterList(it) } }

    open fun DartGenerationContext.visitFormalParameterList(parameters: DartFormalParameterList) =
        super.visitFormalParameterList(parameters, this)

    final override fun visitSimpleFormalParameter(
        parameter: DartSimpleFormalParameter,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(parameter) { visitSimpleFormalParameter(it) } }

    open fun DartGenerationContext.visitSimpleFormalParameter(parameter: DartSimpleFormalParameter) =
        super.visitSimpleFormalParameter(parameter, this)

    final override fun visitFieldFormalParameter(
        parameter: DartFieldFormalParameter,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(parameter) { visitFieldFormalParameter(it) } }

    open fun DartGenerationContext.visitFieldFormalParameter(parameter: DartFieldFormalParameter) =
        super.visitFieldFormalParameter(parameter, this)

    final override fun visitDefaultFormalParameter(
        defaultParameter: DartDefaultFormalParameter,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(defaultParameter) { visitDefaultFormalParameter(it) } }

    open fun DartGenerationContext.visitDefaultFormalParameter(defaultParameter: DartDefaultFormalParameter) =
        super.visitDefaultFormalParameter(defaultParameter, this)

    // Statements
    final override fun visitBlock(block: DartBlock, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(block) { visitBlock(it) } }

    open fun DartGenerationContext.visitBlock(block: DartBlock) = super.visitBlock(block, this)

    final override fun visitExpressionStatement(
        statement: DartExpressionStatement,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(statement) { visitExpressionStatement(it) } }

    open fun DartGenerationContext.visitExpressionStatement(statement: DartExpressionStatement) =
        super.visitExpressionStatement(statement, this)

    final override fun visitVariableDeclarationStatement(
        statement: DartVariableDeclarationStatement,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(statement) { visitVariableDeclarationStatement(it) } }

    open fun DartGenerationContext.visitVariableDeclarationStatement(statement: DartVariableDeclarationStatement) =
        super.visitVariableDeclarationStatement(statement, this)

    final override fun visitReturnStatement(statement: DartReturnStatement, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(statement) { visitReturnStatement(it) } }

    open fun DartGenerationContext.visitReturnStatement(statement: DartReturnStatement) =
        super.visitReturnStatement(statement, this)

    final override fun visitIfStatement(statement: DartIfStatement, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(statement) { visitIfStatement(it) } }

    open fun DartGenerationContext.visitIfStatement(statement: DartIfStatement) =
        super.visitIfStatement(statement, this)

    final override fun visitTryStatement(statement: DartTryStatement, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(statement) { visitTryStatement(it) } }

    open fun DartGenerationContext.visitTryStatement(statement: DartTryStatement) =
        super.visitTryStatement(statement, this)

    final override fun visitWhileStatement(statement: DartWhileStatement, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(statement) { visitWhileStatement(it) } }

    open fun DartGenerationContext.visitWhileStatement(statement: DartWhileStatement) =
        super.visitWhileStatement(statement, this)

    final override fun visitForStatement(statement: DartForStatement, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(statement) { visitForStatement(it) } }

    open fun DartGenerationContext.visitForStatement(statement: DartForStatement) =
        super.visitForStatement(statement, this)

    final override fun visitForPartsWithDeclarations(
        forParts: DartForPartsWithDeclarations,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(forParts) { visitForPartsWithDeclarations(it) } }

    open fun DartGenerationContext.visitForPartsWithDeclarations(forParts: DartForPartsWithDeclarations) =
        super.visitForPartsWithDeclarations(forParts, this)

    final override fun visitForEachPartsWithDeclarations(
        forParts: DartForEachPartsWithDeclarations,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(forParts) { visitForEachPartsWithDeclarations(it) } }

    open fun DartGenerationContext.visitForEachPartsWithDeclarations(forParts: DartForEachPartsWithDeclarations) =
        super.visitForEachPartsWithDeclarations(forParts, this)

    // Type
    final override fun visitTypeArgumentList(
        typeArguments: DartTypeArgumentList,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(typeArguments) { visitTypeArgumentList(it) } }

    open fun DartGenerationContext.visitTypeArgumentList(typeArguments: DartTypeArgumentList) =
        super.visitTypeArgumentList(typeArguments, this)

    final override fun visitNamedType(type: DartNamedType, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(type) { visitNamedType(it) } }

    open fun DartGenerationContext.visitNamedType(type: DartNamedType) = super.visitNamedType(type, this)
    final override fun visitFunctionType(type: DartFunctionType, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(type) { visitFunctionType(it) } }

    open fun DartGenerationContext.visitFunctionType(type: DartFunctionType) = super.visitFunctionType(type, this)

    final override fun visitTypeParameterList(
        typeParameters: DartTypeParameterList,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(typeParameters) { visitTypeParameterList(it) } }

    open fun DartGenerationContext.visitTypeParameterList(typeParameters: DartTypeParameterList) =
        super.visitTypeParameterList(typeParameters, this)

    final override fun visitTypeParameter(typeParameter: DartTypeParameter, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(typeParameter) { visitTypeParameter(it) } }

    open fun DartGenerationContext.visitTypeParameter(typeParameter: DartTypeParameter) =
        super.visitTypeParameter(typeParameter, this)

    // Function body
    final override fun visitEmptyFunctionBody(body: DartEmptyFunctionBody, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(body) { visitEmptyFunctionBody(it) } }

    open fun DartGenerationContext.visitEmptyFunctionBody(body: DartEmptyFunctionBody) =
        super.visitEmptyFunctionBody(body, this)

    final override fun visitBlockFunctionBody(body: DartBlockFunctionBody, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(body) { visitBlockFunctionBody(it) } }

    open fun DartGenerationContext.visitBlockFunctionBody(body: DartBlockFunctionBody) =
        super.visitBlockFunctionBody(body, this)

    final override fun visitExpressionFunctionBody(
        body: DartExpressionFunctionBody,
        context: DartGenerationContext
    ): String = with(context) { runAndReportCodeGenerationError(body) { visitExpressionFunctionBody(it) } }

    open fun DartGenerationContext.visitExpressionFunctionBody(body: DartExpressionFunctionBody) =
        super.visitExpressionFunctionBody(body, this)

    final override fun visitLabel(label: DartLabel, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(label) { visitLabel(it) } }

    open fun DartGenerationContext.visitLabel(label: DartLabel) = super.visitLabel(label, this)

    final override fun visitCode(code: DartCode, context: DartGenerationContext): String =
        with(context) { runAndReportCodeGenerationError(code) { visitCode(it) } }

    open fun DartGenerationContext.visitCode(code: DartCode) = super.visitCode(code, this)

}