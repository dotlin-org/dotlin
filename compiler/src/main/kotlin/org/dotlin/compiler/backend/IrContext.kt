package org.dotlin.compiler.backend

import org.dotlin.compiler.backend.attributes.IrAttributes
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.ir.element.IrIfNullExpression
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.isDartBool
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.isDartNumberPrimitive
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.isDartString
import org.dotlin.compiler.backend.util.*
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartPrefixedIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.jetbrains.kotlin.backend.common.ir.isTopLevel
import org.jetbrains.kotlin.backend.jvm.ir.psiElement
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.*
import org.jetbrains.kotlin.ir.types.IrDynamicType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.impl.IrDynamicTypeImpl
import org.jetbrains.kotlin.ir.types.toKotlinType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.bindingContextUtil.getAbbreviatedTypeOrType
import org.jetbrains.kotlin.types.Variance
import java.nio.file.Path

abstract class IrContext : IrAttributes {
    abstract val bindingContext: BindingContext
    abstract val symbolTable: SymbolTable
    abstract val irBuiltIns: IrBuiltIns
    abstract val dartNameGenerator: DartNameGenerator

    abstract val dartProject: DartProject

    lateinit var currentFile: IrFile
        private set

    fun enterFile(file: IrFile) {
        currentFile = file
    }

    val dynamicType: IrDynamicType = IrDynamicTypeImpl(null, emptyList(), Variance.INVARIANT)

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
        getTwoAnnotationArgumentsOf<String, Boolean>(dotlin.DartLibrary)
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

    fun IrExpression.isDartConst(
        initializedIn: IrDeclaration?,
        constInlineContainer: IrSimpleFunction? = null
    ): Boolean = isDartConst(implicit = initializedIn?.isDartConst() == true, constInlineContainer)

    fun IrExpression.isDartConst(
        implicit: Boolean = false,
        constInlineContainer: IrSimpleFunction? = null
    ): Boolean {
        fun IrFunctionAccessExpression.areArgumentsDartConst() = valueArguments.all {
            it == null || it.isDartConst(
                implicit,
                constInlineContainer
            )
        }

        fun IrCall.isOperatorCallOnConstPrimitives(): Boolean {
            if (!areArgumentsDartConst()) {
                return false
            }

            val receiver = receiver
            if (receiver != null && !receiver.isDartConst(implicit, constInlineContainer)) return false

            val types = listOfNotNull(receiver?.type) + valueArguments.mapNotNull { it?.type }

            return when {
                types.all { it.isDartNumberPrimitive(orNullable = true) } -> when (origin) {
                    EQEQ, PLUS, UPLUS, MINUS, UMINUS, MUL, DIV -> true
                    else -> false
                }
                types.all { it.isDartString(orNullable = true) } -> when (origin) {
                    EQEQ, PLUS -> true
                    else -> false
                }
                types.all { it.isDartBool(orNullable = true) } -> when (origin) {
                    EQEQ, OROR, ANDAND -> true
                    else -> false
                }
                else -> false
            }
        }

        val needConst by lazy { (implicit || hasAnnotation(dotlin.const)) }

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
                                isConst = isConst && element.isDartConst(implicit, constInlineContainer)
                            }

                            element.acceptChildren(this, null)
                        }

                    },
                    data = null
                )

                isConst
            }
            is IrFunctionAccessExpression -> {
                val invoked = symbol.owner

                when {
                    needConst && invoked.isDartConst() && areArgumentsDartConst() -> true
                    this is IrConstructorCall -> {
                        val parentClass = invoked.parentAsClass
                        when {
                            // Some classes are always to be const constructed.
                            parentClass.isDartConst() -> true
                            // If the argument(s) in the $Return are all const, make it const.
                            parentClass.defaultType.isDotlinReturn() -> areArgumentsDartConst()
                            else -> false
                        }
                    }
                    this is IrCall -> {
                        invoked as IrSimpleFunction
                        when (origin) {
                            GET_PROPERTY, GET_LOCAL_PROPERTY ->
                                invoked.correspondingProperty?.isDartConst() == true
                            else -> isOperatorCallOnConstPrimitives()
                        }
                    }
                    else -> false
                }
            }
            is IrTypeOperatorCall -> argument.isDartConst(implicit, constInlineContainer)
            is IrStringConcatenation -> arguments.all { it.isDartConst(implicit, constInlineContainer) }
            is IrGetValue -> {
                val owner = symbol.owner

                when {
                    owner.isDartConst() -> true
                    // In `const inline` functions, parameter references are considered const. They will be made
                    // non-const in the final Dart output, but will be const-inlined when the parent function is
                    // called.
                    owner is IrValueParameter && owner.parent == constInlineContainer -> true
                    else -> false
                }
            }
            is IrFunctionExpression -> needConst && run {
                var containsNonGlobalReference = false

                function.body?.acceptChildrenVoid(
                    object : IrCustomElementVisitorVoid {
                        override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)

                        override fun visitDeclarationReference(expression: IrDeclarationReference) {
                            if (!expression.isAccessibleInDartConstLambda(function)) {
                                containsNonGlobalReference = true
                            }

                            if (!containsNonGlobalReference) {
                                super.visitDeclarationReference(expression)
                            }
                        }
                    }
                )

                !containsNonGlobalReference
            }
            else -> false
        }
    }


    fun IrDeclarationReference.isAccessibleInDartConstLambda(
        function: IrFunction
    ): Boolean {
        if (isDartConst()) {
            return true
        }

        // For instance members, we check the instance (receiver) itself.
        if (this is IrMemberAccessExpression<*>) {
            receiver?.let {
                return it is IrDeclarationReference && it.isAccessibleInDartConstLambda(function)
            }
        }

        val declaration = symbol.owner as? IrDeclaration ?: return false

        return declaration.run {
            val parent = this.parent
            parent == function || isTopLevel || isDartStatic ||
                    (parent is IrClass && parent.isObject && !parent.isAnonymousObject)
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