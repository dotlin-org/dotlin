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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@Disabled
@DisplayName("Compile: Built-ins: Nothing")
class Nothing : BuiltInsTest("Nothing") {
    @Language("dart")
    override val dart =
        """ 
        class Nothing {
          Nothing._() : super();
        }
        """


    @Test
    fun `function which returns Nothing`() = assertCompile {
        kotlinWithDeclaration(
            """
            fun test(): Nothing {
                throw Exception()
            }
            """
        )
        dartWithDeclaration(
            """
            Nothing test() {
              throw Exception();
            }
            """
        )
    }
}