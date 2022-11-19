package org.dotlin.compiler.backend.steps.src2ir.analyze.ir.checkers

import org.dotlin.compiler.backend.steps.ir2ast.ir.isFakeOverride
import org.dotlin.compiler.backend.steps.ir2ast.ir.isOriginalFunctionOrProperty
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.DUPLICATE_ENUM_MEMBER_NAME
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.VAR_IN_ENUM
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrAnalyzerContext
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrDeclarationChecker
import org.jetbrains.kotlin.backend.jvm.ir.psiElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedDeclaration

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object EnumChecker : IrDeclarationChecker {
    override val reports = listOf(VAR_IN_ENUM, DUPLICATE_ENUM_MEMBER_NAME)

    override fun IrAnalyzerContext.check(source: KtDeclaration, declaration: IrDeclaration) {
        if (declaration !is IrClass || !declaration.isEnumClass) return

        val uniqueEnumEntryMembers = mutableListOf<IrDeclaration>()

        for (member in declaration.declarations) {
            if (member.isFakeOverride()) continue

            if (member is IrProperty && member.isVar) {
                trace.report(VAR_IN_ENUM.on(member.psiElement ?: source))
                continue
            }

            if (member is IrEnumEntry) {
                val anonymousClass = member.correspondingClass ?: continue
                uniqueEnumEntryMembers += anonymousClass.declarations.filter { it.isOriginalFunctionOrProperty() }
            }
        }

        uniqueEnumEntryMembers.associateWith { it.dartNameOrNull }.let {
            val names = it.values
            it.forEach { (member, name) ->
                if (names.count { n -> n == name } > 1) {
                    val reportOn = (member.psiElement as? KtNamedDeclaration)?.nameIdentifier ?: source
                    trace.report(DUPLICATE_ENUM_MEMBER_NAME.on(reportOn))
                }
            }
        }

        // TODO: Check if all members are const

    }
}