/*
 * Copyright 2022 Wilko Manger
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

package org.dotlin.compiler.backend.steps.src2ir.analyze.ir

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclaration


//region DiagnosticFactory1 on extensions
@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement> DiagnosticFactory1<E, DeclarationDescriptor>.on(
    element: E,
    a: IrDeclaration
): ParametrizedDiagnostic<E> = on(element, a.descriptor)
//endregion

//region DiagnosticFactory2 on extensions
@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, B : Any> DiagnosticFactory2<E, DeclarationDescriptor, B>.on(
    element: E,
    a: IrDeclaration,
    b: B
): ParametrizedDiagnostic<E> = on(element, a.descriptor, b)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, A : Any> DiagnosticFactory2<E, A, DeclarationDescriptor>.on(
    element: E,
    a: A,
    b: IrDeclaration
): ParametrizedDiagnostic<E> = on(element, a, b.descriptor)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement> DiagnosticFactory2<E, DeclarationDescriptor, DeclarationDescriptor>.on(
    element: E,
    a: IrDeclaration,
    b: IrDeclaration
): ParametrizedDiagnostic<E> = on(element, a.descriptor, b.descriptor)
//endregion

//region DiagnosticFactory3 on extensions
@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, B : Any, C : Any> DiagnosticFactory3<E, DeclarationDescriptor, B, C>.on(
    element: E,
    a: IrDeclaration,
    b: B,
    c: C
): ParametrizedDiagnostic<E> = on(element, a.descriptor, b, c)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, A : Any, C : Any> DiagnosticFactory3<E, A, DeclarationDescriptor, C>.on(
    element: E,
    a: A,
    b: IrDeclaration,
    c: C
): ParametrizedDiagnostic<E> = on(element, a, b.descriptor, c)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, A : Any, B : Any> DiagnosticFactory3<E, A, B, DeclarationDescriptor>.on(
    element: E,
    a: A,
    b: B,
    c: IrDeclaration
): ParametrizedDiagnostic<E> = on(element, a, b, c.descriptor)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, C : Any> DiagnosticFactory3<E, DeclarationDescriptor, DeclarationDescriptor, C>.on(
    element: E,
    a: IrDeclaration,
    b: IrDeclaration,
    c: C
): ParametrizedDiagnostic<E> = on(element, a.descriptor, b.descriptor, c)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement> DiagnosticFactory3<E, DeclarationDescriptor, DeclarationDescriptor, DeclarationDescriptor>.on(
    element: E,
    a: IrDeclaration,
    b: IrDeclaration,
    c: IrDeclaration
): ParametrizedDiagnostic<E> = on(element, a.descriptor, b.descriptor, c.descriptor)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, A : Any> DiagnosticFactory3<E, A, DeclarationDescriptor, DeclarationDescriptor>.on(
    element: E,
    a: A,
    b: IrDeclaration,
    c: IrDeclaration
): ParametrizedDiagnostic<E> = on(element, a, b.descriptor, c.descriptor)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, B : Any> DiagnosticFactory3<E, DeclarationDescriptor, B, DeclarationDescriptor>.on(
    element: E,
    a: IrDeclaration,
    b: B,
    c: IrDeclaration
): ParametrizedDiagnostic<E> = on(element, a.descriptor, b, c.descriptor)
//endregion

//region DiagnosticFactory4 on extensions
@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, B : Any, C : Any, D : Any> DiagnosticFactory4<E, DeclarationDescriptor, B, C, D>.on(
    element: E,
    a: IrDeclaration,
    b: B,
    c: C,
    d: D
): ParametrizedDiagnostic<E> = on(element, a.descriptor, b, c, d)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, A : Any, C : Any, D : Any> DiagnosticFactory4<E, A, DeclarationDescriptor, C, D>.on(
    element: E,
    a: A,
    b: IrDeclaration,
    c: C,
    d: D
): ParametrizedDiagnostic<E> = on(element, a, b.descriptor, c, d)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, A : Any, B : Any, D : Any> DiagnosticFactory4<E, A, B, DeclarationDescriptor, D>.on(
    element: E,
    a: A,
    b: B,
    c: IrDeclaration,
    d: D
): ParametrizedDiagnostic<E> = on(element, a, b, c.descriptor, d)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, A : Any, B : Any, C : Any> DiagnosticFactory4<E, A, B, C, DeclarationDescriptor>.on(
    element: E,
    a: A,
    b: B,
    c: C,
    d: IrDeclaration
): ParametrizedDiagnostic<E> = on(element, a, b, c, d.descriptor)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, A : Any, B : Any> DiagnosticFactory4<E, A, B, DeclarationDescriptor, DeclarationDescriptor>.on(
    element: E,
    a: A,
    b: B,
    c: IrDeclaration,
    d: IrDeclaration
): ParametrizedDiagnostic<E> = on(element, a, b, c.descriptor, d.descriptor)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, A : Any> DiagnosticFactory4<E, A, DeclarationDescriptor, DeclarationDescriptor, DeclarationDescriptor>.on(
    element: E,
    a: A,
    b: IrDeclaration,
    c: IrDeclaration,
    d: IrDeclaration
): ParametrizedDiagnostic<E> = on(element, a, b.descriptor, c.descriptor, d.descriptor)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement> DiagnosticFactory4<E, DeclarationDescriptor, DeclarationDescriptor, DeclarationDescriptor, DeclarationDescriptor>.on(
    element: E,
    a: IrDeclaration,
    b: IrDeclaration,
    c: IrDeclaration,
    d: IrDeclaration
): ParametrizedDiagnostic<E> = on(element, a.descriptor, b.descriptor, c.descriptor, d.descriptor)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, B : Any, C : Any> DiagnosticFactory4<E, DeclarationDescriptor, B, C, DeclarationDescriptor>.on(
    element: E,
    a: IrDeclaration,
    b: B,
    c: C,
    d: IrDeclaration
): ParametrizedDiagnostic<E> = on(element, a.descriptor, b, c, d.descriptor)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, C : Any> DiagnosticFactory4<E, DeclarationDescriptor, DeclarationDescriptor, C, DeclarationDescriptor>.on(
    element: E,
    a: IrDeclaration,
    b: IrDeclaration,
    c: C,
    d: IrDeclaration
): ParametrizedDiagnostic<E> = on(element, a.descriptor, b.descriptor, c, d.descriptor)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, A : Any, D : Any> DiagnosticFactory4<E, A, DeclarationDescriptor, DeclarationDescriptor, D>.on(
    element: E,
    a: A,
    b: IrDeclaration,
    c: IrDeclaration,
    d: D
): ParametrizedDiagnostic<E> = on(element, a, b.descriptor, c.descriptor, d)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, D : Any> DiagnosticFactory4<E, DeclarationDescriptor, DeclarationDescriptor, DeclarationDescriptor, D>.on(
    element: E,
    a: IrDeclaration,
    b: IrDeclaration,
    c: IrDeclaration,
    d: D
): ParametrizedDiagnostic<E> = on(element, a.descriptor, b.descriptor, c.descriptor, d)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, C : Any, D : Any> DiagnosticFactory4<E, DeclarationDescriptor, DeclarationDescriptor, C, D>.on(
    element: E,
    a: IrDeclaration,
    b: IrDeclaration,
    c: C,
    d: D
): ParametrizedDiagnostic<E> = on(element, a.descriptor, b.descriptor, c, d)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun <E : PsiElement, B : Any, D : Any> DiagnosticFactory4<E, DeclarationDescriptor, B, DeclarationDescriptor, D>.on(
    element: E,
    a: IrDeclaration,
    b: B,
    c: IrDeclaration,
    d: D
): ParametrizedDiagnostic<E> = on(element, a.descriptor, b, c.descriptor, d)
//endregion