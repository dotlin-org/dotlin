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

package org.dotlin.compiler.backend.descriptors.export

import org.dotlin.compiler.backend.descriptors.DartDescriptorContext
import org.dotlin.compiler.backend.descriptors.DartMemberScope
import org.dotlin.compiler.backend.descriptors.printScope
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.utils.Printer

class DartExportScope(
    private val owner: DartExportPackageFragmentDescriptor,
    private val context: DartDescriptorContext,
    private val scope: DartMemberScope,
) : MemberScope {
    private val isNameExported = context.storageManager.createMemoizedFunction<Name, Boolean> {
        val identifier = it.identifierOrNullIfSpecial ?: return@createMemoizedFunction false

        val shownNames = owner.element.show
        val hiddenNames = owner.element.hide

        val shown = shownNames.isNotEmpty() && identifier in shownNames
        val hidden = hiddenNames.isNotEmpty() && identifier in hiddenNames

        (shownNames.isEmpty() && hiddenNames.isEmpty()) || (shown && !hidden)
    }

    private fun Name.isExported(): Boolean = isNameExported(this)

    private val filteredClassifierNames by context.storageManager.createLazyValue {
        scope.getClassifierNames().filter { it.isExported() }.toSet()
    }

    private val filteredFunctionNames by context.storageManager.createLazyValue {
        scope.getFunctionNames().filter { it.isExported() }.toSet()
    }

    private val filteredVariableNames by context.storageManager.createLazyValue {
        scope.getVariableNames().filter { it.isExported() }.toSet()
    }

    override fun getClassifierNames(): Set<Name> = filteredClassifierNames
    override fun getFunctionNames(): Set<Name> = filteredFunctionNames
    override fun getVariableNames(): Set<Name> = filteredVariableNames

    override fun getContributedClassifier(name: Name, location: LookupLocation): ClassifierDescriptor? =
        when {
            name.isExported() -> scope.getContributedClassifier(name, location)
            else -> null
        }

    override fun getContributedDescriptors(
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean
    ): Collection<DeclarationDescriptor> =
        scope.getContributedDescriptors(kindFilter, nameFilter).filter { it.name.isExported() }

    override fun getContributedFunctions(name: Name, location: LookupLocation): Collection<SimpleFunctionDescriptor> =
        when {
            name.isExported() -> scope.getContributedFunctions(name, location)
            else -> emptyList()
        }

    override fun getContributedVariables(name: Name, location: LookupLocation): Collection<PropertyDescriptor> =
        when {
            name.isExported() -> scope.getContributedVariables(name, location)
            else -> emptyList()
        }

    override fun printScopeStructure(p: Printer) = p.printScope()
}