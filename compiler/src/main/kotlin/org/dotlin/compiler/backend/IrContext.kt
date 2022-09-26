package org.dotlin.compiler.backend

import org.dotlin.compiler.backend.steps.ir2ast.attributes.IrAttributes
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrCustomElementVisitor
import org.dotlin.compiler.backend.steps.ir2ast.ir.correspondingProperty
import org.dotlin.compiler.backend.steps.ir2ast.ir.element.IrIfNullExpression
import org.dotlin.compiler.backend.steps.ir2ast.ir.receiver
import org.dotlin.compiler.backend.steps.ir2ast.ir.valueArguments
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.isDartBool
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.isDartNumberPrimitive
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.isDartString
import org.dotlin.compiler.backend.util.*
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartPrefixedIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.jetbrains.kotlin.backend.jvm.ir.psiElement
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.*
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.toKotlinType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.bindingContextUtil.getAbbreviatedTypeOrType
import java.nio.file.Path

abstract class IrContext : IrAttributes {
    abstract val bindingContext: BindingContext
    abstract val symbolTable: SymbolTable
    abstract val irBuiltIns: IrBuiltIns
    abstract val dartNameGenerator: DartNameGenerator

    /**
     * The source root. This is a real, absolute path, meaning symlinks are resolved.
     */
    abstract val sourceRoot: Path

    abstract val dartPackage: DartPackage

    lateinit var currentFile: IrFile
        private set

    fun enterFile(file: IrFile) {
        currentFile = file
    }

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
    val IrFile.dartPath: Path
        get() = dartNameGenerator.runWith(this) { dartPathOf(it) }

    /**
     * The path for the (eventually) generated Dart file. The path is relative to the [currentFile]. Will be empty
     * if this file is the [currentFile].
     *
     * The original Kotlin file name is transformed to snake case, and the `.kt` extension is replaced with `.g.dart`.
     */
    val IrFile.relativeDartPath: Path
        get() = dartNameGenerator.runWith(this) { relativeDartPathOf(it) }

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
                        file.isInCurrentModule -> file.relativeDartPath.toString().let { importPath ->
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

    val isCurrentModuleBuiltIns: Boolean
        get() = currentFile.module.descriptor.let {
            it == it.builtIns.builtInsModule
        }

    fun KtAnnotationEntry.getFqName() = getFqName(bindingContext)

    fun IrExpression.hasAnnotation(fqName: FqName) = hasAnnotation(fqName, bindingContext)

    fun IrExpression.isDartConst(initializedIn: IrDeclaration?): Boolean =
        isDartConst(allowImplicit = initializedIn?.isDartConst() == true)

    fun IrExpression.isDartConst(allowImplicit: Boolean = false): Boolean {
        fun IrCall.isOperatorCallOnConstPrimitives(): Boolean {
            if (valueArgumentsCount > 0 && valueArguments.all { it?.isDartConst(allowImplicit) != true }) return false
            if (receiver != null && !receiver!!.isDartConst(allowImplicit)) return false

            val types = listOfNotNull(receiver?.type) + valueArguments.mapNotNull { it?.type }

            return when {
                types.all { it.isDartNumberPrimitive(orNullable = true) } -> when (origin) {
                    PLUS, UPLUS, MINUS, UMINUS, MUL, DIV -> true
                    else -> false
                }
                types.all { it.isDartString(orNullable = true) } -> when (origin) {
                    PLUS -> true
                    else -> false
                }
                types.all { it.isDartBool(orNullable = true) } -> when (origin) {
                    OROR, ANDAND -> true
                    else -> false
                }
                else -> false
            }
        }

        return when (this) {
            // Enums are always constructed as const.
            is IrGetEnumValue, is IrEnumConstructorCall -> true
            is IrConst<*> -> true
            is IrWhen, is IrIfNullExpression -> {
                var isConst = true

                acceptChildren(
                    object : IrCustomElementVisitor<Unit, Nothing?> {
                        override fun visitElement(element: IrElement, data: Nothing?) {
                            if (element is IrExpression) {
                                isConst = isConst && element.isDartConst(allowImplicit)
                            }

                            element.acceptChildren(this, null)
                        }

                    },
                    data = null
                )

                isConst
            }
            is IrConstructorCall -> {
                val constructor = symbol.owner
                val parentClass = constructor.parentAsClass

                when {
                    // If @const is optional, having the called constructor be const is enough.
                    allowImplicit && constructor.isDartConst() -> true
                    // Some classes are always to be const constructed.
                    parentClass.isDartConst() -> true
                    // If the argument(s) in the $Return are all const, make it const.
                    parentClass.defaultType.isDotlinReturn() -> {
                        valueArguments.all { it?.isDartConst(allowImplicit) == true }
                    }
                    else -> hasAnnotation(DotlinAnnotations.const)
                }
            }
            is IrTypeOperatorCall -> argument.isDartConst(allowImplicit)
            is IrCall -> when (origin) {
                GET_PROPERTY, GET_LOCAL_PROPERTY -> symbol.owner.correspondingProperty?.isDartConst() == true
                else -> isOperatorCallOnConstPrimitives()
            }
            else -> false
        }
    }

    // Super type utils
    private fun IrClass.superTypeSet(): Set<IrType> = defaultType.superTypes().toSet()
    fun IrClass.baseClass(): IrType {
        val classes = superTypeSet().filter { it.classOrNull?.owner?.isClass == true }
        val superImplicitInterfaces = superImplicitInterfaces()
        val superMixins = superMixins()

        return classes.singleOrNull { it !in superImplicitInterfaces && it !in superMixins } ?: irBuiltIns.anyType
    }

    fun IrClass.superInterfaces(): Set<IrType> =
        superTypeSet().filter { it.classOrNull?.owner?.isInterface == true }.toSet()

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun IrClass.superSpecialClasses(only: SuperTypeKind.Special): Set<IrType> {
        val ktClass = psiElement as? KtClass ?: return emptySet()
        val superTypeEntries = ktClass.getSuperTypeList()?.entries?.toList() ?: return emptySet()
        val ktTypes = superTypeEntries.mapNotNull {
            val typeReference = it.typeReference ?: return@mapNotNull null

            val isSpecialClass = typeReference.isSpecialInheritanceConstructorCall(
                bindingContext, mustBe = when (only) {
                    SuperTypeKind.Interface.Implicit -> SpecialInheritanceKind.IMPLICIT_INTERFACE
                    SuperTypeKind.Mixin -> SpecialInheritanceKind.MIXIN
                }
            )

            when {
                isSpecialClass -> typeReference.typeElement?.getAbbreviatedTypeOrType(bindingContext)
                else -> null
            }
        }

        return superTypes.filter { it.toKotlinType() in ktTypes }.toSet()
    }

    fun IrClass.superImplicitInterfaces() = superSpecialClasses(only = SuperTypeKind.Interface.Implicit)
    fun IrClass.allSuperImplicitInterfaces(): Set<IrType> =
        superImplicitInterfaces() union superTypes.flatMap {
            it.classOrNull?.owner?.allSuperImplicitInterfaces() ?: emptySet()
        }

    fun IrClass.superMixins() = superSpecialClasses(only = SuperTypeKind.Mixin)

    fun IrClass.superTypes(): Set<SuperType> {
        return setOf(SuperType(SuperTypeKind.Class, baseClass()))
            .plus(superInterfaces().map { SuperType(SuperTypeKind.Interface.Regular, it) })
            .plus(superImplicitInterfaces().map { SuperType(SuperTypeKind.Interface.Implicit, it) })
            .plus(superMixins().map { SuperType(SuperTypeKind.Mixin, it) })
            .toSet()
    }

    fun Iterable<SuperType>.baseClass() = firstOrNull { it.kind is SuperTypeKind.Class }?.type
    fun Iterable<SuperType>.interfaces() = filter { it.kind is SuperTypeKind.Interface }.types()
    fun Iterable<SuperType>.regularInterfaces() = filter { it.kind is SuperTypeKind.Interface.Regular }.types()
    fun Iterable<SuperType>.implicitInterfaces() = filter { it.kind is SuperTypeKind.Interface.Implicit }.types()
    fun Iterable<SuperType>.mixins() = filter { it.kind is SuperTypeKind.Mixin }.types()

    fun Iterable<SuperType>.types() = map { it.type }
}

data class DartUnresolvedImport(val library: String, val alias: String?, val hidden: Boolean)

sealed interface SuperTypeKind {
    /**
     * Special cases of super types: implicit interfaces or mixins.
     */
    sealed interface Special

    object Class : SuperTypeKind {
        override fun toString() = "SuperTypeKind.Class"
    }

    sealed interface Interface : SuperTypeKind {
        object Regular : Interface {
            override fun toString() = "SuperTypeKind.Interface.Regular"
        }

        object Implicit : Interface, Special {
            override fun toString() = "SuperTypeKind.Interface.Implicit"
        }
    }

    object Mixin : SuperTypeKind, Special {
        override fun toString() = "SuperTypeKind.Mixin"
    }
}

data class SuperType(val kind: SuperTypeKind, val type: IrType)