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
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.OverridingStrategy
import org.jetbrains.kotlin.resolve.OverridingUtil
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.ALL
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.CLASSIFIERS_MASK
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.FUNCTIONS_MASK
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.VARIABLES_MASK
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.types.typeUtil.supertypes
import org.jetbrains.kotlin.utils.Printer


class DartMemberScope(
    private val owner: DeclarationDescriptor,
    private val context: DartDescriptorContext,
    private val elements: List<DartDeclarationElement> = emptyList(),
) : MemberScope {
    private val storageManager = context.storageManager

    val classifierElementsByKotlinName by storageManager.createLazyValue {
        elements
            .filterIsInstance<DartInterfaceElement>()
            .plus(elements.filter { it is DartPropertyElement && it.isEnumConstant })
            .associateBy { it.kotlinName }
    }

    val functionElementsByKotlinName by storageManager.createLazyValue {
        elements.filterIsInstance<DartExecutableElement>().associateBy { it.kotlinName }
    }

    val variableElementsByKotlinName by storageManager.createLazyValue {
        elements.filterIsInstance<DartVariableElement>().associateBy { it.kotlinName }
    }

    override fun getClassifierNames(): Set<Name> = classifierElementsByKotlinName.keys
    override fun getFunctionNames(): Set<Name> = functionElementsByKotlinName.keys
    override fun getVariableNames(): Set<Name> = variableElementsByKotlinName.keys

    private val allNames by storageManager.createLazyValue {
        getClassifierNames() + getFunctionNames() + getVariableNames()
    }

    override fun definitelyDoesNotContainName(name: Name): Boolean = name !in allNames

    private val relevantElementsByKotlinName =
        storageManager.createMemoizedFunction<DescriptorKindFilter, Map<Name, DartDeclarationElement>> {
            val maps = mutableListOf<Map<Name, DartDeclarationElement>>()

            if (it.acceptsKinds(CLASSIFIERS_MASK)) {
                maps.add(classifierElementsByKotlinName)
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
        storageManager.createMemoizedFunctionWithNullableValues<GetDescriptorRequest, DeclarationDescriptor> getDescriptor@{ (name, kindFilter, element) ->
            // If an element is passed, no need to search
            element?.let { return@getDescriptor toDescriptor(it) }

            require(name != null) { "name must be passed if element not given" }

            val elementsByName = relevantElementsByKotlinName(kindFilter)
            val foundDescriptor = elementsByName[name]?.let { toDescriptor(it) }

            when {
                // Handle fake override.
                foundDescriptor == null && owner is ClassDescriptor -> {
                    val fromSuperTypes = owner.defaultType.supertypes()
                        .flatMap {
                            it.memberScope.run {
                                getContributedFunctions(name, NoLookupLocation.FROM_BACKEND) +
                                        getContributedVariables(name, NoLookupLocation.FROM_BACKEND)
                            }
                        }

                    if (fromSuperTypes.isEmpty()) return@getDescriptor null

                    var result: CallableMemberDescriptor? = null

                    OverridingUtil.DEFAULT.generateOverridesInFunctionGroup(
                        name,
                        fromSuperTypes,
                        emptyList(),
                        owner,
                        object : OverridingStrategy() {
                            override fun addFakeOverride(fakeOverride: CallableMemberDescriptor) {
                                when (result) {
                                    null -> result = fakeOverride
                                    else -> error("Fake override already set: $result (wants to become $fakeOverride)")
                                }
                            }

                            override fun inheritanceConflict(
                                first: CallableMemberDescriptor,
                                second: CallableMemberDescriptor
                            ) = error("Inheritance conflict: first: $first second: $second")

                            override fun overrideConflict(
                                fromSuper: CallableMemberDescriptor,
                                fromCurrent: CallableMemberDescriptor
                            ) = error("Override conflict: super: $fromSuper current: $fromCurrent")
                        }
                    )

                    OverridingUtil.resolveUnknownVisibilityForMember(result!!) {
                        error("Cannot infer visibility for: $it")
                    }

                    result
                }

                else -> foundDescriptor
            }
        }

    private val toDescriptor = storageManager.createMemoizedFunction<DartDeclarationElement, DeclarationDescriptor> {
        when (it) {
            is DartClassElement, is DartEnumElement -> DartClassDescriptor(
                element = it as DartInterfaceElement,
                context,
                container = owner,
            )

            is DartFunctionElement, is DartConstructorElement -> DartSimpleFunctionDescriptor(
                element = it as DartExecutableElement,
                context,
                container = owner,
            )

            is DartPropertyElement -> when {
                it.isEnumConstant -> DartEnumEntryDescriptor(
                    element = it,
                    context,
                    container = owner as ClassDescriptor,
                )

                else -> DartPropertyDescriptor(
                    element = it,
                    context,
                    container = owner,
                )
            }

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