package org.dotlin.compiler.backend

import org.dotlin.compiler.backend.util.getTwoAnnotationArgumentsOf
import org.dotlin.compiler.backend.util.isActuallyExternal
import org.dotlin.compiler.backend.util.runWith
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartPrefixedIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import java.nio.file.Path

abstract class IrContext {
    abstract val symbolTable: SymbolTable
    abstract val dartNameGenerator: DartNameGenerator

    /**
     * The source root. This is a real, absolute path, meaning symlinks are resolved.
     */
    abstract val sourceRoot: Path

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

    /**
     * The path for the (eventually) generated Dart file. The path is relative to its source root ([sourceRoot] if it's
     * a file in the currently compiling module).
     *
     * The original Kotlin file name is transformed to snake case, and the `.kt` extension is replaced with `.g.dart`.
     */
    val IrFile.relativeDartPath: Path
        get() = dartNameGenerator.runWith(this) { dartRelativePathOf(it) }

    // Annotation utils
    val IrDeclaration.dartHiddenNameFromCore: String?
        get() = when {
            hasDartHideNameFromCoreAnnotation() -> (this as? IrDeclarationWithName)?.simpleDartNameOrNull?.value
            else -> null
        }

    // Import utils
    private val builtInImports = mapOf(
        "dart.core" to "dart:core",
        "dart.typeddata" to "dart:typed_data",
        "dart.math" to "dart:math"
    )

    private fun IrAnnotationContainer.unresolvedImportFromAnnotationFor(declaration: IrDeclarationWithName) =
        getTwoAnnotationArgumentsOf<String, Boolean>(DotlinAnnotations.dartLibrary)
            ?.let { (library, aliased) ->
                DartUnresolvedImport(
                    library,
                    alias = when {
                        aliased -> library.split(':')[1] // TODO: Improve for non Dart SDK imports.
                        else -> null
                    },
                    hidden = aliased
                )
            }

    private fun IrDeclarationWithName.getDartLibraryImport(): DartUnresolvedImport? {
        return when {
            isActuallyExternal -> unresolvedImportFromAnnotationFor(this) ?: when {
                // We don't want to look at the parent's class @DartLibrary annotation for companion objects,
                // because we never need to import an external companion object
                // (it's an error to use it as an instance instead of as static container).
                (this as? IrClass)?.isCompanion != true -> parentClassOrNull?.let {
                    // Aliases and hidings for class member imports are ignored.
                    it.unresolvedImportFromAnnotationFor(it)?.copy(alias = null, hidden = false)
                }
                else -> null
            } ?: when (this) {
                // Try to see if the file has a @DartLibrary annotation.
                !is IrFile -> fileOrNull?.unresolvedImportFromAnnotationFor(this)
                    ?: getPackageFragment()?.fqName?.let { fqName ->
                        // If the declaration belongs to a built-in package (e.g. `dart.math`), we know what to import.
                        builtInImports[fqName.asString()]?.let {
                            DartUnresolvedImport(
                                library = it,
                                alias = null,
                                hidden = false,
                            )
                        }
                    }
                else -> null
            }
            else -> fileOrNull?.let { file ->
                when {
                    file != currentFile -> when {
                        file.isInCurrentModule -> file.relativeDartPath.let { theirDartPath ->
                            val currentDartPath = currentFile.relativeDartPath
                            val importPath = currentDartPath.parent?.relativize(theirDartPath)?.toString()
                                ?: theirDartPath.toString()

                            when {
                                importPath.isNotBlank() -> DartUnresolvedImport(
                                    library = importPath,
                                    alias = null,
                                    hidden = false
                                )
                                else -> null
                            }
                        }
                        // TODO: Package imports
                        else -> null
                    }
                    else -> null
                }
            }
        }
    }

    val IrDeclaration.dartUnresolvedImport: DartUnresolvedImport?
        get() = (this as? IrDeclarationWithName)?.getDartLibraryImport()

    val IrDeclaration.dartLibrary: String?
        get() = dartUnresolvedImport?.library

    val IrDeclaration.dartLibraryAlias: String?
        get() = dartUnresolvedImport?.alias

    val IrFile.isInCurrentModule: Boolean
        get() = module == currentFile.module

    val IrDeclaration.isInCurrentModule: Boolean
        get() = fileOrNull?.isInCurrentModule == true
}

data class DartUnresolvedImport(val library: String, val alias: String?, val hidden: Boolean)