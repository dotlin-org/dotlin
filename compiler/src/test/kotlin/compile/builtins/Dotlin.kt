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

import BaseTest
import assertCompile
import assertCompileFiles
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compile: Built-ins: Dotlin")
class Dotlin : BaseTest {
    @Test
    fun `@DartName`() = assertCompile {
        kotlin(
            """
            @DartName("Dar")
            class Kot

            fun main() {
                Kot()
            }
            """
        )

        dart(
            """
            class Dar {
              Dar() : super();
            }
            
            void main() {
              Dar();
            }
            """
        )
    }

    @Test
    fun `@DartBuiltIn`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            @DartBuiltIn
            class Test

            fun main() {
                Test()
            }
            """
        )

        dart(
            """
            void main() {
              Test();
            }
            """
        )
    }


    @Test
    fun `@DartBuiltInGetter`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            class Hobbit {
                @DartBuiltIn.Getter
                fun isProudfoot(): Boolean = true
            }

            fun main() {
                Hobbit().isProudfoot()
            }
            """
        )

        dart(
            """
            class Hobbit {
              Hobbit() : super();
              bool get isProudfoot {
                return true;
              }
            }

            void main() {
              Hobbit().isProudfoot;
            }
            """
        )
    }

    @Test
    fun `@DartBuiltInGetter override`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            open class Hobbit {
                @DartBuiltIn.Getter
                open fun isProudfoot(): Boolean = false
            }

            class Proudfoot : Hobbit() {
                override fun isProudfoot(): Boolean = true
            }

            fun main() {
                Proudfoot().isProudfoot()
            }
            """
        )

        dart(
            """
            class Hobbit {
              Hobbit() : super();
              bool get isProudfoot {
                return false;
              }
            }

            class Proudfoot extends Hobbit {
              Proudfoot() : super();
              @override
              bool get isProudfoot {
                return true;
              }
            }

            void main() {
              Proudfoot().isProudfoot;
            }
            """
        )
    }

    @Test
    fun `@DartBuiltInImportAlias`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            @DartBuiltIn
            @DartBuiltIn.ImportAlias("dart:core")
            class List

            fun main() {
                List()
            }
            """
        )

        dart(
            """
            import 'dart:core' hide List;
            import 'dart:core' as core;

            void main() {
              core.List();
            }
            """
        )
    }

    @Test
    fun `@DartBuiltInImportAlias separate input files`() = assertCompileFiles {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            package test

            @DartBuiltIn
            @DartBuiltIn.ImportAlias("dart:core")
            class List
            """
        )

        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            package test

            fun main() {
                List()
            }
            """
        )

        dart(
            """
            import 'dart:core' hide List;
            import 'dart:core' as core;

            void main() {
              core.List();
            }
            """
        )
    }

    @Test
    fun `@DartBuiltInImportAlias type reference only`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            @DartBuiltIn
            @DartBuiltIn.ImportAlias("dart:core")
            class List

            fun test(list: List) {}
            """
        )

        dart(
            """
            import 'dart:core' hide List;
            import 'dart:core' as core;

            void test(core.List list) {}
            """
        )
    }

    @Test
    fun `@DartBuiltInHideImport`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            @DartBuiltIn.HideImport("dart:core")
            class Enum

            fun main() {
                Enum()
            }
            """
        )

        dart(
            """
            import 'dart:core' hide Enum;

            class Enum {
              Enum() : super();
            }

            void main() {
              Enum();
            }
            """
        )
    }

    @Test
    fun `@DartBuiltInHideImport twice`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            @DartBuiltIn.HideImport("dart:core")
            class Enum

            @DartBuiltIn.HideImport("dart:core")
            class List

            fun main() {
                Enum()
            }
            """
        )

        dart(
            """
            import 'dart:core' hide Enum, List;

            class Enum {
              Enum() : super();
            }

            class List {
              List() : super();
            }

            void main() {
              Enum();
            }
            """
        )
    }

    @Test
    fun `@DartBuiltInHideImport twice, multiple files`() = assertCompileFiles {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            package test

            @DartBuiltIn.HideImport("dart:core")
            class Something

            @DartBuiltIn.HideImport("dart:core")
            class SomethingElse
            """
        )

        kotlin(
            """
            package test

            fun main() {
                Something()
            }
            """
        )

        dart(
            """
            import 'dart:core' hide Something, SomethingElse;

            class Something {
              Something() : super();
            }

            class SomethingElse {
              SomethingElse() : super();
            }

            void main() {
              Something();
            }
            """
        )
    }
}