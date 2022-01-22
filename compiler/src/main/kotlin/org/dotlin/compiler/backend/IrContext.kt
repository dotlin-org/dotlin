package org.dotlin.compiler.backend

import org.dotlin.compiler.backend.util.runWith
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartPrefixedIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.fileOrNull
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

interface IrContext {
    val symbolTable: SymbolTable
    val dartNameGenerator: DartNameGenerator

    /**
     * If this class has a `@DartImplementationOf` annotation, this will be the value of
     * the corresponding Dart interface of this implementation.
     */
    val IrClass.correspondingDartInterface: IrClass?
        get() = when {
            hasDartImplementationOfAnnotation() -> {
                val (packageName, topLevelName) = dartImplementationFqName!!.let {
                    var splitIndex: Int? = null
                    for (i in it.indices) {
                        val current = it[i]
                        val next = it.getOrNull(i + 1)

                        if (current == '.' && next?.isUpperCase() == true) {
                            splitIndex = i
                            break
                        }
                    }

                    when (splitIndex) {
                        null -> it.split('.').let { split ->
                            split.dropLast(1).joinToString("") to split.last()
                        }
                        else -> {
                            it.substring(0, splitIndex) to it.substring(splitIndex + 1)
                        }
                    }
                }
                val descriptor = fileOrNull?.module?.descriptor?.findClassAcrossModuleDependencies(
                    ClassId(FqName(packageName), Name.identifier(topLevelName))
                ) ?: throw IllegalStateException("Corresponding Dart interface not found: $packageName.$topLevelName")

                symbolTable.referenceClass(descriptor).owner
            }
            else -> null
        }

    val IrClass.correspondingDartInterfaceOrSelf: IrClass
        get() = correspondingDartInterface ?: this

    val IrClass.companionObject: IrClass?
        get() = correspondingDartInterfaceOrSelf.companionObject()

    val IrDeclarationWithName.dartName: DartIdentifier
        get() = dartNameGenerator.runWith(this) { dartNameOf(it) }

    val IrDeclarationWithName.dartNameOrNull: DartIdentifier?
        get() = dartNameGenerator.runWith(this) { dartNameOrNullOf(it) }

    val IrDeclarationWithName.dartNameAsSimple: DartSimpleIdentifier
        get() = dartNameGenerator.runWith(this) { dartNameAsSimpleOf(it) }

    val IrDeclarationWithName.dartNameAsSimpleOrNull: DartSimpleIdentifier?
        get() = dartNameGenerator.runWith(this) { dartNameAsSimpleOrNullOf(it) }

    /**
     * The [dartName] for this declaration. If it's a [DartPrefixedIdentifier], the prefix is removed.
     */
    val IrDeclarationWithName.simpleDartName: DartSimpleIdentifier
        get() = dartNameGenerator.runWith(this) { simpleDartNameOf(it) }

    val IrDeclarationWithName.simpleDartNameOrNull: DartSimpleIdentifier?
        get() = dartNameGenerator.runWith(this) { simpleDartNameOrNullOf(it) }

    // Some IR elements can be asserted that they always have simple identifiers.
    val IrValueDeclaration.dartName: DartSimpleIdentifier
        get() = dartNameAsSimple

    val IrField.dartName: DartSimpleIdentifier
        get() = dartNameAsSimple

    val IrConstructor.dartName: DartSimpleIdentifier
        get() = dartNameAsSimple

    val IrConstructor.dartNameOrNull: DartSimpleIdentifier?
        get() = dartNameAsSimpleOrNull

    fun IrTypeParameter.dartNameValueWith(superTypes: Boolean) =
        dartNameGenerator.run { dartNameValueWith(superTypes) }

    // Annotation utils
    val IrDeclaration.dartHiddenNameFromCore: String?
        get() = when {
            hasDartHideNameFromCoreAnnotation() -> (this as? IrDeclarationWithName)?.simpleDartNameOrNull?.value
            else -> null
        }
}