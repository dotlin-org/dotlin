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

abstract class IrContext {
    abstract val symbolTable: SymbolTable
    abstract val dartNameGenerator: DartNameGenerator

    lateinit var currentFile: IrFile
        private set

    fun enterFile(file: IrFile) {
        currentFile = file
    }

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
        get() = dartNameGenerator.runWith(this) { dartNameOf(it, currentFile) }

    val IrDeclarationWithName.dartNameOrNull: DartIdentifier?
        get() = dartNameGenerator.runWith(this) { dartNameOrNullOf(it, currentFile) }

    val IrDeclarationWithName.dartNameAsSimple: DartSimpleIdentifier
        get() = dartNameGenerator.runWith(this) { dartNameAsSimpleOf(it, currentFile) }

    val IrDeclarationWithName.dartNameAsSimpleOrNull: DartSimpleIdentifier?
        get() = dartNameGenerator.runWith(this) { dartNameAsSimpleOrNullOf(it, currentFile) }

    /**
     * The [dartName] for this declaration. If it's a [DartPrefixedIdentifier], the prefix is removed.
     */
    val IrDeclarationWithName.simpleDartName: DartSimpleIdentifier
        get() = dartNameGenerator.runWith(this) { simpleDartNameOf(it, currentFile) }

    val IrDeclarationWithName.simpleDartNameOrNull: DartSimpleIdentifier?
        get() = dartNameGenerator.runWith(this) { simpleDartNameOrNullOf(it, currentFile) }

    /**
     * The [dartName] for this declaration. If it's a [DartPrefixedIdentifier], the prefix is removed.
     */
    val IrDeclarationWithName.simpleDartNameWithoutKotlinImportAlias: DartSimpleIdentifier
        get() = dartNameGenerator.runWith(this) { simpleDartNameOf(it, currentFile, useKotlinAlias = false) }

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
/*
    fun IrDeclarationWithName.dartNameIn(file: IrFile) =
        dartNameGenerator.runWith(this) { dartNameOf(it, file) }

    fun IrDeclarationWithName.dartNameOrNullIn(file: IrFile) =
        dartNameGenerator.runWith(this) { dartNameOrNullOf(it, file) }

    fun IrDeclarationWithName.dartNameAsSimpleIn(file: IrFile) =
        dartNameGenerator.runWith(this) { dartNameAsSimpleOf(it, file) }

    fun IrDeclarationWithName.dartNameAsSimpleOrNullIn(file: IrFile) =
        dartNameGenerator.runWith(this) { dartNameAsSimpleOrNullOf(it, file) }

    /**
     * The [dartName] for this declaration. If it's a [DartPrefixedIdentifier], the prefix is removed.
     */
    fun IrDeclarationWithName.simpleDartNameIn(file: IrFile) =
        dartNameGenerator.runWith(this) { simpleDartNameOf(it, file) }


    fun IrDeclarationWithName.simpleDartNameOrNullIn(file: IrFile) =
        dartNameGenerator.runWith(this) { simpleDartNameOrNullOf(it, file) }

    // Some IR elements can be asserted that they always have simple identifiers.
    fun IrValueDeclaration.dartNameIn(file: IrFile): DartSimpleIdentifier = dartNameAsSimpleIn(file)
    fun IrField.dartNameIn(file: IrFile): DartSimpleIdentifier = dartNameAsSimpleIn(file)
    fun IrConstructor.dartNameIn(file: IrFile): DartSimpleIdentifier = dartNameAsSimpleIn(file)
    fun IrConstructor.dartNameOrNullIn(file: IrFile): DartSimpleIdentifier? = dartNameAsSimpleOrNullIn(file)
 */