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

@file:OptIn(ObsoleteDescriptorBasedAPI::class)

package org.dotlin.compiler.backend

import org.dotlin.compiler.dart.ast.DartAstNode
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.load.kotlin.toSourceElement
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

class DotlinCompilationException(val errors: Collection<Diagnostic>) : Exception()

abstract class DotlinStepError(
    val type: String,
    val element: String,
    private val msg: String,
) : Error() {
    override val message: String by lazy {
        "$type error occurred: $msg\n\nWhile processing:\n$element"
    }
}

class DotlinLoweringError(
    element: IrElement,
    message: String,
    override val cause: Throwable? = null
) : DotlinStepError("Lowering", element.renderWithSource(), message) {
    constructor(
        element: IrElement,
        cause: Throwable,
    ) : this(element, cause.messageOrFallback(), cause)
}

class DotlinTransformerError(element: IrElement, message: String, override val cause: Throwable? = null) :
    DotlinStepError("Transformer", element.renderWithSource(), message) {
    constructor(
        element: IrElement,
        cause: Throwable,
    ) : this(element, cause.messageOrFallback(), cause)
}

class DotlinCodeGeneratorError(node: DartAstNode, message: String, override val cause: Throwable? = null) :
    DotlinStepError("Code generation", node.toString(), message) {
    constructor(
        node: DartAstNode,
        cause: Throwable,
    ) : this(node, cause.messageOrFallback(), cause)
}

private fun Throwable.messageOrFallback() = message ?: "Unknown"

private fun IrElement.renderWithSource(): String {
    val descriptor = safeAs<IrDeclaration>()
        ?.descriptor

    val renderedElement = descriptor
        ?.let { DescriptorRenderer.FQ_NAMES_IN_TYPES_WITH_ANNOTATIONS.render(it) }
        ?: render()

    val renderedSource = descriptor?.toSourceElement?.getPsi()?.run {
        val file = containingFile?.name?.let {  " (file: $it)" } ?: ""
        "\nSource$file:\n$text"
    } ?: ""

    return "$renderedElement$renderedSource\n"
}

private fun <E, R> runAndReport(
    element: E,
    block: (E) -> R,
    createError: (E, Throwable) -> DotlinStepError
): R = try {
    block(element)
} catch (t: Throwable) {
    when (t) {
        !is DotlinStepError -> throw createError(element, t)
        else -> throw t
    }
}

fun <E : IrElement, R> runAndReportTransformerError(element: E, block: (E) -> R): R =
    runAndReport(element, block, ::DotlinTransformerError)

fun <E : IrElement, R> runAndReportLoweringError(element: E, block: (E) -> R): R =
    runAndReport(element, block, ::DotlinLoweringError)

fun <N : DartAstNode, R> runAndReportCodeGenerationError(element: N, block: (N) -> R): R =
    runAndReport(element, block, ::DotlinCodeGeneratorError)