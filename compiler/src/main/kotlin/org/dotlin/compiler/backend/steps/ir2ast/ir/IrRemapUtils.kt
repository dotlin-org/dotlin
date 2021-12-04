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

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.assertCast
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

@JvmName("remapDeclarationReference")
fun IrElement.remap(mapping: Pair<IrDeclaration, IrDeclaration>) =
    remap(mapOf(mapping))

@JvmName("remapDeclarationReference")
fun IrElement.remap(mapping: Map<out IrDeclaration, IrDeclaration>) {
    require(mapping.all { (old, new) -> new::class.isInstance(old) }) { "Remapping types must match." }
    transformChildrenVoid(DeclarationReferenceRemapper(mapping))
    transformChildrenVoid(
        TypeRemapper(
            mapping.entries
                .filter { (old, new) -> old is IrClass && new is IrClass }
                .map { (old, new) -> old as IrClass to new as IrClass }
                .map { (old, new) -> old.defaultType to new.defaultType }
                .toMap()
        )
    )
}

private class DeclarationReferenceRemapper(
    private val mapping: Map<out IrDeclaration, IrDeclaration>
) : IrElementTransformerVoid() {
    override fun visitValueAccess(expression: IrValueAccessExpression): IrExpression {
        return mapping[expression.symbol.owner]
            ?.assertCast<IrValueDeclaration>()
            ?.let {
                return when (expression) {
                    is IrGetValue -> IrGetValueImpl(
                        expression.startOffset,
                        expression.endOffset,
                        it.type,
                        it.symbol,
                        expression.origin
                    )
                    is IrSetValue -> IrSetValueImpl(
                        expression.startOffset,
                        expression.endOffset,
                        it.type,
                        it.symbol,
                        expression.value,
                        expression.origin
                    )
                    else -> throw UnsupportedOperationException("Unsupported value access: $expression")
                }
            } ?: super.visitValueAccess(expression)
    }

    override fun visitDeclarationReference(expression: IrDeclarationReference): IrExpression {
        val transformed = mapping[expression.symbol.owner]?.let { irDeclaration ->
            expression.deepCopyWithSymbols(
                initialParent = irDeclaration as IrDeclarationParent,
                symbolRemapper = SymbolOwnerRemapper(mapping),
            )
        } ?: super.visitDeclarationReference(expression)

        return transformed.also { it.transformChildrenVoid() }
    }

    override fun visitMemberAccess(expression: IrMemberAccessExpression<*>) =
        visitDeclarationReference(expression)

    override fun visitField(declaration: IrField): IrStatement {
        // This might be necessary because we remove IrProperties from the IR tree,
        // meaning visitProperty is never called.
        declaration.correspondingProperty?.run {
            backingField?.let {
                (mapping[it] as? IrField)?.let { newBackingField ->
                    backingField = newBackingField
                }
            }
        }

        return super.visitField(declaration)
    }
}

inline fun <reified T : IrElement> T.deepCopyWithSymbols(
    initialParent: IrDeclarationParent? = null,
    symbolRemapper: DeepCopySymbolRemapper,
): T {
    acceptVoid(symbolRemapper)
    return transform(DeepCopyIrTreeWithSymbols(symbolRemapper, DeepCopyTypeRemapper(symbolRemapper)), null)
        .patchDeclarationParents(initialParent) as T
}

private class SymbolOwnerRemapper(
    private val mapping: Map<out IrSymbolOwner, IrSymbolOwner>
) : DeepCopySymbolRemapper() {
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
                .filter { it.key.symbol is S } // it.value is guaranteed to be S as well.
                .map { Pair(it.key.symbol as S, it.value.symbol as S) }
                .toMap()
        )
    }

}

private class TypeRemapper(
    private val mapping: Map<out IrType, IrType?>
) : IrElementTransformerVoid() {
    override fun visitExpression(expression: IrExpression) = expression.apply {
        mapping[type]?.let {
            type = it
        }
        transformChildrenVoid()
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall) = expression.apply {
        mapping[typeOperand]?.let {
            typeOperand = it
        }
        transformChildrenVoid()
    }
}

fun IrDeclarationParent.remapDeclarationParents(from: IrDeclarationParent) =
    transformChildrenVoid(ParentRemapper(from, this))

fun IrDeclaration.remapDeclarationParents(mapping: Pair<IrDeclarationParent, IrDeclarationParent>) =
    transformChildrenVoid(ParentRemapper(mapping.first, mapping.second))

private class ParentRemapper(
    private val old: IrDeclarationParent,
    private val new: IrDeclarationParent
) : IrElementTransformerVoid() {
    override fun visitDeclaration(declaration: IrDeclarationBase): IrStatement {
        if (declaration.parent == old) declaration.parent = new
        declaration.transformChildrenVoid()
        return super.visitDeclaration(declaration)
    }
}