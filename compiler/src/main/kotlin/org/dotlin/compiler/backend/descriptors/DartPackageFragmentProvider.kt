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
import org.dotlin.compiler.backend.descriptors.annotation.DartInteropAnnotationPackageFragmentDescriptor
import org.dotlin.compiler.backend.descriptors.export.DartExportPackageFragmentDescriptor
import org.dotlin.compiler.backend.steps.src2ir.DartPackageDeserializer
import org.dotlin.compiler.dart.element.DartElementLocation
import org.dotlin.compiler.dart.element.DartLibraryElement
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentProviderOptimized
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance

class DartPackageFragmentProvider(
    private val project: DartProject,
    private val context: DartDescriptorContext,
) : PackageFragmentProviderOptimized {
    private val libraries: List<DartLibraryElement> by context.storageManager.createLazyValue {
        when (val pkg = DartPackageDeserializer.deserialize(
            project,
            context.pkg,
            context.elementLocator,
        ).single()) {
            null -> emptyList()
            else -> pkg.libraries
        }
    }

    private val fragments: List<PackageFragmentDescriptor> by context.storageManager.createLazyValue {
        val fragments = libraries.flatMap {
            it.units.map { unit ->
                DartPackageFragmentDescriptor(
                    library = it,
                    unit,
                    context,
                    annotations = Annotations.EMPTY, // TODO
                )
            }
        }

        val exportFragments = fragments.flatMap { fragment ->
            // First look in our own fragments, to prevent recursion (and it's faster).
            fun fragmentOf(loc: DartElementLocation) =
                fragments.firstOrNull { it.library.location == loc }
                    ?: context.fqNameOf(context.elementLocator.locate<DartLibraryElement>(loc))
                        .let { fqName ->
                            // TODO: Select based on some criteria?
                            context.module.getPackage(fqName).fragments.firstIsInstance()
                        }

            fragment.library.exports.map { export ->
                DartExportPackageFragmentDescriptor(
                    export,
                    context,
                    fragment,
                    exportedFragment = fragmentOf(export.exportLocation)
                )
            }
        }

        val syntheticAnnotationFragments = (fragments + exportFragments)
            .map { DartInteropAnnotationPackageFragmentDescriptor(it, context) }

        fragments + exportFragments + syntheticAnnotationFragments
    }

    private val getFragmentsOf =
        context.storageManager.createMemoizedFunction<FqName, List<PackageFragmentDescriptor>> { fqName ->
            when {
                fqName.startsWithPackageFqName() -> fragments.filter { it.fqName == fqName }
                else -> emptyList()
            }
        }

    @Deprecated("for usages use #packageFragments(FqName) at final point, for impl use #collectPackageFragments(FqName, MutableCollection<PackageFragmentDescriptor>)")
    override fun getPackageFragments(fqName: FqName): List<PackageFragmentDescriptor> = getFragmentsOf(fqName)
    override fun getSubPackagesOf(fqName: FqName, nameFilter: (Name) -> Boolean): Collection<FqName> {
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

    override fun isEmpty(fqName: FqName): Boolean =
        !fqName.startsWithPackageFqName() || fragments.none { it.fqName == fqName }

    private fun FqName.startsWithPackageFqName() = toString().startsWith(context.pkg.fqName.toString())
}