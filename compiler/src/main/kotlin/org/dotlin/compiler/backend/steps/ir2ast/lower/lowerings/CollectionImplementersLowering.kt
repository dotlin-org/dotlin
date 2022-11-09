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

import org.dotlin.compiler.backend.kotlin.Array
import org.dotlin.compiler.backend.kotlin.collections.ImmutableList
import org.dotlin.compiler.backend.kotlin.collections.ImmutableMap
import org.dotlin.compiler.backend.kotlin.collections.ImmutableSet
import org.dotlin.compiler.backend.kotlin.collections.List
import org.dotlin.compiler.backend.kotlin.collections.Map
import org.dotlin.compiler.backend.kotlin.collections.MutableList
import org.dotlin.compiler.backend.kotlin.collections.MutableMap
import org.dotlin.compiler.backend.kotlin.collections.MutableSet
import org.dotlin.compiler.backend.kotlin.collections.Set
import org.dotlin.compiler.backend.kotlin.collections.WriteableList
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrDartDeclarationOrigin
import org.dotlin.compiler.backend.steps.ir2ast.ir.deepCopyWith
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.InterfaceKind.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.InterfaceMutabilityKind.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.MemberKind.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.dotlin.compiler.backend.util.isDotlinExternal
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.toDartIdentifier
import org.jetbrains.kotlin.backend.common.ir.addChild
import org.jetbrains.kotlin.backend.common.ir.setDeclarationsParent
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.util.collectionUtils.filterIsInstanceAnd

/**
 * Missing methods from Dart's List are implemented. For example, `ImmutableList` has no mutable methods. These must
 * be implemented by throwing.
 *
 * Add a special marker interface to classes implementing any Dotlin collection interface to speed up
 * type checking.
 *
 * Must run before [OperatorsLowering], [PropertySimplifyingLowering] and [OverriddenParametersLowering].
 */
@Suppress("UnnecessaryVariable")
class CollectionImplementersLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass || declaration.isDotlinExternal) return noChange()

        val implementedInterfaces = mutableListOf<FqName>()

        // Implement marker interface.
        declaration.superTypes = declaration.superTypes.flatMap {
            with(dartBuiltIns.dotlin) {
                val marker = when (it.classFqName) {
                    ImmutableList -> immutableListMarker
                    WriteableList -> writeableListMarker
                    Array -> fixedSizeListMarker
                    MutableList -> mutableListMarker
                    ImmutableSet -> immutableSetMarker
                    MutableSet -> mutableSetMarker
                    ImmutableMap -> immutableMapMarker
                    MutableMap -> mutableMapMarker
                    else -> return@flatMap listOf(it)
                }

                implementedInterfaces.add(it.classFqName!!)

                listOfNotNull(it, marker.owner.defaultType)
            }
        }

        // Doesn't implement any collection interface, nothing to do here.
        if (implementedInterfaces.isEmpty()) return noChange()
        // Mutable implementations don't miss anything.
        if (implementedInterfaces.any { it == MutableList || it == MutableSet || it == MutableMap }) return noChange()

        val mutableCollectionMembers = setOf(
            MemberInfo("add", ADD),
            MemberInfo("addAll", ADD),
            MemberInfo("clear", CLEAR),
            MemberInfo("length", SIZE, property = true),
            MemberInfo("removeWhere", REMOVE),
            MemberInfo("retainWhere", REMOVE),
            MemberInfo("remove", REMOVE),
        )

        // Make precedence for selecting certain data: List > Set > Map, and
        // Writeable > Array > regular List > Immutable.
        val interfacesByPrecedence = listOf(
            WriteableList,
            Array,
            List,
            ImmutableList,
            Set,
            ImmutableSet,
            Map,
            ImmutableMap,
        ).mapIndexed { index, fqName -> fqName to index }.toMap()

        implementedInterfaces.sortBy { interfacesByPrecedence[it] }

        // Also acts as a precedence, if the implementer implements List AND Set, the throwing
        // message will refer to it as 'list'.
        val interfaceKinds = implementedInterfaces
            .mapNotNull {
                when (it) {
                    List, ImmutableList, WriteableList, Array -> LIST
                    Set, ImmutableSet -> SET
                    Map, ImmutableMap -> MAP
                    else -> null
                }
            }

        addMissingMethods(
            InterfaceTargetInfo(
                owner = declaration,
                kind = interfaceKinds.first(),
                mutabilityKind = implementedInterfaces.firstNotNullOf {
                    when (it) {
                        WriteableList -> WRITEABLE
                        Array -> FIXED_SIZE
                        List, Set, Map -> READ_ONLY
                        ImmutableList, ImmutableSet, ImmutableMap -> IMMUTABLE
                        else -> null
                    }
                }
            ),
            fullInterfaces = interfaceKinds.map {
                when (it) {
                    LIST -> irBuiltIns.mutableListClass.owner
                    SET -> irBuiltIns.mutableSetClass.owner
                    MAP -> irBuiltIns.mutableMapClass.owner
                }
            },
            possibleMissingMembersInfo = interfaceKinds.flatMap {
                when (it) {
                    LIST -> mutableCollectionMembers + setOf(
                        // Writeable
                        MemberInfo("first", MODIFY, property = true),
                        MemberInfo("last", MODIFY, property = true),
                        MemberInfo("set", MODIFY),
                        MemberInfo("setAll", MODIFY),
                        MemberInfo("setRange", MODIFY),
                        MemberInfo("fillRange", MODIFY),
                        MemberInfo("sort", MODIFY),
                        MemberInfo("shuffle", MODIFY),
                        // Mutable
                        MemberInfo("insert", ADD),
                        MemberInfo("insertAll", ADD),
                        MemberInfo("removeAt", REMOVE),
                        MemberInfo("removeLast", REMOVE),
                        MemberInfo("removeRange", REMOVE),
                        MemberInfo("replaceRange", REMOVE),
                    )
                    SET -> mutableCollectionMembers + setOf(
                        MemberInfo("removeAll", REMOVE),
                        MemberInfo("retainAll", REMOVE)
                    )
                    MAP -> TODO()
                }
            }.toSet()
        )

        return noChange()
    }

    // TODO: Add analyzer check for methods that have the same name and signature of List, but are not
    // present in the Kotlin interface. For example, `MyList` implementing `List<Int>, but adding a
    // `add(element: Int)` method.
    // Error if: Same name as Dart mutable List method but different signature
    // Warning if: Same name AND same signature as Dart mutable List method

    private fun DartLoweringContext.addMissingMethods(
        targetInfo: InterfaceTargetInfo,
        fullInterfaces: List<IrClass>,
        possibleMissingMembersInfo: Set<MemberInfo>,
    ) {
        val target = targetInfo.owner

        fun List<IrDeclaration>.byDartNames() = mapNotNull {
            when (it) {
                is IrDeclarationWithName -> when (val dartName = it.dartNameOrNull) {
                    null -> null
                    else -> dartName to it
                }
                else -> null
            }
        }


        val allFullInterfaceMembers = fullInterfaces.flatMap { it.declarations.byDartNames() }.toMap()
        val allTargetMembers = target.declarations.byDartNames().toMap()

        // Properties are also considered missing if they're present but not var.
        val dartNamesOfMissingProperties = run {
            val propertyInfos = possibleMissingMembersInfo.filter { it.isProperty }

            propertyInfos.mapNotNull {
                when (val declaration = allTargetMembers[it.name]) {
                    null -> it.name
                    else -> {
                        declaration as IrProperty
                        when {
                            declaration.isVar -> null
                            else -> it.name
                        }
                    }
                }
            }
        }

        val missingMembers = allFullInterfaceMembers.keys
            .subtract(allTargetMembers.keys)
            .plus(dartNamesOfMissingProperties)
            .map { it to allFullInterfaceMembers[it]!! }
            .mapNotNull { (name, member) ->
                when (val memberInfo = possibleMissingMembersInfo.firstOrNull { it.name == name }) {
                    null -> null
                    else -> memberInfo to member
                }
            }
            .toMap()

        missingMembers.forEach { (info, member) ->
            fun throwingBody(container: IrSymbol): IrBlockBody {
                return IrBlockBodyImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET) {
                    statements += buildStatement(container) {
                        irThrow(
                            IrConstructorCallImpl(
                                SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                                type = dartBuiltIns.unsupportedError.defaultType,
                                symbol = dartBuiltIns.unsupportedError.owner.primaryConstructor!!.symbol,
                                valueArgumentsCount = 1,
                                typeArgumentsCount = 0,
                                constructorTypeArgumentsCount = 0
                            ).apply {
                                val verb = info.kind.display
                                val anAdjective = targetInfo.mutabilityKind.display
                                val noun = targetInfo.kind.display
                                putValueArgument(
                                    index = 0,
                                    "Cannot $verb $anAdjective $noun".toIrConst(context.irBuiltIns.stringType)
                                )
                            }
                        )
                    }
                }
            }

            when (info.name.value) {
                // Special case for some properties: It becomes a var instead of val.
                // The setter is copied to the existing property.
                "length", "first", "last" -> {
                    member as IrProperty
                    val copiedSetter = member.setter!!.deepCopyWith(remapReferences = false) {
                        isFakeOverride = false
                    }

                    var indexOfOriginal: Int = -1
                    val sourceProperty = target.declarations
                        .filterIsInstanceAnd<IrProperty> { prop -> prop.dartNameOrNull == info.name }
                        .firstOrNull()
                        // If found on the target, we remove it.
                        ?.also {
                            indexOfOriginal = target.declarations.indexOf(it)
                            target.declarations.removeAt(indexOfOriginal)
                        }
                        ?: member

                    val newProperty = sourceProperty.deepCopyWith(remapReferences = sourceProperty != member) {
                        isVar = true
                    }
                    newProperty.setter = copiedSetter.apply {
                        parent = newProperty.parent
                        body = throwingBody(copiedSetter.symbol)
                    }

                    when (indexOfOriginal) {
                        -1 -> target.addChild(newProperty)
                        else -> target.declarations.add(indexOfOriginal, newProperty).also {
                            newProperty.setDeclarationsParent(target)
                        }
                    }
                }
                else -> {
                    val copy = when (member) {
                        is IrProperty -> member.deepCopyWith(remapReferences = false) {
                            isFakeOverride = false
                            origin = IrDartDeclarationOrigin.COPIED_OVERRIDE
                        }.apply {
                            overriddenSymbols = listOf(member.symbol)
                        }
                        is IrSimpleFunction -> member.deepCopyWith(remapReferences = false) {
                            isFakeOverride = false
                            origin = IrDartDeclarationOrigin.COPIED_OVERRIDE
                        }.apply {
                            overriddenSymbols = listOf(member.symbol)
                            body = throwingBody(symbol)
                        }
                        else -> throw UnsupportedOperationException("Unsupported collection member: ${member.render()}")
                    }

                    target.addChild(copy)
                }
            }
        }
    }
}

private data class InterfaceTargetInfo(
    val owner: IrClass,
    val kind: InterfaceKind,
    val mutabilityKind: InterfaceMutabilityKind,
)

private enum class InterfaceKind(val display: String) {
    LIST("list"),
    SET("set"),
    MAP("map"),
}

private enum class InterfaceMutabilityKind(val display: String) {
    WRITEABLE("a writeable"),
    FIXED_SIZE("a fixed-size"),
    IMMUTABLE("an immutable"),
    READ_ONLY("a read-only")
}

private data class MemberInfo(val name: DartIdentifier, val kind: MemberKind, val isProperty: Boolean) {
    constructor(name: String, kind: MemberKind, property: Boolean = false)
            : this(name.toDartIdentifier(), kind, property)
}

private enum class MemberKind(val display: String) {
    REMOVE("remove from"),
    ADD("add to"),
    CLEAR("clear"),
    MODIFY("modify"),
    SIZE("change the size of")
}
