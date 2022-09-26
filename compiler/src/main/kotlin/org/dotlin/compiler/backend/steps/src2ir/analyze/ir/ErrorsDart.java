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

package org.dotlin.compiler.backend.steps.src2ir.analyze.ir;

import com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.diagnostics.*;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtExpression;
import org.jetbrains.kotlin.types.KotlinType;

import static org.jetbrains.kotlin.diagnostics.PositioningStrategies.DECLARATION_SIGNATURE_OR_DEFAULT;
import static org.jetbrains.kotlin.diagnostics.PositioningStrategies.DEFAULT;

// This MUST be in Java, because the initializer code uses Java reflection.

public interface ErrorsDart {
    // TODO: Change third type arg back to DeclarationDescriptor, as soon as the
    // rendered isn't broken anymore (would break when rendering properties).
    DiagnosticFactory2<KtDeclaration, String, String> DART_NAME_CLASH =
            DiagnosticFactory2.create(Severity.ERROR, DECLARATION_SIGNATURE_OR_DEFAULT);

    DiagnosticFactory0<KtDeclaration> EXTENSION_WITHOUT_EXPLICIT_DART_EXTENSION_NAME_IN_PUBLIC_PACKAGE =
            DiagnosticFactory0.create(Severity.WARNING, DECLARATION_SIGNATURE_OR_DEFAULT);

    DiagnosticFactory1<KtDeclaration, KotlinType> WRONG_SET_OPERATOR_RETURN_TYPE =
            DiagnosticFactory1.create(Severity.ERROR, DECLARATION_SIGNATURE_OR_DEFAULT);

    // TODO: Change second arg back to DeclarationDescriptor
    DiagnosticFactory1<KtDeclaration, String> WRONG_SET_OPERATOR_RETURN =
            DiagnosticFactory1.create(Severity.WARNING, DECLARATION_SIGNATURE_OR_DEFAULT);

    DiagnosticFactory0<KtExpression> SPECIAL_INHERITANCE_CONSTRUCTOR_MISUSE =
            DiagnosticFactory0.create(Severity.ERROR, DEFAULT);

    DiagnosticFactory0<KtDeclaration> CONST_INITIALIZED_WITH_NON_CONSTANT_VALUE =
            DiagnosticFactory0.create(Severity.ERROR, DEFAULT);

    DiagnosticFactory0<KtExpression> ONLY_CONSTRUCTOR_CALLS_CAN_BE_CONST =
            DiagnosticFactory0.create(Severity.ERROR, DEFAULT);

    DiagnosticFactory0<KtExpression> LONG_REFERENCE =
            DiagnosticFactory0.create(Severity.ERROR, DEFAULT);

    DiagnosticFactory1<KtDeclaration, String> IMPLICIT_LONG_REFERENCE =
            DiagnosticFactory1.create(Severity.ERROR, DEFAULT);

    DiagnosticFactory0<KtExpression> FLOAT_REFERENCE =
            DiagnosticFactory0.create(Severity.ERROR, DEFAULT);

    DiagnosticFactory0<KtExpression> CHAR_REFERENCE =
            DiagnosticFactory0.create(Severity.ERROR, DEFAULT);

    DiagnosticFactory0<PsiElement> UNNECESSARY_REIFIED =
            DiagnosticFactory0.create(Severity.WARNING, DEFAULT);

    DiagnosticFactory0<KtExpression> CONST_LAMBDA_ACCESSING_NON_GLOBAL_VALUE =
            DiagnosticFactory0.create(Severity.ERROR, DEFAULT);

    // Dart emulated errors.
    DiagnosticFactory0<KtExpression> CONST_WITH_NON_CONST =
            DiagnosticFactory0.create(Severity.ERROR, DEFAULT);

    DiagnosticFactory0<KtExpression> NON_CONSTANT_DEFAULT_VALUE_IN_CONST_CONSTRUCTOR =
            DiagnosticFactory0.create(Severity.ERROR, DEFAULT);

    @SuppressWarnings("UnusedDeclaration")
    Object _initializer = new Object() {
        {
            Errors.Initializer.initializeFactoryNamesAndDefaultErrorMessages(ErrorsDart.class, new DefaultErrorMessagesDart());
        }
    };
}
