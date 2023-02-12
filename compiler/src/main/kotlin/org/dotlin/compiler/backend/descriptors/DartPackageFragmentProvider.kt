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

import org.dotlin.compiler.backend.DartProject
import org.dotlin.compiler.backend.steps.src2ir.DartPackageDeserializer
import org.dotlin.compiler.dart.element.DartLibraryElement
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentProviderOptimized
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class DartPackageFragmentProvider(
    private val project: DartProject,
    private val context: DartDescriptorContext,
) : PackageFragmentProviderOptimized {
    private val libraries: List<DartLibraryElement> by lazy {
        when (val pkg = DartPackageDeserializer.deserialize(
            project,
            context.pkg,
            context.elementLocator,
        ).single()) {
            null -> emptyList()
            else -> pkg.libraries
        }
    }

    private val fragments by lazy {
        libraries.flatMap {
            it.units.map { unit ->
                DartPackageFragmentDescriptor(
                    library = it,
                    unit,
                    context,
                    annotations = Annotations.EMPTY, // TODO
                )
            }
        }
    }

    private val getFragmentsOf =
        context.storageManager.createMemoizedFunction<FqName, List<DartPackageFragmentDescriptor>> { fqName ->
            when {
                fqName.startsWithPackageFqName() -> fragments.filter { it.fqName == fqName }
                else -> emptyList()
            }
        }

    @Deprecated("for usages use #packageFragments(FqName) at final point, for impl use #collectPackageFragments(FqName, MutableCollection<PackageFragmentDescriptor>)")
    override fun getPackageFragments(fqName: FqName): List<PackageFragmentDescriptor> = getFragmentsOf(fqName)
    override fun getSubPackagesOf(fqName: FqName, nameFilter: (Name) -> Boolean): Collection<FqName> {
        // TODO: A module can contain multiple fqnames
        if (!fqName.startsWithPackageFqName()) {
            return emptyList()
        }

        return fragments.asSequence()
            .map { it.fqName }
            .filter { it.parent() == fqName && nameFilter(it.shortName()) }
            .toList()
    }

    override fun collectPackageFragments(
        fqName: FqName,
        packageFragments: MutableCollection<PackageFragmentDescriptor>
    ) {
        packageFragments.addAll(getFragmentsOf(fqName))
    }

    override fun isEmpty(fqName: FqName): Boolean = !fqName.startsWithPackageFqName() // TODO: Can be more precise

    private fun FqName.startsWithPackageFqName() = toString().startsWith(context.pkg.fqName.toString())
}