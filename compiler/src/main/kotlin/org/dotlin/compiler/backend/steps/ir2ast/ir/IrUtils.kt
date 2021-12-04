/*
 * Copyright 2021 Wilko Manger
 *
 * This file is part of Dotlin.
 *
 * Dotlin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dotlin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Dotlin.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.dotlin.compiler.backend.steps.ir2ast.ir

import org.dotlin.compiler.backend.steps.falseIfNull
import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.toDart
import org.dotlin.compiler.dart.ast.type.DartTypeAnnotation
import org.jetbrains.kotlin.backend.common.ir.*
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrClassSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrConstructorSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrFieldSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrPropertySymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.collectionUtils.filterIsInstanceAnd

val IrSimpleFunction.correspondingProperty: IrProperty?
    get() = correspondingPropertySymbol?.owner

inline fun <S : IrSymbol, reified D : IrOverridableDeclaration<S>> D.resolveOverride(): D? =
    overriddenSymbols.firstOrNull()?.owner as D?

fun IrValueParameter.resolveOverride(): IrValueParameter? {
    val irFunction = parent as? IrSimpleFunction ?: return null
    return irFunction.resolveOverride()?.valueParameters?.get(index)
}

val IrOverridableDeclaration<*>.isOverride: Boolean
    get() = resolveOverride() != null

val IrValueParameter.isOverride: Boolean
    get() = resolveOverride() != null

fun IrExpression.hasReferenceToThis() = hasAny { it is IrExpression && it.isThisReference() }

fun IrProperty.hasReferenceToThis() = backingField?.initializer?.expression?.hasReferenceToThis() ?: false

fun IrExpression.hasAny(block: (IrElement) -> Boolean) =
    block(this) || hasAnyChildren(block)


fun IrExpression.hasAnyChildren(block: (IrElement) -> Boolean): Boolean {
    var hasIt = false

    val visitor = object : IrElementVisitor<Unit, Unit> {
        override fun visitElement(element: IrElement, data: Unit) {
            if (!hasIt && block(element)) {
                hasIt = true
            }
        }
    }

    acceptChildren(visitor, Unit)

    return hasIt
}


val IrDeclaration.parentClassProperties: Sequence<IrProperty>
    get() = parentClassOrNull?.properties
        ?: emptySequence()

/**
 * Also handles properties that were simplified by [PropertySimplifyingLowering].
 */
val IrClass.properties: Sequence<IrProperty>
    get() = declarations
        .asSequence()
        .mapNotNull {
            it as? IrProperty
                ?: (it as? IrField)?.correspondingProperty
                ?: (it as? IrSimpleFunction)?.correspondingProperty
        }
        .distinct()

/**
 * Properties this primary constructor parameter references, including the [correspondingProperty].
 * For the corresponding property that's actually declared with this parameter, use [correspondingProperty].
 */
val IrValueParameter.propertyDependents: Sequence<IrProperty>
    get() = parentClassProperties.filter { it.hasReferenceTo(this) }

fun IrValueParameter.asAssignable(origin: IrDeclarationOrigin = this.origin) =
    copyTo(parent as IrFunction, isAssignable = true, origin = origin)

fun IrExpression.hasReferenceTo(parameter: IrValueParameter) =
    hasDirectReferenceTo(parameter) || hasIndirectReferenceTo(parameter)

fun IrExpression.hasDirectReferenceTo(parameter: IrValueParameter): Boolean {
    return this is IrDeclarationReference && symbol == parameter.symbol
}

fun IrExpression.hasIndirectReferenceTo(parameter: IrValueParameter): Boolean {
    var hasReference = false

    val visitor = object : IrElementVisitor<Unit, Unit> {
        override fun visitElement(element: IrElement, data: Unit) {
            if (!hasReference && element is IrDeclarationReference) {
                hasReference = element.symbol == parameter.symbol

                val owner = element.symbol.owner
                if (owner is IrSimpleFunction) {
                    val ownerCorrespondingPropertySymbol = owner.correspondingPropertySymbol

                    // This means the element's owner is a getter which points
                    // to value parameters' corresponding property.
                    hasReference =
                        hasReference || ownerCorrespondingPropertySymbol == parameter.correspondingProperty?.symbol

                    // This means the element's owner is a property with an initializer that has a reference to
                    // the parameter.
                    hasReference =
                        hasReference || ownerCorrespondingPropertySymbol?.owner?.hasReferenceTo(parameter) ?: false
                }

                if (!hasReference) {
                    element.acceptChildren(this, Unit)
                }
            }
        }
    }

    acceptChildren(visitor, Unit)

    return hasReference
}

fun IrProperty.hasReferenceTo(parameter: IrValueParameter) =
    backingField?.initializer?.expression?.hasReferenceTo(parameter) ?: false

fun IrProperty.hasDirectReferenceTo(parameter: IrValueParameter) =
    backingField?.initializer?.expression?.hasDirectReferenceTo(parameter) ?: false

fun IrProperty.hasIndirectReferenceTo(parameter: IrValueParameter) =
    backingField?.initializer?.expression?.hasIndirectReferenceTo(parameter) ?: false

/**
 * Properties this primary constructor parameter references, not including the [correspondingProperty].
 * For the corresponding property that's actually declared with this parameter, use [correspondingProperty].
 */
val IrValueParameter.otherPropertyDependents: List<IrProperty>
    get() {
        return propertyDependents
            .filter {
                val value = it.backingField?.initializer?.expression
                value !is IrGetValue || value.origin != IrStatementOrigin.INITIALIZE_PROPERTY_FROM_PARAMETER
            }
            .toList()
    }

/**
 * Whether the [IrExpression] is 'complex' in terms of Dart final initializers.
 */
fun IrExpression?.isComplex() = this != null && this !is IrConst<*>

fun IrExpression?.isThisReference() = this is IrGetValue && symbol.owner.name == Name.special("<this>")

fun IrExpression.wrap() = IrExpressionBodyImpl(this)

/**
 * The property that's declared and initialized through this parameter.
 */
val IrValueParameter.correspondingProperty: IrProperty?
    get() = parentClassProperties.firstOrNull {
        it.isInitializedByParameter && it.hasDirectReferenceTo(this)
    }

// If typeOrNull returns null, it's a star projection, which corresponds best to dynamic in Dart.
fun IrTypeArgument.toDart(context: DartTransformContext): DartTypeAnnotation =
    typeOrNull?.toDart(context) ?: DartTypeAnnotation.DYNAMIC

fun todo(element: IrElement): Nothing = TODO("${element::class.java.simpleName} is not supported")

private fun IrType.isNullableType(signature: IdSignature) =
    isNullable() && classifierOrNull?.signature == signature

fun IrType.isNullableBoolean() = isNullableType(IdSignatureValues._boolean)
fun IrType.isNullableByte() = isNullableType(IdSignatureValues._byte)
fun IrType.isNullableShort() = isNullableType(IdSignatureValues._short)
fun IrType.isNullableInt() = isNullableType(IdSignatureValues._int)
fun IrType.isNullableLong() = isNullableType(IdSignatureValues._long)
fun IrType.isNullableFloat() = isNullableType(IdSignatureValues._float)
fun IrType.isNullableDouble() = isNullableType(IdSignatureValues._double)
fun IrType.isNullableChar() = isNullableType(IdSignatureValues._char)


// Dart built-ins:
// - bool
// - double
// - Enum
// - int
// - Null
// - num
// - String
fun IrType.isDartCorePrimitive() =
    isBoolean() || isNullableBoolean() ||
            isShort() || isNullableShort() ||
            isInt() || isNullableInt() ||
            isLong() || isNullableLong() ||
            isDouble() || isNullableDouble() ||
            isFloat() || isNullableFloat() ||
            isString() || isNullableString()

val IrDeclarationWithVisibility.isPrivate
    get() = visibility == DescriptorVisibilities.PRIVATE

inline fun <reified T : IrDeclarationWithName> List<IrDeclaration>.withName(name: String): T =
    filterIsInstance<T>().first { it.name == Name.identifier(name) }

fun List<IrDeclaration>.methodWithName(name: String) = withName<IrSimpleFunction>(name)
fun List<IrDeclaration>.propertyWithName(name: String) = withName<IrProperty>(name)
fun List<IrDeclaration>.getterWithName(name: String) = propertyWithName(name).getter!!
fun List<IrDeclaration>.setterWithName(name: String) = propertyWithName(name).setter!!
fun List<IrDeclaration>.fieldWithName(name: String) = withName<IrField>(name)

inline fun <reified T : IrDeclarationWithName> IrDeclarationContainer.declarationWithName(name: String) =
    declarations.withName<T>(name)

fun IrDeclarationContainer.methodWithName(name: String) = declarationWithName<IrSimpleFunction>(name)
fun IrDeclarationContainer.propertyWithName(name: String) = declarationWithName<IrProperty>(name)
fun IrDeclarationContainer.getterWithName(name: String) = propertyWithName(name).getter!!
fun IrDeclarationContainer.setterWithName(name: String) = propertyWithName(name).setter!!
fun IrDeclarationContainer.fieldWithName(name: String) = declarationWithName<IrField>(name)

inline fun <reified T : IrDeclarationWithName> MutableList<IrDeclaration>.addIfNotExists(
    name: Name,
    block: (Name) -> T
): T {
    val found = filterIsInstanceAnd<T> { it.name == name }.firstOrNull()
    if (found != null) {
        return found
    }

    val value = block(name)
    add(value)
    return value
}

inline fun <reified T : IrDeclarationWithName> MutableList<IrDeclaration>.addIfNotExists(declaration: T): T {
    val found = filterIsInstanceAnd<T> { it.name == declaration.name }.firstOrNull()
    if (found != null) {
        return found
    }

    add(declaration)
    return declaration
}

inline fun <T : IrElement> IrGeneratorWithScope.buildStatement(
    origin: IrStatementOrigin? = null,
    builder: IrSingleStatementBuilder.() -> T
) = IrSingleStatementBuilder(context, scope, UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin).builder()

val IrProperty.isInitializedByParameter: Boolean
    get() = backingField?.isInitializedByParameter.falseIfNull()

val IrField.isInitializedByParameter: Boolean
    get() {
        val initializerValue = initializer?.expression
        return initializerValue is IrGetValue &&
                initializerValue.origin == IrStatementOrigin.INITIALIZE_PROPERTY_FROM_PARAMETER
    }

val IrProperty.isToBeInitializedInFieldInitializerList: Boolean
    get() = backingField?.isToBeInitializedInFieldInitializerList.falseIfNull()

val IrField.isToBeInitializedInFieldInitializerList: Boolean
    get() = hasInitializerOriginOf<IrDartStatementOrigin.COMPLEX_PARAM_PROPERTY_TO_BE_INITIALIZED_IN_FIELD_INITIALIZER_LIST>()

val IrProperty.isInitializedInBody: Boolean
    get() = backingField?.isInitializedInBody.falseIfNull()

val IrField.isInitializedInBody: Boolean
    get() = hasInitializerOriginOf(IrDartStatementOrigin.COMPLEX_PROPERTY_INITIALIZED_IN_BODY) ||
            hasInitializerOriginOf(IrDartStatementOrigin.PROPERTY_REFERENCING_THIS_INITIALIZED_IN_BODY)

fun IrField.hasInitializerOriginOf(origin: IrStatementOrigin) =
    (initializer as? IrExpressionBodyWithOrigin)?.origin == origin

inline fun <reified T : IrStatementOrigin> IrField.hasInitializerOriginOf() =
    (initializer as? IrExpressionBodyWithOrigin)?.origin is T

val IrProperty.isOverride: Boolean
    get() = getter?.isOverride.falseIfNull() || setter?.isOverride.falseIfNull()

val IrProperty.type: IrType
    get() = backingField?.type ?: getter?.returnType!!

private val IrStatement.leftHandOfSet: IrDeclaration?
    get() = when (this) {
        is IrSetValue -> symbol.owner
        is IrSetField -> symbol.owner
        else -> null
    }

val IrStatement.propertyItAssignsTo: IrProperty?
    get() = (leftHandOfSet as? IrField)?.correspondingPropertySymbol?.owner

val IrStatement.parameterItAssignsTo: IrValueParameter?
    get() = leftHandOfSet as? IrValueParameter

val IrStatement.isInitializerForPropertyToBeInitializedInFieldInitializerList: Boolean
    get() = this is IrSetValue && isInitializerForPropertyToBeInitializedInFieldInitializerList

val IrSetValue.isInitializerForPropertyToBeInitializedInFieldInitializerList: Boolean
    get() {
        val assignee = leftHandOfSet as? IrValueParameter ?: return false
        val correspondingProperty = assignee.correspondingProperty ?: return false

        return correspondingProperty.isToBeInitializedInFieldInitializerList && isInitializerForComplexParameter
    }

val IrStatement.isInitializerForComplexParameter: Boolean
    get() = this is IrSetValue &&
            (origin == IrDartStatementOrigin.COMPLEX_PARAM_INIT_DEFAULT_VALUE
                    || origin == IrDartStatementOrigin.COMPLEX_PARAM_INIT_NULLABLE)

val IrProperty.hasImplicitGetter: Boolean
    get() = getter != null && getter!!.isImplicitGetter

val IrProperty.hasImplicitSetter: Boolean
    get() = setter != null && setter!!.isImplicitSetter

fun IrProperty.markAsToBeInitializedInFieldInitializerList(defaultValue: IrExpressionBody) {
    backingField!!.setInitializerOriginTo(
        IrDartStatementOrigin.COMPLEX_PARAM_PROPERTY_TO_BE_INITIALIZED_IN_FIELD_INITIALIZER_LIST(defaultValue)
    )
}

fun IrProperty.unsetInitializerOrigin() = backingField!!.unsetInitializerOrigin()

val IrFunction.isImplicitGetter: Boolean
    get() = isGetter && origin == IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR

val IrFunction.isImplicitSetter: Boolean
    get() = isSetter && origin == IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR

val IrProperty.hasExplicitBackingField: Boolean
    get() = backingField?.isExplicitBackingField == true

fun IrField.setInitializerOriginTo(
    origin: IrStatementOrigin,
    expression: IrExpression? = initializer?.expression
) {
    if (initializer == null) return

    initializer = IrExpressionBodyWithOrigin(
        expression = initializer!!.expression,
        origin = origin
    )
}

fun IrField.unsetInitializerOrigin() {
    if (initializer == null) return

    initializer = IrExpressionBodyImpl(initializer!!.expression)
}

/**
 * Returns the single element of this `Sequence`, null if it's empty and throws if there's more than one element.
 */
fun <T> Sequence<T>.singleOrNullIfEmpty(lazyMessage: (() -> String)? = null): T? {
    val iterator = iterator()

    if (!iterator.hasNext()) return null

    val single = iterator.next()

    if (iterator.hasNext()) {
        throw IllegalArgumentException(lazyMessage?.invoke() ?: "Sequence has more than one element.")
    }

    return single
}

val IrField.isOverride: Boolean
    get() = correspondingProperty?.isOverride.falseIfNull()

val IrField.correspondingProperty: IrProperty?
    get() = correspondingPropertySymbol?.owner

val IrField.isExplicitBackingField: Boolean
    get() = !correspondingProperty?.hasImplicitGetter.falseIfNull() &&
            !correspondingProperty?.hasImplicitSetter.falseIfNull() &&
            correspondingProperty?.backingField == this &&
            origin == IrDeclarationOrigin.PROPERTY_BACKING_FIELD

val IrType.owner: IrDeclarationWithName
    get() = classifierOrFail.owner as IrDeclarationWithName

fun IrFactory.buildFunFrom(
    from: IrFunction,
    builder: IrFunctionBuilder.() -> Unit
): IrSimpleFunction {
    return buildFun {
        updateFrom(from)
        name = from.name
        returnType = from.returnType
        builder()
    }.copyPropertiesFrom(from)
}

fun IrFactory.buildConstructorFrom(from: IrConstructor, builder: IrFunctionBuilder.() -> Unit): IrConstructor {
    return IrFunctionBuilder().run {
        name = Name.special("<init>")

        updateFrom(from)
        returnType = from.returnType
        builder()

        createConstructor(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            origin,
            IrConstructorSymbolImpl(),
            name, visibility, returnType, isInline, isExternal, isPrimary, isExpect
        )
    }.copyPropertiesFrom(from)
}

private fun <T : IrFunction> T.copyPropertiesFrom(from: IrFunction): T = apply {
    body = from.body?.deepCopyWithSymbols(this)
    parent = from.parent
    copyValueParametersFrom(from, substitutionMap = emptyMap())
    copyTypeParametersFrom(from)

    extensionReceiverParameter = from.extensionReceiverParameter?.deepCopyWithSymbols(this)
    dispatchReceiverParameter = from.dispatchReceiverParameter?.deepCopyWithSymbols(this)

    if (this is IrSimpleFunction && from is IrSimpleFunction) {
        overriddenSymbols = from.overriddenSymbols
    }
}

fun IrBuilderWithScope.irCall(
    callee: IrFunction,
    receiver: IrExpression? = null,
    vararg valueArguments: IrExpression
): IrFunctionAccessExpression =
    irCall(callee).apply {
        this@apply.dispatchReceiver = receiver

        if (valueArguments.size > callee.valueParameters.size) {
            throw IllegalArgumentException("Too many value arguments passed for this function")
        }

        valueArguments.forEachIndexed { index, arg -> putValueArgument(index, arg) }
    }

fun IrClass.deepCopyWith(builder: IrClassBuilder.() -> Unit = {}): IrClass {
    val classBuilder = IrClassBuilder().also { it.updateFrom(this) }
    builder(classBuilder)

    return factory.createClass(
        startOffset,
        endOffset,
        classBuilder.origin,
        IrClassSymbolImpl(),
        classBuilder.name,
        classBuilder.kind,
        classBuilder.visibility,
        classBuilder.modality,
        classBuilder.isCompanion,
        classBuilder.isInner,
        classBuilder.isData,
        classBuilder.isExternal,
        classBuilder.isInline,
        classBuilder.isExpect,
        classBuilder.isFun,
        source,
    ).apply {
        val original = this@deepCopyWith

        parent = original.parent
        val copyOfOriginal = original.deepCopyWithSymbols(parent)
        declarations += copyOfOriginal.declarations
        superTypes = original.superTypes
        remapDeclarationParents(from = copyOfOriginal)
        createParameterDeclarations()
    }
}

fun IrProperty.deepCopyWith(builder: IrPropertyBuilder.() -> Unit): IrProperty {
    val propertyBuilder = IrPropertyBuilder().also { it.updateFrom(this) }
    builder(propertyBuilder)

    return factory.createProperty(
        startOffset,
        endOffset,
        propertyBuilder.origin,
        IrPropertySymbolImpl(),
        propertyBuilder.name,
        propertyBuilder.visibility,
        propertyBuilder.modality,
        propertyBuilder.isVar,
        propertyBuilder.isConst,
        propertyBuilder.isLateinit,
        propertyBuilder.isDelegated,
        propertyBuilder.isExternal,
        propertyBuilder.isExpect,
        propertyBuilder.isFakeOverride,
        propertyBuilder.containerSource,
    ).apply {
        val original = this@deepCopyWith

        parent = original.parent
        val copyOfOriginal = original.deepCopyWithSymbols(parent)
        backingField = copyOfOriginal.backingField
        getter = copyOfOriginal.getter
        setter = copyOfOriginal.setter
    }
}

fun IrField.deepCopyWith(builder: IrFieldBuilder.() -> Unit): IrField {
    val fieldBuilder = IrFieldBuilder().also { it.updateFrom(this) }
    builder(fieldBuilder)

    return factory.createField(
        startOffset,
        endOffset,
        fieldBuilder.origin,
        IrFieldSymbolImpl(),
        fieldBuilder.name,
        fieldBuilder.type,
        fieldBuilder.visibility,
        fieldBuilder.isFinal,
        fieldBuilder.isExternal,
        fieldBuilder.isStatic,
    ).apply {
        val original = this@deepCopyWith

        parent = original.parent
        val copyOfOriginal = original.deepCopyWithSymbols(parent)

        this.correspondingPropertySymbol = original.correspondingPropertySymbol
        initializer = copyOfOriginal.initializer
    }
}

fun IrValueParameter.copy(
    parent: IrFunction = this.parent as IrFunction,
    origin: IrDeclarationOrigin = this.origin,
    type: IrType? = null,
) = if (type != null) copyTo(parent, origin, type = type) else copyTo(parent, origin)

fun List<IrValueParameter>.copy(parent: IrFunction? = null) =
    map { it.copy(parent = parent ?: it.parent as IrFunction) }

fun IrValueParameter.wasComplex() = origin is IrDartDeclarationOrigin.COMPLEX_PARAM

fun IrFunction.isEqualsOverriddenFromAny() =
    name == Name.identifier("equals") &&
            dispatchReceiverParameter != null &&
            valueParameters.single().type.isNullableAny() &&
            (this as? IrSimpleFunction)?.overriddenSymbols
                ?.singleOrNull()
                ?.owner
                ?.parentClassOrNull
                ?.defaultType
                ?.isAny() ?: false

fun IrDeclarationContainer.moveChild(move: Pair<IrDeclaration, IrDeclarationContainer>) {
    declarations.remove(move.first)
    move.second.addChild(move.first)
}

fun IrElement.isDartConst(): Boolean = when (this) {
    // The constructor of _$DefaultMarker is always const.
    is IrConstructor -> when {
        // Enums always get const constructors.
        parentAsClass.isEnumClass -> true
        else -> origin == IrDartDeclarationOrigin.COMPLEX_PARAM_DEFAULT_VALUE
    }
    is IrConstructorCall -> symbol.owner.isDartConst()
    // Enum fields are always const.
    is IrField -> origin == IrDeclarationOrigin.FIELD_FOR_ENUM_ENTRY
    is IrConst<*> -> true
    else -> false
}

val IrFunctionAccessExpression.valueArguments: List<IrExpression>
    get() = (0 until valueArgumentsCount).map { getValueArgument(it)!! }

val IrFunctionAccessExpression.typeArguments: List<IrType>
    get() = (0 until typeArgumentsCount).map { getTypeArgument(it)!! }

fun IrElement.replaceExpressions(block: (IrExpression) -> IrExpression) {
    transformChildren(
        object : IrElementTransformerVoid() {
            override fun visitExpression(expression: IrExpression): IrExpression {
                expression.transformChildrenVoid()

                return block(expression)
            }
        },
        data = null
    )
}