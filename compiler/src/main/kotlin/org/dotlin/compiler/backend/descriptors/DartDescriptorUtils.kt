package org.dotlin.compiler.backend.descriptors

import org.dotlin.compiler.backend.DartPackage
import org.dotlin.compiler.backend.descriptors.annotation.DartInteropAnnotationPackageFragmentDescriptor
import org.dotlin.compiler.backend.descriptors.export.DartExportPackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.name.FqName

/**
 * FQN of the Dart package itself.
 */
// TODO: There can be multiple fq names, if Kotlin code is present.
// TODO: When changing this, make it a member of DartDescriptor.
val DartPackage.fqName: FqName
    get() {
        val group = run {
            fun String.reverseDomain() = split(".").reversed().joinToString(".")

            when {
                publisher.isNotEmpty() -> publisher.reverseDomain()
                else -> repository?.host?.reverseDomain() ?: "pkg" // TODO: Better fallback?
            }
        }

        return FqName("$group.${name}")
    }

val DeclarationDescriptor.dartPackageFragment: DartPackageFragmentDescriptor
    get() = when (val container = containingPackageFragment) {
        is DartPackageFragmentDescriptor -> container
        is DartInteropAnnotationPackageFragmentDescriptor -> when (val fragment = container.fragment) {
            is DartPackageFragmentDescriptor -> fragment
            is DartExportPackageFragmentDescriptor -> fragment.fragment
            else -> throw UnsupportedOperationException("Unexpected fragment: $fragment")
        }

        is DartExportPackageFragmentDescriptor -> container.fragment
        else -> throw UnsupportedOperationException("Unexpected package fragment: $container")
    }

private val DeclarationDescriptor.containingPackageFragment: PackageFragmentDescriptor
    get() = when (this) {
        is PackageFragmentDescriptor -> this
        else -> containingDeclaration?.containingPackageFragment
            ?: throw UnsupportedOperationException("No package fragment found")
    }