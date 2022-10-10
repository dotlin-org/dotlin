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

package org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.declaration

import org.dotlin.compiler.backend.dotlin
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.dotlin.compiler.backend.util.getFqName
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext

object DartNameAnnotationOverrideChecker : DeclarationChecker {
    override fun check(
        declaration: KtDeclaration,
        descriptor: DeclarationDescriptor,
        context: DeclarationCheckerContext
    ) {
        if (!descriptor.annotations.hasAnnotation(dotlin.DartName)) return

        if (descriptor is CallableMemberDescriptor && descriptor.original.overriddenDescriptors.isNotEmpty()) {
            val source = declaration.annotationEntries.firstOrNull {
                it.getFqName(context.trace.bindingContext) == dotlin.DartName
            }
            context.trace.report(ErrorsDart.DART_NAME_ON_OVERRIDE.on(source ?: declaration))
        }
    }
}