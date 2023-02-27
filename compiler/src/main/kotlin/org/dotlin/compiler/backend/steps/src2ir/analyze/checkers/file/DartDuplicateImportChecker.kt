package org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.file

import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.DUPLICATE_IMPORT
import org.dotlin.compiler.backend.util.descriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingTrace

/**
 * Checks whether the same Dart declarations are imported through multiple imports. This can happen if for example
 * a class is imported through a Dart `export` directive and also imported through the file it was declared in.
 */
class DartDuplicateImportChecker(private val trace: BindingTrace) : SourceFileChecker {
    override fun check(file: KtFile) {
        val descriptorsByImports = file.importDirectives.associateWith {
           it.descriptor(trace.bindingContext)
        }

        for ((import, descriptor) in descriptorsByImports) {
            if (descriptor == null) continue

            val otherImport = descriptorsByImports.entries
                .firstOrNull { (i, d) -> i != import && d == descriptor }
                ?.key
                ?: continue

            trace.report(DUPLICATE_IMPORT.on(import, descriptor, otherImport))
        }
    }
}