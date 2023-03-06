package org.dotlin.compiler.backend.descriptors

import org.dotlin.compiler.backend.DartPackage
import org.dotlin.compiler.backend.steps.src2ir.DartElementLocator
import org.dotlin.compiler.dart.element.DartDeclarationElement
import org.dotlin.compiler.dart.element.DartInterfaceElement
import org.dotlin.compiler.dart.element.DartLibraryElement
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.storage.StorageManager
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

class DartDescriptorContext(
    val module: ModuleDescriptor,
    val pkg: DartPackage,
    val elementLocator: DartElementLocator,
    val storageManager: StorageManager,
) {
    private val fqNameOfLibrary = storageManager.createMemoizedFunction<DartLibraryElement, FqName> { element ->
        val fileFqName = element.path
            .dropWhile { it.name == "lib" }
            .joinToString(".") { it.nameWithoutExtension }

        when (fileFqName) {
            // package:meta/meta.dart -> dev.dart.meta
            pkg.name -> pkg.fqName
            // package:meta/something.dart -> dev.dart.meta.something
            else -> FqName("${pkg.fqName}.$fileFqName")
        }
    }

    fun fqNameOf(element: DartLibraryElement): FqName = fqNameOfLibrary(element)

    private val fqNameOfDeclaration = storageManager.createMemoizedFunction<DartDeclarationElement, FqName> { element ->
        val libraryFqName = fqNameOf(elementLocator.locate<DartLibraryElement>(element.location.library))

        val parentName = when (val parent = element.parent) {
            is DartInterfaceElement -> parent.kotlinName
            else -> null
        }

        FqName(listOfNotNull(libraryFqName, parentName, element.kotlinName).joinToString("."))
    }

    fun fqNameOf(element: DartDeclarationElement): FqName = fqNameOfDeclaration(element)
}