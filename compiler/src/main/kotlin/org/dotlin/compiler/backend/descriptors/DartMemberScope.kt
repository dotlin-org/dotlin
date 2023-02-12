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
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.CALLABLES
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.CLASSIFIERS
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.FUNCTIONS
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.NON_SINGLETON_CLASSIFIERS
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.PACKAGES
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.SINGLETON_CLASSIFIERS
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.TYPE_ALIASES
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.VALUES
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.VARIABLES
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.utils.Printer


class DartMemberScope(
    private val owner: DeclarationDescriptor,
    private val context: DartDescriptorContext,
    private val elements: List<DartDeclarationElement> = emptyList(),
) : MemberScope {
    private val storageManager = context.storageManager

    private data class GetDescriptorArgs(
        val kindFilter: DescriptorKindFilter,
        val nameFilter: (Name) -> Boolean,
        val lookupLocation: LookupLocation? = null
    )

    private data class SpecificNameFilter(private val name: Name) : (Name) -> Boolean {
        override fun invoke(name: Name): Boolean = name == this.name
    }

    private val getDescriptors =
        storageManager.createMemoizedFunction<GetDescriptorArgs, List<DartDeclarationDescriptor>> { (kindFilter, nameFilter, lookupLocation) ->
            elements
                .asSequence()
                .filter {
                    when (kindFilter) {
                        ALL -> true
                        CALLABLES -> it is DartFunctionElement
                        NON_SINGLETON_CLASSIFIERS -> false // TODO
                        SINGLETON_CLASSIFIERS -> false // TODO
                        TYPE_ALIASES -> false // TODO
                        CLASSIFIERS -> false // TODO
                        PACKAGES -> false // TODO?
                        FUNCTIONS -> it is DartFunctionElement
                        VARIABLES -> it is DartFieldElement // TODO
                        VALUES -> false // TODO
                        else -> false
                    }
                }
                .map { toDescriptor(it) }
                .filter { nameFilter(it.name) }
                .toList()
        }

    private val toDescriptor =
        storageManager.createMemoizedFunction<DartDeclarationElement, DartDeclarationDescriptor> {
            when (it) {
                //is DartFieldElement ->
                is DartClassElement -> DartClassDescriptor(
                    element = it,
                    context,
                    container = owner,
                )

                is DartFunctionElement -> DartSimpleFunctionDescriptor(
                    element = it,
                    context,
                    container = owner,
                )

                is DartConstructorElement -> DartConstructorDescriptor(
                    element = it,
                    context,
                    container = owner as DartClassDescriptor,
                )

                else -> throw UnsupportedOperationException("Unsupported element: $it")
            }
        }

    override fun getClassifierNames(): Set<Name> = getDescriptors(
        GetDescriptorArgs(
            kindFilter = ALL,
            MemberScope.ALL_NAME_FILTER
        )
    ).map { it.name }.toSet()

    override fun getContributedClassifier(name: Name, location: LookupLocation): ClassifierDescriptor? {
        return getDescriptors(
            GetDescriptorArgs(kindFilter = ALL, SpecificNameFilter(name))
        ).firstOrNull() as? ClassifierDescriptor
    }

    override fun getContributedDescriptors(
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean
    ): Collection<DeclarationDescriptor> = getDescriptors(
        GetDescriptorArgs(kindFilter, nameFilter)
    )

    override fun getContributedFunctions(name: Name, location: LookupLocation): Collection<SimpleFunctionDescriptor> =
        getDescriptors(
            GetDescriptorArgs(
                kindFilter = FUNCTIONS,
                SpecificNameFilter(name),
                lookupLocation = null
            )
        ).map { it as SimpleFunctionDescriptor }

    override fun getContributedVariables(name: Name, location: LookupLocation): Collection<PropertyDescriptor> =
        getDescriptors(
            GetDescriptorArgs(
                kindFilter = VARIABLES,
                SpecificNameFilter(name),
                lookupLocation = null
            )
        ).map { it as PropertyDescriptor }

    override fun getFunctionNames(): Set<Name> =
        getDescriptors(
            GetDescriptorArgs(
                kindFilter = FUNCTIONS,
                MemberScope.ALL_NAME_FILTER
            )
        ).map { it.name }.toSet()

    override fun getVariableNames(): Set<Name> =
        getDescriptors(
            GetDescriptorArgs(
                kindFilter = VARIABLES,
                MemberScope.ALL_NAME_FILTER
            )
        ).map { it.name }.toSet()

    override fun printScopeStructure(p: Printer) {
        // TODO
    }

}