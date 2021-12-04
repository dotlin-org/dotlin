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

package compile.builtins

import assertCompile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class Throwable : BuiltInsTest("Throwable") {
    @Language("dart")
    override val dart =
        """ 
        class Throwable {
          Throwable.${'$'}constructor${'$'}0(String? message) : this(message, null);
          Throwable.${'$'}constructor${'$'}1(Throwable? cause) : this(cause?.toString(), cause);
          Throwable.${'$'}constructor${'$'}2() : this(null, null);
          final Throwable? cause;
          final String? message;
          Throwable(
            this.message,
            this.cause,
          );
        }
        """

    @Disabled
    @Test
    fun `function that throws`() = assertCompile {
        kotlinWithDeclaration(
            """
            fun main() {
                throw Throwable()
            }
            """
        )
        dartWithDeclaration(
            """
            void main() {
              throw Throwable();
            }
            """
        )
    }
}