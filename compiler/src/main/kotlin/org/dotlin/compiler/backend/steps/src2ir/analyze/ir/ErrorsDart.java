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

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.diagnostics.*;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.types.KotlinType;

import static org.jetbrains.kotlin.diagnostics.PositioningStrategies.DECLARATION_SIGNATURE_OR_DEFAULT;

// This MUST be in Java, because the initializer code uses Java reflection.

public interface ErrorsDart {
    DiagnosticFactory2<KtDeclaration, String, DeclarationDescriptor> DART_NAME_CLASH =
            DiagnosticFactory2.create(Severity.ERROR, DECLARATION_SIGNATURE_OR_DEFAULT);

    DiagnosticFactory0<KtDeclaration> EXTENSION_WITHOUT_EXPLICIT_DART_EXTENSION_NAME_IN_PUBLIC_PACKAGE =
            DiagnosticFactory0.create(Severity.WARNING, DECLARATION_SIGNATURE_OR_DEFAULT);

    DiagnosticFactory1<KtDeclaration, KotlinType> WRONG_SET_OPERATOR_RETURN_TYPE =
            DiagnosticFactory1.create(Severity.ERROR, DECLARATION_SIGNATURE_OR_DEFAULT);

    DiagnosticFactory1<KtDeclaration, DeclarationDescriptor> WRONG_SET_OPERATOR_RETURN =
            DiagnosticFactory1.create(Severity.WARNING, DECLARATION_SIGNATURE_OR_DEFAULT);

    @SuppressWarnings("UnusedDeclaration")
    Object _initializer = new Object() {
        {
            Errors.Initializer.initializeFactoryNamesAndDefaultErrorMessages(ErrorsDart.class, new DefaultErrorMessagesDart());
        }
    };
}
