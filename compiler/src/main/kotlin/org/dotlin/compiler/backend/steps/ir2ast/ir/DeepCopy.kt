/*
 * Copyright 2021-2022 Wilko Manger
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

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.*
import org.jetbrains.kotlin.name.Name
import kotlin.reflect.KClass

/**
 * Deep-copies this declaration and remaps all references to it (if [remapReferences] is true).
 */
inline fun <reified T : IrDeclaration> T.deepCopy(
    remapReferences: Boolean = true,
    initialParent: IrDeclarationParent? = null
): T = when (this) {
    is IrClass -> deepCopyWith(remapReferences, initialParent) {}
    is IrConstructor -> deepCopyWith(remapReferences, initialParent) {}
    is IrFunction -> deepCopyWith(remapReferences, initialParent) {}
    is IrProperty -> deepCopyWith(remapReferences, initialParent) {}
    is IrField -> deepCopyWith(remapReferences, initialParent) {}
    is IrValueParameter -> deepCopyWith(remapReferences, initialParent) {}
    is IrVariable -> deepCopyWith(remapReferences, initialParent) {}
    else -> throw UnsupportedOperationException("Cannot deep-copy ${T::class.simpleName}")
}

/**
 * Deep-copies this declaration and remaps all references to it (if [remapReferences] is true).
 */
inline fun <reified T : IrDeclarationWithName> T.deepCopyWith(
    remapReferences: Boolean = true,
    initialParent: IrDeclarationParent? = null,
    name: Name,
): T = when (this) {
    is IrClass -> deepCopyWith(remapReferences, initialParent) { this.name = name }
    is IrConstructor -> deepCopyWith(remapReferences, initialParent) { this.name = name }
    is IrFunction -> deepCopyWith(remapReferences, initialParent) { this.name = name }
    is IrProperty -> deepCopyWith(remapReferences, initialParent) { this.name = name }
    is IrField -> deepCopyWith(remapReferences, initialParent) { this.name = name }
    is IrValueParameter -> deepCopyWith(remapReferences, initialParent) { this.name = name }
    is IrVariable -> deepCopyWith(remapReferences, initialParent) { this.name = name }
    else -> throw UnsupportedOperationException("Cannot deep-copy ${T::class.simpleName}")
}

/**
 * Deep-copies the given class and remaps all references to it (if [remapReferences] is true).
 */
inline fun <reified T : IrPossiblyExternalDeclaration> T.deepCopyWith(
    remapReferences: Boolean = true,
    initialParent: IrDeclarationParent? = null,
    isExternal: Boolean,
): T = when (this) {
    is IrClass -> deepCopyWith(remapReferences, initialParent) { this.isExternal = isExternal }
    is IrConstructor -> deepCopyWith(remapReferences, initialParent) { this.isExternal = isExternal }
    is IrFunction -> deepCopyWith(remapReferences, initialParent) { this.isExternal = isExternal }
    is IrProperty -> deepCopyWith(remapReferences, initialParent) { this.isExternal = isExternal }
    is IrField -> deepCopyWith(remapReferences, initialParent) { this.isExternal = isExternal }
    else -> throw UnsupportedOperationException("Cannot deep-copy ${T::class.simpleName}")
}

/**
 * Deep-copies the given class and remaps all references to it (if [remapReferences] is true).
 */
inline fun <reified T : IrClass> T.deepCopyWith(
    remapReferences: Boolean = true,
    initialParent: IrDeclarationParent? = null,
    block: IrClassBuilder.() -> Unit
): T = deepCopyWith(
    block,
    createBuilder = { IrClassBuilder() },
    updateFrom = {
        updateFrom(it)
        name = it.name
    },
    initialParent,
    remapReferences
)

/**
 * Deep-copies the given function and remaps all references to it (if [remapReferences] is true).
 */
inline fun <reified T : IrFunction> T.deepCopyWith(
    remapReferences: Boolean = true,
    initialParent: IrDeclarationParent? = null,
    block: IrFunctionBuilder.() -> Unit
): T = deepCopyWith(
    block,
    createBuilder = { IrFunctionBuilder() },
    updateFrom = {
        updateFrom(it)
        name = it.name
        returnType = it.returnType
    },
    initialParent,
    remapReferences
).also {
    if (this is IrSimpleFunction && it is IrSimpleFunction) {
        it.correspondingPropertySymbol = correspondingPropertySymbol
    }

    if (remapReferences) {
        it.remapReferences(
            valueParameters.zip(it.valueParameters)
                .map { (old, new) -> old.symbol to new.symbol }
                .toMap()
        )
    }
}

/**
 * Deep-copies the given property and remaps all references to it (if [remapReferences] is true).
 */
inline fun <reified T : IrProperty> T.deepCopyWith(
    remapReferences: Boolean = true,
    initialParent: IrDeclarationParent? = null,
    block: IrPropertyBuilder.() -> Unit
): T = deepCopyWith(
    block,
    createBuilder = { IrPropertyBuilder() },
    updateFrom = {
        updateFrom(it)
        name = it.name
    },
    initialParent,
    remapReferences
)

/**
 * Deep-copies the given value parameter and remaps all references to it (if [remapReferences] is true).
 */
inline fun <reified T : IrField> T.deepCopyWith(
    remapReferences: Boolean = true,
    initialParent: IrDeclarationParent? = null,
    block: IrFieldBuilder.() -> Unit
): T = deepCopyWith(
    block,
    createBuilder = { IrFieldBuilder() },
    updateFrom = {
        updateFrom(it)
        name = it.name
    },
    initialParent,
    remapReferences
).apply {
    correspondingPropertySymbol = this@deepCopyWith.correspondingPropertySymbol
}

/**
 * Deep-copies the given variable and remaps all references to it (if [remapReferences] is true).
 */
inline fun <reified T : IrVariable> T.deepCopyWith(
    remapReferences: Boolean = true,
    initialParent: IrDeclarationParent? = null,
    block: IrVariableBuilder.() -> Unit
): T = deepCopyWith(
    block,
    createBuilder = { IrVariableBuilder() },
    updateFrom = {
        updateFrom(it)
        name = it.name
    },
    initialParent,
    remapReferences
)

/**
 * Deep-copies the given field and remaps all references to it (if [remapReferences] is true).
 */
inline fun <reified T : IrValueParameter> T.deepCopyWith(
    remapReferences: Boolean = true,
    initialParent: IrDeclarationParent? = null,
    block: IrValueParameterBuilder.() -> Unit
): T = deepCopyWith(
    block,
    createBuilder = { IrValueParameterBuilder() },
    updateFrom = {
        updateFrom(it)
        name = it.name
    },
    initialParent,
    remapReferences
)

/**
 * Don't use this, use specialized versions.
 */
inline fun <reified D : IrDeclaration, B : IrDeclarationBuilder> D.deepCopyWith(
    block: B.() -> Unit,
    createBuilder: () -> B,
    updateFrom: B.(D) -> Unit,
    initialParent: IrDeclarationParent?,
    remapReferences: Boolean,
): D {
    lateinit var deepCopier: DeepCopier

    return deepCopyWithSymbols(initialParent ?: parent) { symbolRemapper, typeRemapper ->
        val builder = createBuilder().also {
            updateFrom(it, this)
        }

        block(builder)

        DeepCopier(
            symbolRemapper,
            typeRemapper,
            symbolRenamer = SingleSymbolRenamer(symbol, builder.name),
            declarationRebuilder = SingleDeclarationRebuilder(symbol, builder)
        ).also {
            deepCopier = it
        }
    }.also {
        if (remapReferences) {
            it.remapReferencesEverywhere(symbol to it.symbol)
            it.remapTypesEverywhere(
                DeepCopyTypeRemapper(SymbolReferenceRemapper(symbol to it.symbol)).also { remapper ->
                    remapper.deepCopy = deepCopier
                }
            )
        }
    }
}

interface DeclarationRebuilder {
    fun getClassBuilder(symbol: IrClassSymbol): IrClassBuilder? = null
    fun getFunctionBuilder(symbol: IrFunctionSymbol): IrFunctionBuilder? = null
    fun getPropertyBuilder(symbol: IrPropertySymbol): IrPropertyBuilder? = null
    fun getFieldBuilder(symbol: IrFieldSymbol): IrFieldBuilder? = null
    fun getValueParameterBuilder(symbol: IrValueParameterSymbol): IrValueParameterBuilder? = null
}

class SingleDeclarationRebuilder(private val symbol: IrSymbol, private val builder: IrDeclarationBuilder) :
    DeclarationRebuilder {
    private inline fun <reified B : IrDeclarationBuilder> builderOrNull(symbol: IrSymbol) = when (symbol) {
        this.symbol -> builder as B
        else -> null
    }

    override fun getClassBuilder(symbol: IrClassSymbol): IrClassBuilder? = builderOrNull(symbol)
    override fun getFunctionBuilder(symbol: IrFunctionSymbol): IrFunctionBuilder? = builderOrNull(symbol)
    override fun getPropertyBuilder(symbol: IrPropertySymbol): IrPropertyBuilder? = builderOrNull(symbol)
    override fun getFieldBuilder(symbol: IrFieldSymbol): IrFieldBuilder? = builderOrNull(symbol)
    override fun getValueParameterBuilder(symbol: IrValueParameterSymbol): IrValueParameterBuilder? =
        builderOrNull(symbol)
}

class SingleSymbolRenamer(private val symbol: IrSymbol, private val name: Name) : SymbolRenamer {
    private fun newNameIfMatch(symbol: IrSymbol, orElse: () -> Name) = when (symbol) {
        this.symbol -> name
        else -> orElse()
    }

    override fun getClassName(symbol: IrClassSymbol) = newNameIfMatch(symbol) { symbol.owner.name }
    override fun getFunctionName(symbol: IrSimpleFunctionSymbol) = newNameIfMatch(symbol) { symbol.owner.name }
    override fun getFieldName(symbol: IrFieldSymbol) = newNameIfMatch(symbol) { symbol.owner.name }
    override fun getEnumEntryName(symbol: IrEnumEntrySymbol) = newNameIfMatch(symbol) { symbol.owner.name }
    override fun getVariableName(symbol: IrVariableSymbol) = newNameIfMatch(symbol) { symbol.owner.name }
    override fun getTypeParameterName(symbol: IrTypeParameterSymbol) = newNameIfMatch(symbol) { symbol.owner.name }
    override fun getValueParameterName(symbol: IrValueParameterSymbol) = newNameIfMatch(symbol) { symbol.owner.name }
    override fun getTypeAliasName(symbol: IrTypeAliasSymbol) = newNameIfMatch(symbol) { symbol.owner.name }
}



class DeclarationReferenceRemapper(
    private val mapping: Map<out IrSymbol, IrSymbol>
) : IrElementTransformerVoid() {
    private inline fun <reified S : IrSymbol> IrOverridableDeclaration<S>.remapOverrides() {
        overriddenSymbols = overriddenSymbols.map { mapping[it] as? S ?: it }
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction) = declaration.apply {
        transformChildrenVoid()
        remapOverrides()
    }

    override fun visitProperty(declaration: IrProperty) = declaration.apply {
        transformChildrenVoid()
        remapOverrides()
    }

    override fun visitDeclarationReference(expression: IrDeclarationReference): IrExpression {
        return when (mapping[expression.symbol]) {
            null -> super.visitDeclarationReference(expression)
            else -> expression.deepCopy()
        }
    }

    override fun visitMemberAccess(expression: IrMemberAccessExpression<*>) =
        visitDeclarationReference(expression)

    override fun visitField(declaration: IrField): IrStatement {
        // This might be necessary because we remove IrProperties from the IR tree,
        // meaning visitProperty is never called.
        declaration.correspondingProperty?.run {
            backingField?.let {
                (mapping[it.symbol] as? IrFieldSymbol)?.let { newBackingFieldSymbol ->
                    backingField = newBackingFieldSymbol.owner
                }
            }
        }

        return super.visitField(declaration)
    }

    private inline fun <reified E : IrExpression> E.deepCopy(): E {
        val symbolRemapper = SymbolReferenceRemapper(mapping)
        acceptVoid(symbolRemapper)
        return transform(
            DeepCopyIrTreeWithSymbols(symbolRemapper, DeepCopyTypeRemapper(symbolRemapper)),
            data = null
        ).also {
            val old = this
            // Functions of function expression still need their parents set.
            it.acceptChildrenVoid(
                object : IrElementVisitorVoid {
                    override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)

                    override fun visitFunctionExpression(expression: IrFunctionExpression) {
                        super.visitFunctionExpression(expression)

                        // TODO (possibly): Make sure there's only a single match.

                        val oldFunctionExpression = old.firstOrNullChild<IrFunctionExpression> { oldChild ->
                            oldChild.attributeOwnerId === expression.attributeOwnerId
                        } ?: error("Original IrFunctionExpression not found for $expression")

                        expression.function.parent = oldFunctionExpression.function.parent
                    }
                }
            )
        } as E
    }
}

class SymbolReferenceRemapper(
    private val mapping: Map<out IrSymbol, IrSymbol>
) : DeepCopySymbolRemapper() {
    constructor(mapping: Pair<IrSymbol, IrSymbol>) : this(mapOf(mapping))

    private fun Map.Entry<IrSymbol, IrSymbol>.hasEquivalentTypes(validTypes: Iterable<KClass<*>>): Boolean {
        return validTypes.any {
            it.isInstance(key) && it.isInstance(value)
        }
    }

    init {
        require(
            mapping.all {
                it.hasEquivalentTypes(
                    listOf(
                        IrFileSymbol::class,
                        IrExternalPackageFragmentSymbol::class,
                        IrEnumEntrySymbol::class,
                        IrFieldSymbol::class,
                        IrClassSymbol::class,
                        IrScriptSymbol::class,
                        IrTypeParameterSymbol::class,
                        IrValueParameterSymbol::class,
                        IrVariableSymbol::class,
                        IrReturnTargetSymbol::class,
                        IrConstructorSymbol::class,
                        IrReturnableBlockSymbol::class,
                        IrPropertySymbol::class,
                        IrLocalDelegatedPropertySymbol::class,
                        IrTypeAliasSymbol::class
                    )
                )
            }
        ) {
            "The new symbol does not have the equivalent type of the old symbol. " +
                    "Or the symbol type has not been added to check."
        }
    }

    init {
        classes.putRelevant()
        scripts.putRelevant()
        constructors.putRelevant()
        enumEntries.putRelevant()
        externalPackageFragments.putRelevant()
        fields.putRelevant()
        files.putRelevant()
        functions.putRelevant()
        properties.putRelevant()
        returnableBlocks.putRelevant()
        typeParameters.putRelevant()
        valueParameters.putRelevant()
        variables.putRelevant()
        localDelegatedProperties.putRelevant()
        typeAliases.putRelevant()
    }

    private inline fun <reified S : IrSymbol> HashMap<S, S>.putRelevant() {
        putAll(
            mapping.entries
                .filter { it.key is S } // it.value is guaranteed to be S as well.
                .map { Pair(it.key as S, it.value as S) }
                .toMap()
        )
    }
}

class DeepCopier(
    symbolRemapper: SymbolRemapper,
    typeRemapper: TypeRemapper,
    symbolRenamer: SymbolRenamer,
    private val declarationRebuilder: DeclarationRebuilder,
) : DeepCopyIrTreeWithSymbols(symbolRemapper, typeRemapper, symbolRenamer) {
    override fun visitClass(declaration: IrClass) = declaration.let {
        val builder = declarationRebuilder.getClassBuilder(declaration.symbol)
            ?: return super.visitClass(declaration)

        super.visitClass(
            object : IrClass() {
                override var annotations = it.annotations
                override var attributeOwnerId = it.attributeOwnerId
                override val declarations = it.declarations

                @ObsoleteDescriptorBasedAPI
                override val descriptor = it.descriptor
                override val endOffset = builder.endOffset
                override val factory = it.factory
                override val isCompanion = builder.isCompanion
                override val isData = builder.isCompanion
                override val isExpect = builder.isExpect
                override val isExternal = builder.isExternal
                override val isFun = builder.isFun
                override val isInner = builder.isInner
                override val isValue = it.isValue
                override val kind = builder.kind
                override var metadata = it.metadata
                override var modality = builder.modality
                override var name = builder.name
                override var origin = builder.origin
                override var parent = it.parent
                override var sealedSubclasses = it.sealedSubclasses
                override val source = it.source
                override val startOffset = builder.startOffset
                override var superTypes = it.superTypes
                override val symbol = it.symbol
                override var thisReceiver = it.thisReceiver
                override var typeParameters = it.typeParameters
                override var valueClassRepresentation = it.valueClassRepresentation
                override var visibility = builder.visibility
            },
        )
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction) = declaration.let {
        val builder = declarationRebuilder.getFunctionBuilder(it.symbol)
            ?: return super.visitSimpleFunction(it)

        super.visitSimpleFunction(
            object : IrSimpleFunction() {
                override var annotations = it.annotations
                override var attributeOwnerId = it.attributeOwnerId
                override var body = it.body
                override val containerSource = builder.containerSource
                override var contextReceiverParametersCount = it.contextReceiverParametersCount
                override var correspondingPropertySymbol = it.correspondingPropertySymbol

                @ObsoleteDescriptorBasedAPI
                override val descriptor = it.descriptor
                override var dispatchReceiverParameter = it.dispatchReceiverParameter
                override val endOffset = builder.endOffset
                override var extensionReceiverParameter = it.extensionReceiverParameter
                override val factory = it.factory
                override val isExpect = builder.isExpect
                override val isExternal = builder.isExternal
                override val isFakeOverride = builder.isFakeOverride
                override val isInfix = builder.isInfix
                override val isInline = builder.isInline
                override val isOperator = builder.isOperator
                override val isSuspend = builder.isSuspend
                override val isTailrec = builder.isTailrec
                override var metadata = it.metadata
                override val modality = builder.modality
                override var name = builder.name
                override var origin = builder.origin
                override var overriddenSymbols = it.overriddenSymbols
                override var parent = it.parent
                override var returnType = builder.returnType
                override val startOffset = builder.startOffset
                override val symbol = it.symbol
                override var typeParameters = it.typeParameters
                override var valueParameters = it.valueParameters
                override var visibility = builder.visibility
            }
        )
    }

    override fun visitConstructor(declaration: IrConstructor) = declaration.let {
        val builder = declarationRebuilder.getFunctionBuilder(it.symbol)
            ?: return super.visitConstructor(it)

        super.visitConstructor(
            object : IrConstructor() {
                override var annotations = it.annotations
                override var body = it.body
                override val containerSource = builder.containerSource
                override var contextReceiverParametersCount = it.contextReceiverParametersCount

                @ObsoleteDescriptorBasedAPI
                override val descriptor = it.descriptor
                override var dispatchReceiverParameter = it.dispatchReceiverParameter
                override val endOffset = builder.endOffset
                override var extensionReceiverParameter = it.extensionReceiverParameter
                override val factory = it.factory
                override val isExpect = builder.isExpect
                override val isExternal = builder.isExternal
                override val isInline = builder.isInline
                override val isPrimary = builder.isPrimary
                override var metadata = it.metadata
                override var name = builder.name
                override var origin = builder.origin
                override var parent = it.parent
                override var returnType = builder.returnType
                override val startOffset = builder.startOffset
                override val symbol = it.symbol
                override var typeParameters = it.typeParameters
                override var valueParameters = it.valueParameters
                override var visibility = builder.visibility
            }
        )
    }


    override fun visitProperty(declaration: IrProperty) = declaration.let {
        val builder = declarationRebuilder.getPropertyBuilder(it.symbol)
            ?: return super.visitProperty(it)

        super.visitProperty(
            object : IrProperty() {
                override var annotations = it.annotations
                override var attributeOwnerId = it.attributeOwnerId
                override var backingField = it.backingField
                override val containerSource = builder.containerSource

                @ObsoleteDescriptorBasedAPI
                override val descriptor = it.descriptor
                override val endOffset = builder.endOffset
                override val factory = it.factory
                override var getter = it.getter
                override val isConst = builder.isConst
                override val isDelegated = builder.isDelegated
                override val isExpect = builder.isExpect
                override val isExternal = builder.isExternal
                override val isFakeOverride = builder.isFakeOverride
                override val isLateinit = builder.isLateinit
                override val isVar = builder.isVar
                override var metadata = it.metadata
                override val modality = builder.modality
                override var name = builder.name
                override var origin = builder.origin
                override var overriddenSymbols = it.overriddenSymbols
                override var parent = it.parent
                override var setter = it.setter
                override val startOffset = builder.startOffset
                override val symbol = it.symbol
                override var visibility = builder.visibility
            }
        )
    }

    override fun visitField(declaration: IrField) = declaration.let {
        val builder = declarationRebuilder.getFieldBuilder(it.symbol)
            ?: return super.visitField(it)

        super.visitField(
            object : IrField() {
                override var annotations = it.annotations
                override var correspondingPropertySymbol = it.correspondingPropertySymbol

                @ObsoleteDescriptorBasedAPI
                override val descriptor = it.descriptor
                override val endOffset = it.endOffset
                override val factory = it.factory
                override var initializer = it.initializer
                override val isExternal = builder.isExternal
                override val isFinal = builder.isFinal
                override val isStatic = builder.isStatic
                override var metadata = builder.metadata
                override var name = builder.name
                override var origin = builder.origin
                override var parent = it.parent
                override val startOffset = builder.startOffset
                override val symbol = it.symbol
                override var type = builder.type
                override var visibility = builder.visibility
            }
        )
    }

    override fun visitValueParameter(declaration: IrValueParameter) = declaration.let {
        val builder = declarationRebuilder.getValueParameterBuilder(it.symbol)
            ?: return super.visitValueParameter(it)

        super.visitValueParameter(
            object : IrValueParameter() {
                override var annotations = it.annotations
                override var defaultValue = it.defaultValue

                @ObsoleteDescriptorBasedAPI
                override val descriptor = it.descriptor
                override val endOffset = it.endOffset
                override val factory = it.factory
                override val index = it.index
                override val isAssignable = builder.isAssignable
                override val isCrossinline = it.isCrossinline
                override val isHidden = builder.isHidden
                override val isNoinline = builder.isNoinline
                override var name = builder.name
                override var origin = builder.origin
                override var parent = it.parent
                override val startOffset = it.startOffset
                override val symbol = it.symbol
                override var type = builder.type
                override var varargElementType = builder.varargElementType
            }
        )
    }
}

class IrVariableBuilder : IrDeclarationBuilder() {
    lateinit var type: IrType

    var isVar: Boolean = false
    var isLateinit: Boolean = false
    var isConst: Boolean = false

    fun updateFrom(from: IrVariable) {
        super.updateFrom(from)

        type = from.type
        isVar = from.isVar
        isLateinit = from.isLateinit
        isConst = from.isConst
    }
}