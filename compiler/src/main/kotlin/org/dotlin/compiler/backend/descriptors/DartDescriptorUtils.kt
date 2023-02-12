package org.dotlin.compiler.backend.descriptors

import org.dotlin.compiler.backend.DartPackage
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