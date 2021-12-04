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

@file:Suppress("DIFFERENT_NAMES_FOR_THE_SAME_PARAMETER_IN_SUPERTYPES", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package org.dotlin.compiler.backend.steps.ir2ast.lower

import org.dotlin.compiler.backend.steps.ir2ast.ir.remap
import org.dotlin.compiler.backend.steps.ir2ast.ir.singleOrNullIfEmpty
import org.dotlin.compiler.backend.steps.ir2ast.lower.RemapLevel.*
import org.dotlin.compiler.backend.steps.replace
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.visitors.*

typealias Transform<E> = (E) -> Transformations<E>

typealias Transformations<E> = Sequence<Transformation<E>>

interface IrTransformer<E : IrElement, R> {
    fun transform(element: E): R
}

interface IrSingleTransformer<E : IrElement> : IrTransformer<E, Transformation<E>?>

interface IrMultipleTransformer<E : IrElement> : IrTransformer<E, Transformations<E>>

interface IrDeclarationTransformer : IrMultipleTransformer<IrDeclaration>, FileLoweringPass {
    override fun lower(irFile: IrFile) = irFile.transformDeclarations(::transform)

    override fun transform(declaration: IrDeclaration): Transformations<IrDeclaration>
}

interface IrExpressionTransformer : IrSingleTransformer<IrExpression>, FileLoweringPass {
    override fun lower(irFile: IrFile) {
        irFile.transformChildren(
            object : IrElementTransformer<IrDeclarationParent> {
                override fun visitDeclaration(
                    declaration: IrDeclarationBase,
                    parent: IrDeclarationParent
                ): IrStatement {
                    val newParent = if (declaration is IrDeclarationParent) declaration else parent

                    return super.visitDeclaration(declaration, newParent)
                }

                override fun visitExpression(expression: IrExpression, parent: IrDeclarationParent): IrExpression {
                    expression.transformChildren(this, parent)

                    return when (val transformation = transform(expression, parent)) {
                        is Transformation.Replace -> {
                            if (transformation.old != null) {
                                throw UnsupportedOperationException("Cannot replace another expression")
                            }

                            transformation.new
                        }
                        is Transformation.Add -> throw UnsupportedOperationException("Cannot add an expression")
                        is Transformation.Remove -> throw UnsupportedOperationException("Cannot remove an expression")
                        else -> expression
                    }
                }
            },
            irFile
        )
    }

    override fun transform(expression: IrExpression): Transformation<IrExpression>? = noChange()
    fun transform(expression: IrExpression, containerParent: IrDeclarationParent): Transformation<IrExpression>? =
        transform(expression)
}

interface IrStatementTransformer : IrMultipleTransformer<IrStatement>, FileLoweringPass {
    override fun lower(irFile: IrFile) {
        irFile.acceptChildrenVoid(object : IrElementVisitorVoid {
            override fun visitBlockBody(body: IrBlockBody) {
                super.visitBlockBody(body)

                var i = 0

                body.statements.toList().forEach { statement ->
                    transform(statement, body).forEach { transformation ->
                        i += body.statements.transform(transformation, at = i)
                    }

                    i++
                }
            }

            override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)
        })
    }

    override fun transform(element: IrStatement) = throw UnsupportedOperationException("This should not be used.")

    fun transform(statement: IrStatement, body: IrBlockBody): Transformations<IrStatement>
}

interface IrBodyExpressionTransformer : IrSingleTransformer<IrExpression>, FileLoweringPass {
    override fun lower(irFile: IrFile) {
        irFile.acceptChildrenVoid(object : IrElementVisitorVoid {
            override fun visitExpressionBody(body: IrExpressionBody) {
                super.visitExpressionBody(body)

                @Suppress("UNCHECKED_CAST")
                body.expression.transformSingle({ exp ->
                    transform(exp, body)?.let {
                        require(it !is Transformation.Remove) { "Cannot remove expression from expression body" }
                        sequenceOf(it)
                    } ?: emptySequence()
                }) {
                    body.expression = it!!
                }
            }

            override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)
        })
    }

    override fun transform(element: IrExpression) = throw UnsupportedOperationException("This should not be used.")

    fun transform(expression: IrExpression, body: IrExpressionBody): Transformation<IrExpression>?
}

interface IrStatementAndBodyExpressionTransformer : FileLoweringPass {
    val statementTransformer: IrStatementTransformer
    val bodyExpressionTransformer: IrBodyExpressionTransformer

    override fun lower(irFile: IrFile) {
        statementTransformer.lower(irFile)
        bodyExpressionTransformer.lower(irFile)
    }
}

fun IrDeclarationContainer.transformDeclarations(block: Transform<IrDeclaration>) {
    var i = 0
    declarations.toList().forEach { irDeclaration ->
        block(irDeclaration).forEach {
            i += declarations.transform(parent = this as IrDeclarationParent, it, at = i)
        }

        when (irDeclaration) {
            is IrProperty -> irDeclaration.transform(block)
            is IrFunction -> irDeclaration.transform(block)
        }

        i++

        if (irDeclaration is IrDeclarationContainer) {
            irDeclaration.transformDeclarations(block)
        }

        // We check for local declarations and expressions with declarations.
        irDeclaration.transformChildrenVoid(
            object : IrElementTransformerVoid() {
                override fun visitBody(body: IrBody): IrBody {
                    if (body !is IrBlockBody) return body

                    body.statements.apply {
                        filterIsInstance<IrDeclaration>().forEach { localDeclaration ->
                            block(localDeclaration).forEach { transformation ->
                                when (transformation) {
                                    is Transformation.Replace<IrDeclaration> -> {
                                        val old = transformation.old ?: localDeclaration
                                        replace(old, transformation.new)
                                    }
                                    is Transformation.Add -> add(indexOf(localDeclaration), transformation.element)
                                    is Transformation.Remove -> remove(localDeclaration)
                                }

                                if (localDeclaration is IrDeclarationContainer) {
                                    localDeclaration.transformDeclarations(block)
                                    // We don't want to visit bodies with this transformer, otherwise we'll have an
                                    // in infinite loop.
                                    return@visitBody body
                                }
                            }
                        }
                    }

                    return super.visitBody(body)
                }

                override fun visitFunctionExpression(expression: IrFunctionExpression): IrExpression {
                    expression.function.transformSingle(block) { expression.function = it!! }

                    return super.visitFunctionExpression(expression)
                }
            }
        )
    }
}

private fun <E : IrDeclaration> Transformation<E>.setParents(parent: IrDeclarationParent) {
    when (this) {
        is Transformation.Replace -> new.parent = parent
        is Transformation.Add -> element.parent = parent
        else -> {}
    }
}

private fun <E : IrDeclaration> Transformation<E>.remapReferences(
    old: () -> IrDeclaration
) {
    if (this !is Transformation.Replace) return

    val parent = when (remapAt) {
        NONE -> return
        PARENT -> new.parent
        CLASS -> new.parentClassOrNull ?: return
        FILE -> new.file
    }

    parent.remap((this.old ?: old()) to new)

    // IrValueParameters can be referenced in the class, remap them as well.
    val actualOld = this.old ?: old()
    if (actualOld is IrConstructor && new is IrConstructor) {
        actualOld.valueParameters.zip((new as IrConstructor).valueParameters)
            .map { (oldParam, newParam) -> Transformation.Replace(oldParam, newParam, remapAt = CLASS) }
            .forEach { it.remapReferences { it.old!! } }
    }
}

@JvmName("transformDeclarations")
private fun MutableList<IrDeclaration>.transform(
    parent: IrDeclarationParent,
    transformation: Transformation<IrDeclaration>,
    at: Int
): Int {
    transformation.setParents(parent)
    transformation.remapReferences(old = { this[at] })

    @Suppress("UNCHECKED_CAST")
    return (this as MutableList<IrElement>).transform(transformation as Transformation<IrElement>, at)
}

@JvmName("transformElements")
private fun <E : IrElement> MutableList<E>.transform(transformation: Transformation<E>, at: Int): Int {
    var i = 0

    when (transformation) {
        is Transformation.Replace -> {
            val oldIndex = transformation.old?.let { indexOf(it) }

            when {
                oldIndex == null -> this[at] = transformation.new
                oldIndex >= 0 -> this[oldIndex] = transformation.new
            }
        }
        is Transformation.Add -> {
            // Add the element after this one.
            add(at + 1, transformation.element)
            i = 1
        }
        is Transformation.Remove -> {
            when (transformation.element) {
                null -> removeAt(at)
                else -> remove(transformation.element)
            }
            i = -1
        }
    }

    return i
}

private fun IrProperty.transform(block: Transform<IrDeclaration>) {
    getter?.transformSingle(block) { getter = it }
    setter?.transformSingle(block) { setter = it }
    backingField?.transformSingle(block) { backingField = it }
}

@Suppress("UNCHECKED_CAST")
private fun IrFunction.transform(block: Transform<IrDeclaration>) {
    val mutableValueParameters = valueParameters.toMutableList() as MutableList<IrDeclaration>
    valueParameters.forEachIndexed { index, element ->
        block(element).singleOrNullIfEmpty()?.let {
            mutableValueParameters.transform(
                parent = element.parent,
                it,
                at = index
            )
        }
    }

    valueParameters = mutableValueParameters as MutableList<IrValueParameter>
}

/**
 * Applies the transformation to a field.
 */
@Suppress("UNCHECKED_CAST")
private inline fun <reified E1 : E2, reified E2 : IrElement> E1.transformSingle(
    block: Transform<E2>,
    setBlock: (E1?) -> Unit
) {
    val transformation = block(this)
        .singleOrNullIfEmpty { "Cannot transform single element with multiple transformations" } ?: return

    if (transformation is Transformation.Replace && transformation.new is IrDeclaration) {
        (transformation as Transformation.Replace<IrDeclaration>).remapReferences { this as IrDeclaration }
    }

    transformation.replacementOf(this, isSubject = true)?.let {
        if (it is E1) {
            setBlock(it)
            return
        }
    }

    if (transformation.isRemoval(of = this, isSubject = true)) {
        setBlock(null)
        return
    }
}

@Suppress("UNCHECKED_CAST")
private fun <E : IrElement, T : Transformation<E>> T.replacementOf(element: E, isSubject: Boolean = false) = when {
    this is Transformation.Replace<*> && ((isSubject && old == null) || old === element) -> new as E
    else -> null
}

@Suppress("UNCHECKED_CAST")
private fun <E : IrElement, T : Transformation<E>> T.isRemoval(of: E, isSubject: Boolean = false) =
    this is Transformation.Remove<*> && ((isSubject && element == null) || element === of)

sealed class Transformation<E : IrElement> {
    class Replace<E : IrElement>(
        val old: E?,
        val new: E,
        val remapAt: RemapLevel = FILE
    ) : Transformation<E>()

    class Add<E : IrElement>(val element: E) : Transformation<E>()

    class Remove<E : IrElement>(val element: E? = null) : Transformation<E>()
}

enum class RemapLevel {
    NONE,
    PARENT,
    CLASS,
    FILE,
}

infix fun <E : IrElement> Transformation<E>.and(other: Transformation<E>) = sequenceOf(this, other)
infix fun <E : IrElement> Transformation<E>.and(other: Sequence<Transformation<E>>) =
    sequenceOf(sequenceOf(this), other).flatten()

infix fun <E : IrElement> Transformation<E>.and(other: Iterable<Transformation<E>>) = and(other.asSequence())

infix fun <E : IrElement> Sequence<Transformation<E>>.and(other: Transformation<E>) = this + other
infix fun <E : IrElement> Sequence<Transformation<E>>.and(other: Sequence<Transformation<E>>) = this + other
infix fun <E : IrElement> Sequence<Transformation<E>>.and(other: Iterable<Transformation<E>>) = this + other

infix fun <E : IrElement> Iterable<Transformation<E>>.and(other: Transformation<E>) = asSequence().and(other)
infix fun <E : IrElement> Iterable<Transformation<E>>.and(other: Iterable<Transformation<E>>) =
    asSequence().and(other.asSequence())

infix fun <E : IrElement> Iterable<Transformation<E>>.and(other: Sequence<Transformation<E>>) =
    this.asSequence().and(other)

fun <E : IrElement> IrTransformer<E, *>.just(transformation: Transformation<E>) = sequenceOf(transformation)
fun <E : IrElement> IrTransformer<E, *>.just(transformation: () -> Transformation<E>) = just(transformation())

fun <E : IrElement> IrTransformer<E, *>.replace(element: E, with: E, remapAt: RemapLevel = FILE) =
    Transformation.Replace(old = element, new = with)

fun <E : IrElement> IrTransformer<E, *>.replaceWith(element: E, remapAt: RemapLevel = FILE) =
    Transformation.Replace(old = null, new = element, remapAt = remapAt)

fun <E : IrElement> IrTransformer<E, *>.add(element: E) = Transformation.Add(element)
fun <E : IrElement> IrTransformer<E, *>.remove(element: E? = null) = Transformation.Remove(element)

fun <E : IrElement> IrSingleTransformer<E>.noChange(): Transformation<E>? = null
fun <E : IrElement> IrMultipleTransformer<E>.noChange() = emptySequence<Transformation<E>>()

