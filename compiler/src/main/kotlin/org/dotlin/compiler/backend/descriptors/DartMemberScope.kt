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

package org.dotlin.compiler.backend.descriptors

import org.dotlin.compiler.dart.element.*
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.ALL
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.CLASSIFIERS_MASK
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.FUNCTIONS_MASK
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.VARIABLES_MASK
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.utils.Printer


class DartMemberScope(
    private val owner: DeclarationDescriptor,
    private val context: DartDescriptorContext,
    private val elements: List<DartDeclarationElement> = emptyList(),
) : MemberScope {
    private val storageManager = context.storageManager

    val classElementsByKotlinName by storageManager.createLazyValue {
        elements.filterIsInstance<DartClassElement>().associateBy { it.kotlinName }
    }

    val functionElementsByKotlinName by storageManager.createLazyValue {
        elements.filterIsInstance<DartExecutableElement>().associateBy { it.kotlinName }
    }

    val variableElementsByKotlinName by storageManager.createLazyValue {
        elements.filterIsInstance<DartVariableElement>().associateBy { it.kotlinName }
    }

    override fun getClassifierNames(): Set<Name> = classElementsByKotlinName.keys
    override fun getFunctionNames(): Set<Name> = functionElementsByKotlinName.keys
    override fun getVariableNames(): Set<Name> = variableElementsByKotlinName.keys

    private val relevantElementsByKotlinName =
        storageManager.createMemoizedFunction<DescriptorKindFilter, Map<Name, DartDeclarationElement>> {
            val maps = mutableListOf<Map<Name, DartDeclarationElement>>()

            if (it.acceptsKinds(CLASSIFIERS_MASK)) {
                maps.add(classElementsByKotlinName)
            }

            if (it.acceptsKinds(FUNCTIONS_MASK)) {
                maps.add(functionElementsByKotlinName)
            }

            if (it.acceptsKinds(VARIABLES_MASK)) {
                maps.add(variableElementsByKotlinName)
            }

            when {
                maps.isNotEmpty() -> maps.reduce { acc, map -> acc + map }
                else -> emptyMap()
            }
        }

    private data class GetDescriptorsRequest(
        val kindFilter: DescriptorKindFilter,
        val nameFilter: (Name) -> Boolean,
    )

    private val getDescriptors =
        storageManager.createMemoizedFunction<GetDescriptorsRequest, List<DeclarationDescriptor>> { (kindFilter, nameFilter) ->
            val elementsByName = relevantElementsByKotlinName(kindFilter)

            elementsByName.entries
                .filter { (name, _) -> nameFilter(name) }
                .map { (_, element) -> toDescriptor(element) }
        }


    private data class GetDescriptorRequest(
        val name: Name? = null,
        val kindFilter: DescriptorKindFilter = ALL,
        /**
         * If the element is already available, it's quicker to pass it directly.
         */
        val element: DartDeclarationElement? = null,
    )

    private val getDescriptor =
        storageManager.createMemoizedFunctionWithNullableValues<GetDescriptorRequest, DeclarationDescriptor> { (name, kindFilter, element) ->
            // If an element is passed, no need to search
            element?.let { return@createMemoizedFunctionWithNullableValues toDescriptor(it) }

            val elementsByName = relevantElementsByKotlinName(kindFilter)
            elementsByName[name]?.let { toDescriptor(it) }
        }

    private val toDescriptor = storageManager.createMemoizedFunction<DartDeclarationElement, DeclarationDescriptor> {
        when (it) {
            is DartClassElement -> DartClassDescriptor(
                element = it,
                context,
                container = owner,
            )

            is DartFunctionElement, is DartConstructorElement -> DartSimpleFunctionDescriptor(
                element = it as DartExecutableElement,
                context,
                container = owner,
            )

            is DartPropertyElement -> DartPropertyDescriptor(
                element = it,
                context,
                container = owner,
            )

            else -> throw UnsupportedOperationException("Unsupported element: $it")
        }
    }

    override fun getContributedClassifier(name: Name, location: LookupLocation): ClassifierDescriptor? =
        getDescriptor(GetDescriptorRequest(name)) as? ClassifierDescriptor

    override fun getContributedDescriptors(
        kindFilter: DescriptorKindFilter, nameFilter: (Name) -> Boolean
    ): Collection<DeclarationDescriptor> = getDescriptors(GetDescriptorsRequest(kindFilter, nameFilter))

    override fun getContributedFunctions(name: Name, location: LookupLocation): Collection<SimpleFunctionDescriptor> =
        // Since Dart does not support overloads, we know that we'll always have a single result.
        listOfNotNull(getDescriptor(GetDescriptorRequest(name)) as? SimpleFunctionDescriptor)

    override fun getContributedVariables(name: Name, location: LookupLocation): Collection<PropertyDescriptor> =
        // Since Dart does not support overloads, we know that we'll always have a single result.
        listOfNotNull(getDescriptor(GetDescriptorRequest(name)) as? PropertyDescriptor)

    override fun printScopeStructure(p: Printer) = p.printScope()

}