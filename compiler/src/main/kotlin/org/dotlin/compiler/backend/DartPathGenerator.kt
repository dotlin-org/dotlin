/*
 * Copyright 2023 Wilko Manger
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

import org.dotlin.compiler.backend.descriptors.DartDescriptor
import org.dotlin.compiler.backend.descriptors.dartPackageFragment
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.relativeTo

object DartPathGenerator {
    /**
     * Dotlin-generated Dart file extension, without dot.
     */
    const val FILE_EXTENSION = "dt.g.dart"

    private val Path.dartFileName: String
        get() = when (extension) {
            "kt" -> fileName.toString().foldIndexed(initial = "") { index, acc, char ->
                acc + when {
                    index != 0 && char.isUpperCase() && !acc.last().isUpperCase() -> "_$char"
                    else -> char.toString()
                }
            }.lowercase().replace(Regex("\\.kt$"), ".${FILE_EXTENSION}")

            else -> fileName.toString()
        }


    /**
     * The path for the (eventually) generated Dart file. The path is relative to its source root.
     *
     * The original Kotlin file name is transformed to snake case, and the `.kt` extension is replaced
     * with `.dt.g.dart`.
     */
    context(IrContext)
    private fun Path.dartPath(isInCurrentModule: Boolean): Path {
        val fileName = dartFileName

        val relativeParentPath = when {
            isInCurrentModule -> relativeTo(dartProject.path).parent
            else -> parent // File paths are always serialized as relative to their project root.
        } ?: Path("")

        return relativeParentPath.resolve(fileName)
    }


    /**
     * The path for the (eventually) generated Dart file. The path is relative to the [currentFile]. Will be empty
     * if this file is the [currentFile].
     *
     * The original Kotlin file name is transformed to snake case, and the `.kt` extension is replaced with `.g.dart`.
     *
     * @receiver Must be a Dart path already.
     * @param other The other Dart path.
     */
    context(IrContext)
    private fun Path.relativeDartPathTo(other: Path): Path = other.parent?.let { relativeTo(it) } ?: this

    /**
     * The path for the (eventually) generated Dart file. The path is relative to its source root.
     *
     * The original Kotlin file name is transformed to snake case, and the `.kt` extension is replaced
     * with `.dt.g.dart`.
     */
    context(IrContext)
    val IrFile.dartPath: Path
        get() = Path(path).dartPath(isInCurrentModule)

    /**
     * The path for the (eventually) generated Dart file. The path is relative to the `currentFile`. Will be empty
     * if this file is the `currentFile`.
     *
     * The original Kotlin file name is transformed to snake case, and the `.kt` extension is replaced with `.g.dart`.
     */
    context(IrContext)
    val IrFile.relativeDartPath: Path
        get() = dartPath.relativeDartPathTo(currentFile.dartPath)

    /**
     * The path for the Dart file. The path is relative to its source root.
     */
    context(IrContext)
    val IrExternalPackageFragment.dartPath: Path
        get() = packageFragmentDescriptor.dartPackageFragment.dartPath

    /**
     * The path for the Dart file. The path is relative to the `currentFile`. Will be empty
     * if this file is the `currentFile`.
     */
    context(IrContext)
    val IrExternalPackageFragment.relativeDartPath: Path
        get() = packageFragmentDescriptor.dartPackageFragment.relativeDartPath

    /**
     * The path for the (eventually generated) Dart file. The path is relative to the `currentFile`. Will be empty
     * if this file is the `currentFile`.
     *
     * The original Kotlin file name is transformed to snake case, and the `.kt` extension is replaced with `.g.dart`.
     */
    context(IrContext)
    val IrPackageFragment.dartPath: Path
        get() = when (this) {
            is IrFile -> dartPath
            is IrExternalPackageFragment -> dartPath
            else -> throw UnsupportedOperationException("Unexpected IrPackageFragment: $this")
        }

    /**
     * The path for the (eventually generated) Dart file. The path is relative to the `currentFile`. Will be empty
     * if this file is the `currentFile`.
     *
     * The original Kotlin file name is transformed to snake case, and the `.kt` extension is replaced with `.g.dart`.
     */
    context(IrContext)
    val IrPackageFragment.relativeDartPath: Path
        get() = when (this) {
            is IrFile -> relativeDartPath
            is IrExternalPackageFragment -> relativeDartPath
            else -> throw UnsupportedOperationException("Unexpected IrPackageFragment: $this")
        }

    /**
     * The path for the Dart file. The path is relative to its source root.
     */
    context(IrContext)
    val <D> D.dartPath: Path where D : DartDescriptor, D : DeclarationDescriptor
        get() {
            val path = dartPackageFragment.library.path

            return when {
                // DartLibraryElements from our own project are serialized with a path relative to their package path.
                module.isCurrent -> dartProject.packagePath.resolve(path).relativeTo(dartProject.path)
                else -> path
            }
        }

    /**
     * The path for the Dart file. The path is relative to the `currentFile`. Will be empty
     * if this file is the `currentFile`.
     */
    context(IrContext)
    val <D> D.relativeDartPath: Path  where D : DartDescriptor, D : DeclarationDescriptor
        get() = dartPath.relativeDartPathTo(currentFile.dartPath)
}


