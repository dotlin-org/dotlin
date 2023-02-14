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

package org.dotlin.compiler.backend.steps.ast2dart

import org.dotlin.compiler.backend.steps.ast2dart.transformer.*
import org.dotlin.compiler.dart.ast.DartAstNode
import org.dotlin.compiler.dart.ast.DartLabel
import org.dotlin.compiler.dart.ast.annotation.DartAnnotatedNode
import org.dotlin.compiler.dart.ast.annotation.DartAnnotation
import org.dotlin.compiler.dart.ast.collection.DartCollectionElement
import org.dotlin.compiler.dart.ast.collection.DartCollectionElementList
import org.dotlin.compiler.dart.ast.compilationunit.DartCompilationUnit
import org.dotlin.compiler.dart.ast.declaration.DartDeclaration
import org.dotlin.compiler.dart.ast.declaration.classlike.DartClassLikeDeclaration
import org.dotlin.compiler.dart.ast.declaration.classlike.DartExtendsClause
import org.dotlin.compiler.dart.ast.declaration.classlike.DartImplementsClause
import org.dotlin.compiler.dart.ast.declaration.classlike.DartWithClause
import org.dotlin.compiler.dart.ast.declaration.classlike.member.DartClassMember
import org.dotlin.compiler.dart.ast.declaration.classlike.member.constructor.DartConstructorInitializer
import org.dotlin.compiler.dart.ast.declaration.function.DartNamedFunctionDeclaration
import org.dotlin.compiler.dart.ast.declaration.function.body.DartFunctionBody
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclarationList
import org.dotlin.compiler.dart.ast.directive.DartCombinator
import org.dotlin.compiler.dart.ast.directive.DartDirective
import org.dotlin.compiler.dart.ast.expression.DartArgumentList
import org.dotlin.compiler.dart.ast.expression.DartExpression
import org.dotlin.compiler.dart.ast.expression.literal.DartInterpolationElement
import org.dotlin.compiler.dart.ast.parameter.DartFormalParameter
import org.dotlin.compiler.dart.ast.parameter.DartFormalParameterList
import org.dotlin.compiler.dart.ast.statement.DartForLoopParts
import org.dotlin.compiler.dart.ast.statement.DartStatement
import org.dotlin.compiler.dart.ast.statement.trycatch.DartCatchClause
import org.dotlin.compiler.dart.ast.type.DartTypeAnnotation
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameter
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameterList

class DartGenerationContext {
    lateinit var parent: DartAstNode
        private set

    fun <R> with(parent: DartAstNode, block: () -> R): R {
        val oldParent = when {
            this::parent.isInitialized -> this.parent
            else -> null
        }

        this.parent = parent
        return block().also {
            if (oldParent != null) {
                this.parent = oldParent
            }
        }

    }

    fun DartAstNode.accept(parent: DartAstNode): String {
        val transformer = when (this) {
            is DartExpression, is DartArgumentList, is DartInterpolationElement -> DartExpressionTransformer
            is DartCollectionElement, is DartCollectionElementList -> DartCollectionElementTransformer
            is DartNamedFunctionDeclaration, is DartFunctionBody -> DartFunctionDeclarationTransformer
            is DartClassMember -> DartClassMemberTransformer
            is DartClassLikeDeclaration, is DartExtendsClause, is DartImplementsClause, is DartWithClause -> {
                DartClassLikeTransformer
            }

            is DartDeclaration, is DartVariableDeclarationList -> DartDeclarationTransformer
            is DartCompilationUnit, is DartDirective, is DartCombinator -> DartCompilationUnitTransformer
            is DartConstructorInitializer -> DartConstructorInitializerTransformer
            is DartFormalParameter, is DartFormalParameterList -> DartFormalParameterTransformer
            is DartLabel, is DartAnnotation -> DartMiscTransformer
            is DartStatement, is DartCatchClause, is DartForLoopParts -> DartStatementTransformer
            is DartTypeAnnotation, is DartTypeArgumentList, is DartTypeParameter, is DartTypeParameterList -> {
                DartTypeAnnotationTransformer
            }

            else -> error("No transformer for ${this::class.java.simpleName}")
        }

        return with(parent) {
            accept(transformer, this@DartGenerationContext)
        }
    }

    fun <N : DartAstNode> N.acceptChild(
        prefix: String = "",
        suffix: String = "",
        block: N.() -> DartAstNode?
    ): String = acceptChildOrNull(prefix, suffix, block) ?: ""

    fun <N : DartAstNode> N.acceptChildOrNull(
        prefix: String = "",
        suffix: String = "",
        block: N.() -> DartAstNode?
    ): String? = block(this)?.accept(this)?.let { "$prefix$it$suffix" }

    inline fun <reified N : DartAstNode> DartAstNode.acceptChildIf(
        prefix: String = "",
        suffix: String = "",
        noinline block: N.() -> DartAstNode?
    ): String = if (this is N) acceptChild(prefix, suffix, block) else ""

    fun <N : DartAstNode> N.acceptChild(
        separator: String,
        prefix: String = "",
        suffix: String = "",
        ifEmpty: String = "$prefix$suffix",
        block: N.() -> Collection<DartAstNode>
    ): String = block(this).acceptAll(parent = this@acceptChild, separator, prefix, suffix, ifEmpty)

    inline fun <reified N : DartAstNode> DartAstNode.acceptChildIf(
        separator: String,
        prefix: String = "",
        suffix: String = "",
        ifEmpty: String = "$prefix$suffix",
        noinline block: N.() -> Collection<DartAstNode>
    ): String = if (this is N) acceptChild(separator, prefix, suffix, ifEmpty, block) else ""

    fun <N, C : DartAstNode> N.accept(
        separator: String = "",
        prefix: String = "",
        suffix: String = "",
        ifEmpty: String = "$prefix$suffix"
    ): String where N : Collection<C>, N : DartAstNode = acceptAll(this, separator, prefix, suffix, ifEmpty)

    private fun <N : Collection<C>, C : DartAstNode> N.acceptAll(
        parent: DartAstNode,
        separator: String = "",
        prefix: String = "",
        suffix: String = "",
        ifEmpty: String = "$prefix$suffix"
    ): String = when {
        isEmpty() -> ifEmpty
        else -> joinToString(separator, prefix, suffix) { it.accept(parent) }
    }

    fun DartAnnotatedNode.acceptChildAnnotations() = acceptChild(separator = " ", suffix = " ") { annotations }
}