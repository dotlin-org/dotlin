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
@Suppress("PrivatePropertyName")
@DisplayName("Compile: Built-ins: String")
class StringTest : BuiltInsTest("String") {
    private val StringCompanion = "\$StringCompanion"

    private val instance = "\$instance"
    private val StringExt = "\$StringExt"

    @Language("dart")
    override val dart =
        """
        class $StringCompanion {
          $StringCompanion._() : super();
          static final $StringCompanion $instance = $StringCompanion._();
        }
        
        extension $StringExt on String {
          String plus(Object? other) =>
              this + (other == null ? 'null' : other.toString());
          String subSequence(
            int startIndex,
            int endIndex,
          ) =>
              this.substring(startIndex, endIndex);
          int compareTo(String other) => this.length - other.length;
          bool operator <(String other) => this.compareTo(other) < 0;
          bool operator >(String other) => this.compareTo(other) > 0;
          bool operator <=(String other) => this.compareTo(other) <= 0;
          bool operator >=(String other) => this.compareTo(other) >= 0;
        }
        """


    @Test
    fun `calling subSequence method on String`() = assertCompile {
        kotlinWithDeclaration(
            """
            fun main() {
                "abc".subSequence(0, 1)
            }
            """
        )
        dartWithDeclaration(
            """
            void main() {
              'abc'.subSequence(0, 1);
            }
            """
        )
    }

    @Test
    fun `calling plus operator on String`() = assertCompile {
        kotlinWithDeclaration(
            """
            fun main() {
                "abc" + "def"
            }
            """
        )

        dartWithDeclaration(
            """
            void main() {
              'abc' + 'def';
            }
            """
        )
    }

    @Test
    fun `calling plus operator with null on String`() = assertCompile {
        kotlinWithDeclaration(
            """
            fun main() {
                "abc" + null
            }
            """
        )

        dartWithDeclaration(
            """
            void main() {
              'abc'.plus(null);
            }
            """
        )
    }

    @Test
    fun `calling plus operator with object on String`() = assertCompile {
        kotlinWithDeclaration(
            """
            class Test

            fun main() {
                "abc" + Test()
            }
            """
        )

        dartWithDeclaration(
            """
            class Test {
              Test() : super();
            }
            
            void main() {
              'abc'.plus(Test());
            }
            """
        )
    }

    @Test
    fun `calling greater operator on String`() = assertCompile {
        kotlinWithDeclaration(
            """
            fun main() {
                "abc" > "sd"
            }
            """
        )
        dartWithDeclaration(
            """
            void main() {
              'abc' > 'sd';
            }
            """
        )
    }

    @Test
    fun `calling less operator on String`() = assertCompile {
        kotlinWithDeclaration(
            """
            fun main() {
                "abc" < "sd"
            }
            """
        )

        dartWithDeclaration(
            """
            void main() {
              'abc' < 'sd';
            }
            """
        )
    }

    @Test
    fun `calling greater or equal operator on String`() = assertCompile {
        kotlinWithDeclaration(
            """
            fun main() {
                "abc" >= "sd"
            }
            """
        )

        dartWithDeclaration(
            """
            void main() {
              'abc' >= 'sd';
            }
            """
        )
    }

    @Test
    fun `calling less or equal operator on String`() = assertCompile {
        kotlinWithDeclaration(
            """
            fun main() {
                "abc" <= "sd"
            }
            """
        )

        dartWithDeclaration(
            """
            void main() {
              'abc' <= 'sd';
            }
            """
        )
    }
}