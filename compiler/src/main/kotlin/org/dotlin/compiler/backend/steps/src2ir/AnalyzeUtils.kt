/*
 * Copyright 2022 Wilko Manger
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

package org.dotlin.compiler.backend.steps.src2ir

import org.dotlin.compiler.backend.DotlinCompilationException
import org.dotlin.compiler.errors
import org.dotlin.compiler.hasErrors
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.cli.common.messages.DefaultDiagnosticReporter
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTraceContext
import org.jetbrains.kotlin.util.slicedMap.BasicWritableSlice
import org.jetbrains.kotlin.util.slicedMap.RewritePolicy.DO_NOTHING

/**
 * Throws if there are any non-suppressed errors in the `bindingContext`.
 */
fun AnalysisResult.throwIfHasErrors() = bindingContext.diagnosticsExceptSuppressed.throwIfHasErrors()

fun Iterable<Diagnostic>.throwIfHasErrors() {
    if (hasErrors) {
        throw DotlinCompilationException(errors)
    }
}

private val suppressedDiagnosticsSlice = BasicWritableSlice<Int, Diagnostic>(DO_NOTHING)

fun BindingTraceContext.markSuppressed(diagnostic: Diagnostic) {
    record(suppressedDiagnosticsSlice, diagnostic.hashCode(), diagnostic)
}

val BindingContext.diagnosticsExceptSuppressed: Iterable<Diagnostic>
    get() = diagnostics.filter { get(suppressedDiagnosticsSlice, it.hashCode()) == null }

fun DefaultDiagnosticReporter.report(diagnostic: Diagnostic) = diagnostic.let {
    report(it, it.psiFile, DefaultErrorMessages.render(it))
}

fun MessageCollector.report(diagnostic: Diagnostic) {
    DefaultDiagnosticReporter(this).report(diagnostic)
}

fun Iterable<Diagnostic>.reportAll(messageCollector: MessageCollector) {
    val reporter = DefaultDiagnosticReporter(messageCollector)

    forEach {
        reporter.report(it)
    }
}

fun Iterable<Diagnostic>.reportOnly(messageCollector: MessageCollector, vararg diagnostics: DiagnosticFactory<*>) {
    val reporter = DefaultDiagnosticReporter(messageCollector)

    filter {
        it.factory in diagnostics
    }.forEach {
        reporter.report(it)
    }
}

fun DeclarationDescriptor.isIteratorMethod() = this is FunctionDescriptor && isOperator &&
        (name == Name.identifier("next") || name == Name.identifier("hasNext"))