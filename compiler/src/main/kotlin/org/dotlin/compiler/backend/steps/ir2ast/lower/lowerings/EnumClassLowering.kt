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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings

import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.util.annotate
import org.dotlin.compiler.backend.util.isDartConst
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities.PRIVATE
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities.PUBLIC
import org.jetbrains.kotlin.descriptors.Modality.*
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrConstructorImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrValueParameterImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.impl.IrConstructorSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance.INVARIANT
import org.jetbrains.kotlin.utils.addToStdlib.castAll
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

// TODO: Use
private object Documentation {
    const val VALUES = """
    /**
     * Returns an array containing the constants of this enum type, in the order they're declared.
     * This method may be used to iterate over the constants.
     * @values
     */
    """

    const val VALUE_OF = """
    /**
     * Returns the enum constant of this type with the specified name. The string must match exactly an identifier used to declare an enum constant in this type. (Extraneous whitespace characters are not permitted.)
     * @throws IllegalArgumentException if this enum type has no constant with the specified name
     * @valueOf
     */
    """
}

/**
 * Must run before [ExtensionsLowering] and [PropertySimplifyingLowering].
 */
@Suppress("UnnecessaryVariable")
class EnumClassLowering(override val context: DotlinLoweringContext) : IrDeclarationLowering {
    override fun DotlinLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass || !declaration.isEnumClass) return noChange()

        val enum = declaration

        val valuesMethod = enum.getSpecialEnumMember("values")
        valuesMethod.annotate(dotlinIrBuiltIns.dartGetter)
        enum.declarations.remove(valuesMethod)

        moveAndImplementValueOf(enum, valuesMethod)

        // Clear all constructor bodies (except for delegating constructor calls)
        enum.constructors.forEach { constructor ->
            (constructor.body as? IrBlockBody)?.statements?.removeIf { it !is IrDelegatingConstructorCall }
        }

        val entries = enum.declarations.filterIsInstance<IrEnumEntry>()

        // Real initializer constructor call is hidden in the called constructor.
        entries.forEach { entry ->
            entry.moveDelegatingConstructorCall()
        }

        val hasAnonymousClasses = entries.any { it.correspondingClass != null }
        if (!hasAnonymousClasses) return noChange()

        val file = enum.file

        val virtualMembers = enum.declarations.filter { it.isVirtual() }.castAll<IrOverridableDeclaration<*>>()

        val virtualToDelegateBaseMembers: Map<IrOverridableDeclaration<*>, IrOverridableDeclaration<*>>

        // If there are any anonymous enum entry classes, we create a private abstract base class for these
        // anonymous classes.
        val delegateBaseClass = irFactory.buildClass {
            name = Name.identifier("\$${enum.simpleDartName}")
            visibility = PRIVATE
            modality = ABSTRACT
        }.apply {
            createParameterDeclarations()

            // All open/abstract members are copied to the base class.
            virtualToDelegateBaseMembers = virtualMembers.associateWith { member ->
                member.deepCopyWithSymbols(initialParent = member.parent).also { addChild(it) }
            }

            addChild(
                IrConstructorImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    origin = IrDeclarationOrigin.ENUM_CLASS_SPECIAL_MEMBER, // TODO: Better origin
                    symbol = IrConstructorSymbolImpl(),
                    name = Name.special("<init>"),
                    visibility = PUBLIC,
                    returnType = defaultType,
                    isInline = false,
                    isExternal = false,
                    isPrimary = true,
                    isExpect = false,
                ).apply {
                    isDartConst = true
                }
            )

            file.addChild(this)
        }

        val baseDelegateProperty = createDelegatePropertyAndParameters(enum, delegateBaseClass)

        // Redirect all virtual properties and methods to the delegate.
        virtualMembers.forEach {
            val delegateMember = virtualToDelegateBaseMembers[it]!!
            val delegated = delegateIn(enum, baseDelegateProperty, member = it, delegateMember = delegateMember)

            enum.declarations.apply {
                val index = indexOf(it)
                removeAt(index)
                add(index, delegated)
            }
        }

        for (entry in entries) {
            val entryClass = entry.correspondingClass ?: continue

            entryClass.apply {
                name = Name.identifier("\$${enum.simpleDartName}\$${entryClass.name}")
                superTypes = superTypes + delegateBaseClass.defaultType

                val anonymousClassConstructor = declarations.firstIsInstance<IrConstructor>().apply {
                    visibility = PUBLIC
                    isDartConst = true
                    body = null
                }

                // We insert a new instance of the anonymous class as the last argument of
                // the enum entry constructor call.
                val oldConstructorCall = entry.constructorCall
                entry.initializerExpression = IrExpressionBodyImpl(
                    buildStatement(entry.symbol) {
                        IrEnumConstructorCallImpl(
                            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                            oldConstructorCall.type,
                            oldConstructorCall.symbol,
                            typeArgumentsCount = 0,
                            valueArgumentsCount = oldConstructorCall.valueArgumentsCount + 1
                        ).apply {
                            oldConstructorCall.valueArguments.forEachIndexed { i, arg -> putValueArgument(i, arg) }

                            putValueArgument(
                                index = valueArgumentsCount - 1,
                                buildStatement(symbol) {
                                    irCallConstructor(
                                        anonymousClassConstructor.symbol,
                                        typeArguments = emptyList()
                                    ).apply {
                                        isDartConst = true
                                    }
                                }
                            )
                        }
                    }
                )

                // Members that are unique to the anonymous class become extensions.
                declarations.toList().forEach {
                    if (it.isOriginalFunctionOrProperty()) {
                        val delegated = delegateIn(
                            enum,
                            baseDelegateProperty,
                            member = it,
                            delegateMember = it,
                            wrapGetDelegate = { getDelegate -> irAs(getDelegate, entryClass.defaultType) }
                        )

                        addChild(delegated)

                        fun IrSimpleFunction.makeExtension() {
                            extensionReceiverParameter = dispatchReceiverParameter
                            dispatchReceiverParameter = null
                            extensionReceiverParameter?.type = enum.defaultType
                        }

                        when (delegated) {
                            is IrSimpleFunction -> delegated.makeExtension()
                            is IrProperty -> {
                                delegated.getter?.makeExtension()
                                delegated.setter?.makeExtension()
                            }
                        }
                    }
                }
            }

            file.addChild(entryClass)
        }

        return noChange()
    }

    private fun DotlinLoweringContext.createDelegatePropertyAndParameters(
        enum: IrClass,
        anonymousBaseClass: IrClass
    ): IrProperty {
        val delegateType = anonymousBaseClass.defaultType

        lateinit var primaryDelegateParameter: IrValueParameter

        enum.constructors.forEach {
            val delegateParameter = irFactory.buildValueParameter(
                IrValueParameterBuilder().apply {
                    name = Name.identifier("\$delegate")
                    type = delegateType
                    index = it.valueParameters.size
                },
                parent = it
            )

            it.valueParameters = it.valueParameters + delegateParameter

            if (it.isPrimary) {
                primaryDelegateParameter = delegateParameter
            }

            (it.body as? IrBlockBody)?.statements?.replaceAll { statement ->
                when (statement) {
                    is IrDelegatingConstructorCall -> IrDelegatingConstructorCallImpl(
                        statement.startOffset, UNDEFINED_OFFSET,
                        statement.type,
                        statement.symbol,
                        typeArgumentsCount = 0,
                        valueArgumentsCount = statement.valueArgumentsCount + 1
                    ).apply {
                        statement.valueArguments.forEachIndexed { i, arg -> putValueArgument(i, arg) }

                        putValueArgument(
                            index = valueArgumentsCount - 1,
                            buildStatement(it.symbol) {
                                irGet(delegateParameter)
                            }
                        )
                    }

                    else -> statement
                }
            }
        }

        return irFactory.buildProperty {
            name = Name.identifier("\$delegate")
            visibility = PRIVATE
            this.modality = FINAL
        }.apply {
            val property = this
            enum.addChild(property)

            backingField = irFactory.buildField {
                updateFrom(property)
                name = property.name
                visibility = PUBLIC
                this.type = delegateType
            }.apply {
                this.parent = anonymousBaseClass

                initializer = IrExpressionBodyImpl(
                    IrGetValueImpl(
                        SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                        symbol = primaryDelegateParameter.symbol,
                        origin = IrStatementOrigin.INITIALIZE_PROPERTY_FROM_PARAMETER
                    )
                )
            }

            createDefaultGetter()
        }
    }

    /**
     * Returns the delegated member.
     */
    private fun DotlinLoweringContext.delegateIn(
        container: IrClass,
        delegateProperty: IrProperty,
        member: IrDeclaration,
        delegateMember: IrDeclaration,
        wrapGetDelegate: IrSingleStatementBuilder.(IrCall) -> IrExpression = { it }
    ): IrDeclaration {
        val getDelegate = buildStatement(container.symbol) {
            wrapGetDelegate(
                irCall(
                    delegateProperty.getter!!,
                    receiver = irGet(container.thisReceiver!!),
                    origin = IrStatementOrigin.GET_PROPERTY
                )
            )
        }

        return when (member) {
            is IrProperty -> {
                val copiedProperty = member.deepCopyWith {
                    modality = FINAL
                }

                replaceWith(copiedProperty)

                copiedProperty.apply {
                    backingField = null

                    getter!!.apply {
                        origin = IrDeclarationOrigin.ENUM_CLASS_SPECIAL_MEMBER // TODO: Better origin
                        body = IrExpressionBodyImpl(
                            buildStatement(copiedProperty.symbol) {
                                irCall(
                                    (delegateMember as IrProperty).getter!!,
                                    receiver = getDelegate,
                                    origin = IrStatementOrigin.GET_PROPERTY
                                )
                            }
                        )
                    }
                }
            }

            is IrSimpleFunction -> {
                val copiedMethod = member.deepCopyWith {
                    modality = FINAL
                }

                replaceWith(copiedMethod)

                copiedMethod.body = IrExpressionBodyImpl(
                    buildStatement(copiedMethod.symbol) {
                        irCall(
                            delegateMember as IrSimpleFunction,
                            receiver = getDelegate,
                            valueArguments = copiedMethod.valueParameters
                                .map { param -> irGet(param) }
                                .toTypedArray(),
                            typeArguments = copiedMethod.typeParameters
                                .map { param -> param.defaultType }
                        )
                    }
                )

                copiedMethod
            }

            else -> error("Unsupported member: $member")
        }
    }

    private fun IrEnumEntry.moveDelegatingConstructorCall() {
        val constructorCall = initializerExpression?.expression as IrEnumConstructorCall
        val realConstructorCall = constructorCall.symbol.owner.body?.statements
            ?.firstIsInstanceOrNull<IrTypeOperatorCall>()
            ?.argument as? IrEnumConstructorCall
            ?: return

        initializerExpression = IrExpressionBodyImpl(realConstructorCall)
    }

    private val IrEnumEntry.constructorCall: IrEnumConstructorCall
        get() = (initializerExpression as IrExpressionBodyImpl).expression as IrEnumConstructorCall

    // Move valueOf method to the containing, file rename it to `$EnumName$valueOf`, and give it
    // the proper body.
    private fun DotlinLoweringContext.moveAndImplementValueOf(enum: IrClass, valuesMethod: IrSimpleFunction) {
        val valueOfMethod = enum.getSpecialEnumMember("valueOf")

        val parameter = valueOfMethod.valueParameters.first()

        enum.file.addChild(valueOfMethod)
        enum.declarations.remove(valueOfMethod)
        valueOfMethod.name = Name.identifier("$${enum.simpleDartName}\$valueOf")

        valueOfMethod.body = IrExpressionBodyImpl(
            buildStatement(valueOfMethod.symbol) {
                irCall(
                    irBuiltIns.iterableClass.owner.methodWithName("first"),
                    receiver = irCall(valuesMethod),
                    valueArguments = arrayOf(
                        null,
                        IrFunctionExpressionImpl(
                            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                            type = IrSimpleTypeImpl(
                                irBuiltIns.functionN(1).symbol,
                                hasQuestionMark = false,
                                arguments = listOf(
                                    makeTypeProjection(
                                        irBuiltIns.booleanType,
                                        INVARIANT,
                                    ),
                                    makeTypeProjection(
                                        enum.defaultType,
                                        INVARIANT,
                                    ),
                                ),
                                annotations = emptyList(),
                                abbreviation = null
                            ),
                            origin = IrStatementOrigin.LAMBDA,
                            function = irFactory.buildFun {
                                name = Name.special("<anonymous>")
                                returnType = irBuiltIns.booleanType
                            }.apply {
                                val lambda = this
                                parent = valueOfMethod

                                val localValueParam = IrValueParameterImpl(
                                    SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                                    origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA,
                                    symbol = IrValueParameterSymbolImpl(),
                                    name = Name.identifier("v"),
                                    index = 0,
                                    type = enum.defaultType,
                                    varargElementType = null,
                                    isNoinline = false,
                                    isCrossinline = false,
                                    isAssignable = false,
                                    isHidden = false,
                                ).apply {
                                    parent = lambda
                                }

                                valueParameters = listOf(localValueParam)

                                val getter = enum.propertyWithName("name").getter!!

                                body = IrExpressionBodyImpl(
                                    irEquals(
                                        irGet(
                                            type = getter.returnType,
                                            receiver = irGet(localValueParam),
                                            getter.symbol,
                                        ),
                                        irGet(parameter)
                                    )
                                )
                            }
                        )
                    )
                )
            }
        )
    }

    private fun IrClass.getSpecialEnumMember(name: String) = declarations.first {
        it.origin == IrDeclarationOrigin.ENUM_CLASS_SPECIAL_MEMBER &&
                (it as? IrDeclarationWithName)?.name == Name.identifier(name)
    } as IrSimpleFunction
}