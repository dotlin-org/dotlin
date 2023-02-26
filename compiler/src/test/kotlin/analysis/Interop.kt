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

package analysis

import BaseTest
import assertCompilesWithError
import org.jetbrains.kotlin.diagnostics.Errors.UNRESOLVED_REFERENCE
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

@DisplayName("Analysis: Interop")
class Interop : BaseTest {
    @Test
    fun `error if using class without const constructor as annotation`() = assertCompilesWithError(UNRESOLVED_REFERENCE) {
        dart(
            """
            class NonAnnotation {}
            """,
            Path("lib/annon.dart")
        )

        kotlin(
            """
            import dev.pub.test.annon.annotations.NonAnnotation

            @NonAnnotation
            class Test
            """
        )
    }

    @Test
    fun `error if using non-const property as annotation`() = assertCompilesWithError(UNRESOLVED_REFERENCE) {
        dart(
            """
            final nonAnnotation = 3;
            """,
            Path("lib/props.dart")
        )

        kotlin(
            """
            import dev.pub.test.props.annotations.nonAnnotation

            @nonAnnotation
            class Test
            """
        )
    }
}