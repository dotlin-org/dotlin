package org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.file

import org.jetbrains.kotlin.psi.KtFile

interface SourceFileChecker {
    fun check(file: KtFile)
}