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

package compile.dialect.const

import BaseTest
import assertCompile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compile: Dialect: Const Inline")
class ConstInline : BaseTest {
    @Test
    fun `const inline function`() = assertCompile {
        kotlin(
            """
            class Test const constructor()

            const inline fun test() = Test()
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            @sealed
            class Test {
              const Test() : super();
            }

            @pragma("vm:always-consider-inlining")
            Test test() {
              return Test();
            }
            """
        )
    }

    @Test
    fun `const inline function call`() = assertCompile {
        kotlin(
            """
            class Test const constructor()

            const inline fun createTest() = Test()

            fun main() {
                const val x = createTest()
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            @sealed
            class Test {
              const Test() : super();
            }

            @pragma("vm:always-consider-inlining")
            Test createTest() {
              return Test();
            }

            void main() {
              const Test x = Test();
            }
            """
        )
    }

    @Test
    fun `const inline function call as non-const`() = assertCompile {
        kotlin(
            """
            class Test const constructor()

            const inline fun createTest() = Test()

            fun main() {
                val x = createTest()
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            @sealed
            class Test {
              const Test() : super();
            }

            @pragma("vm:always-consider-inlining")
            Test createTest() {
              return Test();
            }

            void main() {
              final Test x = createTest();
            }
            """
        )
    }

    @Test
    fun `const inline function with const variables call`() = assertCompile {
        kotlin(
            """
            class Test const constructor(message: String)

            const inline fun createTest(): Test {
                const val firstPart = "first, "
                const val secondPart = "second"

                return Test(firstPart + secondPart)
            }

            fun main() {
                const val y = createTest()
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/library.dt.g.dart" show SafeStringPlus;
            import "package:meta/meta.dart";

            @sealed
            class Test {
              const Test(String message) : super();
            }

            @pragma("vm:always-consider-inlining")
            Test createTest() {
              const String firstPart = "first, ";
              const String secondPart = "second";
              return Test(firstPart + secondPart);
            }

            void main() {
              const Test y = Test("first, " + "second");
            }
            """
        )
    }

    @Test
    fun `const inline function with const variables and parameters call`() = assertCompile {
        kotlin(
            """
            class Test const constructor(message: String)

            const inline fun createTest(thirdPart: String): Test {
                const val firstPart = "first, "
                const val secondPart = "second, "

                return Test(firstPart + secondPart + thirdPart)
            }

            fun main() {
                const val y = createTest("third")
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/library.dt.g.dart" show SafeStringPlus;
            import "package:meta/meta.dart";

            @sealed
            class Test {
              const Test(String message) : super();
            }

            @pragma("vm:always-consider-inlining")
            Test createTest(String thirdPart) {
              const String firstPart = "first, ";
              const String secondPart = "second, ";
              return Test(firstPart + secondPart + thirdPart);
            }

            void main() {
              const Test y = Test("first, " + "second, " + "third");
            }
            """
        )
    }

    @Test
    fun `const inline function with const variables and parameters non-const call`() = assertCompile {
        kotlin(
            """
            class Test const constructor(message: String)

            const inline fun createTest(thirdPart: String): Test {
                const val firstPart = "first, "
                const val secondPart = "second, "

                return Test(firstPart + secondPart + thirdPart)
            }

            fun main() {
                val y = createTest("third")
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/library.dt.g.dart" show SafeStringPlus;
            import "package:meta/meta.dart";

            @sealed
            class Test {
              const Test(String message) : super();
            }

            @pragma("vm:always-consider-inlining")
            Test createTest(String thirdPart) {
              const String firstPart = "first, ";
              const String secondPart = "second, ";
              return Test(firstPart + secondPart + thirdPart);
            }

            void main() {
              final Test y = createTest("third");
            }
            """
        )
    }

    @Test
    fun `const inline function with const variables and parameters used in const variables call`() = assertCompile {
        kotlin(
            """
            class Test const constructor(message: String)

            const inline fun createTest(thirdPart: String): Test {
                const val firstPart = "first, "
                const val secondAndThirdPart = "second, ${"$"}thirdPart"

                return Test(firstPart + secondAndThirdPart)
            }

            fun main() {
                const val y = createTest("third")
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/library.dt.g.dart" show SafeStringPlus;
            import "package:meta/meta.dart";

            @sealed
            class Test {
              const Test(String message) : super();
            }

            @pragma("vm:always-consider-inlining")
            Test createTest(String thirdPart) {
              const String firstPart = "first, ";
              final String secondAndThirdPart = "second, ${"$"}{thirdPart}";
              return Test(firstPart + secondAndThirdPart);
            }

            void main() {
              const Test y = Test("first, " + "second, third");
            }
            """
        )
    }

    @Test
    fun `lambda literal passed to const inline function referencing object val`() = assertCompile {
        kotlin(
            """
            class Zen const constructor(private val maintainMotorcycle: () -> String)

            const inline fun createZenWith(maintain: () -> String) = Zen(maintain)

            object Good {
                val QUALITY = "Quality"
            }

            fun main() {
                const val zen = createZenWith { Good.QUALITY }
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            @sealed
            class Zen {
              const Zen(this._maintainMotorcycle) : super();
              @nonVirtual
              final String Function() _maintainMotorcycle;
            }

            @pragma("vm:always-consider-inlining")
            Zen createZenWith(String Function() maintain) {
              return Zen(maintain);
            }

            @sealed
            class Good {
              Good._() : super();
              @nonVirtual
              final String ${"$"}QUALITY = "Quality";
              static final Good ${"$"}instance = Good._();
              static final String QUALITY = Good.${"$"}instance.${"$"}QUALITY;
            }

            void main() {
              const Zen zen = Zen(_${"$"}21b1);
            }

            String _${"$"}21b1() {
              return Good.${"$"}instance.${"$"}QUALITY;
            }
            """
        )
    }
}