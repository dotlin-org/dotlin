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

package org.dotlin.compiler.backend.descriptors.annotation

import org.dotlin.compiler.backend.descriptors.DartDescriptor
import org.dotlin.compiler.backend.descriptors.DartDescriptorContext
import org.dotlin.compiler.backend.descriptors.printScope
import org.dotlin.compiler.dart.element.DartClassElement
import org.dotlin.compiler.dart.element.DartElement
import org.dotlin.compiler.dart.element.DartPropertyElement
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.CLASSIFIERS_MASK
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.VARIABLES_MASK
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.utils.Printer

class DartSyntheticAnnotationScope(
    private val owner: DeclarationDescriptor,
    private val scope: MemberScope,
    private val context: DartDescriptorContext,
) : MemberScope {
    private val storageManager = context.storageManager

    private val annotationNames by storageManager.createLazyValue {
        getContributedDescriptors()
            .filterIsInstance<DartDescriptor>()
            .filter { it.element.canBeAnnotation }
            .map { (it as DeclarationDescriptor).name }
            .toSet()
    }

    override fun getClassifierNames(): Set<Name> = annotationNames

    override fun getContributedClassifier(name: Name, location: LookupLocation): ClassifierDescriptor? {
        val descriptor = scope.getContributedClassifier(name, location) ?: scope.getContributedVariables(name, location)
            .singleOrNull()

        return descriptor?.let { toAnnotationDescriptor(it) }
    }

    private val getAnnotations =
        storageManager.createMemoizedFunction<(Name) -> Boolean, List<DeclarationDescriptor>> { nameFilter ->
            scope.getContributedDescriptors(
                DescriptorKindFilter(CLASSIFIERS_MASK and VARIABLES_MASK),
                nameFilter
            ).mapNotNull { toAnnotationDescriptor(it) }
        }

    override fun getContributedDescriptors(
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean
    ): Collection<DeclarationDescriptor> = when {
        kindFilter.acceptsKinds(CLASSIFIERS_MASK) -> getAnnotations(nameFilter)
        else -> emptyList()
    }

    private val toAnnotationDescriptor =
        storageManager.createMemoizedFunctionWithNullableValues<DeclarationDescriptor, DartSyntheticAnnotationClassDescriptor> {
            when {
                it is DartDescriptor && it.element.canBeAnnotation -> DartSyntheticAnnotationClassDescriptor(
                    from = it,
                    owner,
                    context.storageManager
                )

                else -> null
            }
        }

    private val DartElement.canBeAnnotation: Boolean
        get() = when (this) {
            is DartClassElement -> constructors.any { it.isConst }
            is DartPropertyElement -> isConst
            else -> false
        }

    override fun printScopeStructure(p: Printer) = p.printScope()

    override fun getContributedFunctions(name: Name, location: LookupLocation) = emptyList<SimpleFunctionDescriptor>()
    override fun getContributedVariables(name: Name, location: LookupLocation) = emptyList<PropertyDescriptor>()
    override fun getFunctionNames(): Set<Name> = emptySet()
    override fun getVariableNames(): Set<Name> = emptySet()
}