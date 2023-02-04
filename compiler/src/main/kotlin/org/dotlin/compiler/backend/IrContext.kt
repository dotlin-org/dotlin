package org.dotlin.compiler.backend

import org.dotlin.compiler.backend.attributes.IrAttributes
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
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
import org.jetbrains.kotlin.ir.types.IrDynamicType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.impl.IrDynamicTypeImpl
import org.jetbrains.kotlin.ir.types.toKotlinType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
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

    val IrDeclaration.dartNameOrNull: DartIdentifier?
        get() = (this as? IrDeclarationWithName)?.dartNameOrNull

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
     * The path for the (eventually) generated Dart file. The path is relative to its source root.
     *
     * The original Kotlin file name is transformed to snake case, and the `.kt` extension is replaced
     * with `.dt.g.dart`.
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
        constInlineContainer: IrSimpleFunction? = null,
        isArgument: Boolean = false,
    ): Boolean {
        fun IrFunctionAccessExpression.areArgumentsDartConst() = valueArguments.all {
            it == null || it.isDartConst(
                implicit,
                constInlineContainer,
                isArgument = true
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

        val needConst by lazy { (implicit || hasAnnotation(dotlin.const) || isDartConst) }

        return when (this) {
            // Enums are always constructed as const.
            is IrGetEnumValue, is IrEnumConstructorCall -> true
            is IrConst<*> -> true
            is IrWhen -> {
                var isConst = true

                acceptChildren(
                    object : IrElementVisitor<Unit, Nothing?> {
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
                    object : IrElementVisitorVoid {
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
            is IrVararg -> (isArgument || needConst) && elements.all {
                when (it) {
                    is IrExpression -> it.isDartConst(implicit, constInlineContainer)
                    is IrSpreadElement -> it.expression.isDartConst(implicit, constInlineContainer)
                    else -> false
                }
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
        if (!isInCurrentModule) {
            return superTypes
                .filter {
                    val specialInheritedAnnotation = it.annotations.firstOrNull { annotation ->
                        annotation.isAnnotationWithEqualFqName(dotlin.intrinsics.SpecialInheritedType)
                    }

                    when (specialInheritedAnnotation) {
                        null -> false
                        else -> {
                            val arg = specialInheritedAnnotation.getValueArgument(0)

                            when {
                                arg is IrConst<*> && arg.value == only.toString() -> true
                                else -> false
                            }
                        }
                    }
                }
                .toSet()
        }

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

    // TODO: Use data object ?
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