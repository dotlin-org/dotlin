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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrDartDeclarationOrigin
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.dartNameAsSimple
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.dartNameWith
import org.dotlin.compiler.backend.util.sentenceCase
import org.jetbrains.kotlin.backend.common.ir.copyTypeParameters
import org.jetbrains.kotlin.backend.common.ir.createParameterDeclarations
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.isGetter
import org.jetbrains.kotlin.ir.util.isSetter
import org.jetbrains.kotlin.name.Name

class ExtensionsLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    private val extensionContainers = mutableMapOf<String, IrClass>()

    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        val extensionReceiver = when (declaration) {
            is IrFunction -> when {
                !declaration.isGetter && !declaration.isSetter -> declaration.extensionReceiverParameter
                else -> return noChange()
            }
            is IrProperty -> declaration.getter?.extensionReceiverParameter
            else -> return noChange()
        } ?: return noChange()

        // Any type parameters used to declare the receiver type go to the top of the Dart extension declaration, and
        // are removed from the function type parameters.
        val receiverTypeParameters = when (val extensionReceiverType = extensionReceiver.type) {
            is IrSimpleType -> {
                when (val classifier = extensionReceiverType.classifierOrNull?.owner) {
                    is IrTypeParameter -> listOf(classifier)
                    else -> when (declaration) {
                        is IrFunction -> declaration.typeParameters
                            .filter {
                                extensionReceiverType.arguments.any { arg ->
                                    (arg.typeOrNull as? IrSimpleType)?.classifier == it.symbol
                                }
                            }
                        else -> emptyList()
                    }
                }
            }
            else -> emptyList()
        }

        if (declaration is IrFunction) {
            declaration.typeParameters -= receiverTypeParameters
        }

        val extensionContainerName = extensionReceiver.type.getExtensionContainerName(
            currentFile = declaration.file,
            hasExtensionTypeArguments = receiverTypeParameters.isNotEmpty()
        )

        val extensionContainer = extensionContainers.getOrPut(extensionContainerName) {
            irFactory.buildClass {
                origin = IrDartDeclarationOrigin.EXTENSION
                name = Name.identifier(extensionContainerName)
            }.apply {
                copyTypeParameters(receiverTypeParameters)

                declaration.file.let {
                    parent = it
                    it.declarations.add(this)
                }
                createParameterDeclarations()
            }
        }

        extensionContainer.declarations.add(declaration)

        return just { remove() }
    }

    private fun IrType.getExtensionContainerName(currentFile: IrFile, hasExtensionTypeArguments: Boolean): String {
        val (file, mainName) = when (val classifier = classifierOrNull?.owner) {
            is IrClass -> classifier.file to classifier.dartNameAsSimple
            is IrTypeParameter -> classifier.file to classifier.dartNameWith(superTypes = true)
            else -> throw UnsupportedOperationException("Cannot handle extension for $classifier yet")
        }
        val prefix = '$'
        val packagePrefix = when {
            file != currentFile -> file.fqName.pathSegments()
                .map { it.identifier }
                .joinToString { it.sentenceCase() }
            else -> ""
        }
        val typeArguments = when {
            !hasExtensionTypeArguments && this is IrSimpleType -> arguments
                .mapNotNull { it.typeOrNull?.classOrNull?.owner?.dartNameAsSimple?.value }
                .joinToString { it.sentenceCase() }
            else -> ""
        }
        val suffix = "Extensions"

        return "$prefix$packagePrefix$mainName$typeArguments$suffix"
    }
}