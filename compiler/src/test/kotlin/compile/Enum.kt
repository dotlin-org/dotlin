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

package compile

import BaseTest
import assertCompile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compile: Enum")
class Enum : BaseTest {
    @Test
    fun `enum`() = assertCompile {
        kotlin(
            """
            enum class Test {
                alpha,
                beta,
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/function.dt.g.dart" show Function1;
            import "package:meta/meta.dart";

            enum Test {
              alpha._(),
              beta._();
            
              const Test._();
            }

            Test ${'$'}Test${'$'}valueOf(String value) =>
                Test.values.firstWhere((Test v) => v.name == value);
            """
        )
    }

    @Test
    fun `enum with one extra value`() = assertCompile {
        kotlin(
            """
            enum class Test(val lowercase: String) {
                alpha("α"),
                beta("β"),
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/function.dt.g.dart" show Function1;
            import "package:meta/meta.dart";

            enum Test {
              alpha._("α"),
              beta._("β");

              const Test._(this.lowercase);
              @nonVirtual
              final String lowercase;
            }

            Test ${'$'}Test${'$'}valueOf(String value) =>
                Test.values.firstWhere((Test v) => v.name == value);
            """
        )
    }

    @Test
    fun `enum with two extra values`() = assertCompile {
        kotlin(
            """
            enum class Test(val lowercase: String, val uppercase: String) {
                alpha("α", "Α"),
                beta("β", "Β"),
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/function.dt.g.dart" show Function1;
            import "package:meta/meta.dart";

            enum Test {
              alpha._("α", "Α"),
              beta._("β", "Β");
            
              const Test._(
                this.lowercase,
                this.uppercase,
              );
              @nonVirtual
              final String lowercase;
              @nonVirtual
              final String uppercase;
            }

            Test ${'$'}Test${'$'}valueOf(String value) =>
                Test.values.firstWhere((Test v) => v.name == value);
            """
        )
    }

    @Test
    fun `get enum value`() = assertCompile {
        kotlin(
            """
            enum class Test {
                alpha,
                beta,
            }

            fun main() {
                Test.alpha
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/function.dt.g.dart" show Function1;
            import "package:meta/meta.dart";

            enum Test {
              alpha._(),
              beta._();
            
              const Test._();
            }

            void main() {
              Test.alpha;
            }

            Test ${'$'}Test${'$'}valueOf(String value) =>
                Test.values.firstWhere((Test v) => v.name == value);
            """
        )
    }

    @Test
    fun `call Enum values()`() = assertCompile {
        kotlin(
            """
            enum class Test {
                alpha,
                beta,
            }

            fun main() {
                Test.values()
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/function.dt.g.dart" show Function1;
            import "package:meta/meta.dart";

            enum Test {
              alpha._(),
              beta._();
            
              const Test._();
            }

            void main() {
              Test.values;
            }

            Test ${'$'}Test${'$'}valueOf(String value) =>
                Test.values.firstWhere((Test v) => v.name == value);
            """
        )
    }

    @Test
    fun `call Enum valueOf()`() = assertCompile {
        kotlin(
            """
            enum class Test {
                alpha,
                beta,
            }

            fun main() {
                Test.valueOf("alpha")
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/function.dt.g.dart" show Function1;
            import "package:meta/meta.dart";

            enum Test {
              alpha._(),
              beta._();
            
              const Test._();
            }

            void main() {
              ${'$'}Test${'$'}valueOf("alpha");
            }

            Test ${'$'}Test${'$'}valueOf(String value) =>
                Test.values.firstWhere((Test v) => v.name == value);
            """
        )
    }

    @Test
    fun enumValues() = assertCompile {
        kotlin(
            """
            enum class Test {
                alpha,
                beta,
            }

            fun main() {
                const val values = enumValues<Test>()
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/function.dt.g.dart" show Function1;
            import "package:meta/meta.dart";

            enum Test {
              alpha._(),
              beta._();
            
              const Test._();
            }

            void main() {
              const List<Test> values = Test.values;
            }

            Test ${'$'}Test${'$'}valueOf(String value) =>
                Test.values.firstWhere((Test v) => v.name == value);
            """
        )
    }

    @Test
    fun enumValueOf() = assertCompile {
        kotlin(
            """
            enum class Test {
                alpha,
                beta,
            }

            fun main() {
                enumValueOf<Test>("alpha")
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/function.dt.g.dart" show Function1;
            import "package:meta/meta.dart";

            enum Test {
              alpha._(),
              beta._();
            
              const Test._();
            }

            void main() {
              ${'$'}Test${'$'}valueOf("alpha");
            }

            Test ${'$'}Test${'$'}valueOf(String value) =>
                Test.values.firstWhere((Test v) => v.name == value);
            """
        )
    }

    @Test
    fun `enum with method`() = assertCompile {
        kotlin(
            """
            enum class Test(val lowercase: String, val uppercase: String) {
                alpha("α", "Α"),
                beta("β", "Β");

                fun toUppercase() = uppercase
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/function.dt.g.dart" show Function1;
            import "package:meta/meta.dart";

            enum Test {
              alpha._("α", "Α"),
              beta._("β", "Β");

              const Test._(
                this.lowercase,
                this.uppercase,
              );
              @nonVirtual
              final String lowercase;
              @nonVirtual
              final String uppercase;
              @nonVirtual
              String toUppercase() {
                return this.uppercase;
              }
            }

            Test ${'$'}Test${'$'}valueOf(String value) =>
                Test.values.firstWhere((Test v) => v.name == value);
            """
        )
    }

    @Test
    fun `enum with anonymous class`() = assertCompile {
        kotlin(
            """
            class Person

            enum class Temperature(val average: Int) {
                cool(10) {
                    override val min = 3
                    override val max = 15
            
                    override fun isTooCoolFor(person: Person) = true
                },

                lukewarm(30) {
                    override val min = 15
                    override val max = 19
            
                    override fun isTooCoolFor(person: Person) = false
            
                    fun lukewarmOnlyMethod(): Int = 234
                },

                warm(40) {
                    override val min = 20
                    override val max = 25
            
                    override fun isTooCoolFor(person: Person) = false
                    override fun isTooHotFor(person: Person) = false
                },

                hot {
                    override val min = 25
                    override val max = 35
            
                    override fun isTooCoolFor(person: Person) = false
                    override fun isTooHotFor(person: Person) = true
                };

                constructor() : this(100)

                abstract val min: Int
                abstract val max: Int

                abstract fun isTooCoolFor(person: Person): Boolean

                open fun isTooHotFor(person: Person) = false
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/function.dt.g.dart" show Function1;
            import "package:meta/meta.dart";

            @sealed
            class Person {}

            enum Temperature {
              cool._(10, const _${'$'}Temperature${'$'}cool()),
              lukewarm._(30, const _${'$'}Temperature${'$'}lukewarm()),
              warm._(40, const _${'$'}Temperature${'$'}warm()),
              hot._${'$'}constructor${'$'}1(const _${'$'}Temperature${'$'}hot());

              const Temperature._(
                this.average,
                this._${'$'}delegate,
              );
              @nonVirtual
              final int average;
              const Temperature._${'$'}constructor${'$'}1(_${'$'}Temperature ${'$'}delegate)
                  : this._(100, ${'$'}delegate);
              int get min => this._${'$'}delegate.min;
              int get max => this._${'$'}delegate.max;
              @nonVirtual
              bool isTooCoolFor(Person person) => this._${'$'}delegate.isTooCoolFor(person);
              @nonVirtual
              bool isTooHotFor(Person person) => this._${'$'}delegate.isTooHotFor(person);
              @nonVirtual
              final _${'$'}Temperature _${'$'}delegate;
            }

            Temperature ${'$'}Temperature${'$'}valueOf(String value) =>
                Temperature.values.firstWhere((Temperature v) => v.name == value);

            abstract class _${'$'}Temperature {
              abstract final int min;
              abstract final int max;
              bool isTooCoolFor(Person person);
              bool isTooHotFor(Person person) {
                return false;
              }

              const _${'$'}Temperature();
            }

            @sealed
            class _${'$'}Temperature${'$'}cool extends _${'$'}Temperature {
              const _${'$'}Temperature${'$'}cool();
              @override
              final int min = 3;
              @override
              final int max = 15;
              @override
              bool isTooCoolFor(Person person) {
                return true;
              }
            }

            @sealed
            class _${'$'}Temperature${'$'}lukewarm extends _${'$'}Temperature {
              const _${'$'}Temperature${'$'}lukewarm();
              @override
              final int min = 15;
              @override
              final int max = 19;
              @override
              bool isTooCoolFor(Person person) {
                return false;
              }

              @nonVirtual
              int lukewarmOnlyMethod() {
                return 234;
              }
            }

            @sealed
            class _${'$'}Temperature${'$'}warm extends _${'$'}Temperature {
              const _${'$'}Temperature${'$'}warm();
              @override
              final int min = 20;
              @override
              final int max = 25;
              @override
              bool isTooCoolFor(Person person) {
                return false;
              }

              @override
              bool isTooHotFor(Person person) {
                return false;
              }
            }

            @sealed
            class _${'$'}Temperature${'$'}hot extends _${'$'}Temperature {
              const _${'$'}Temperature${'$'}hot();
              @override
              final int min = 25;
              @override
              final int max = 35;
              @override
              bool isTooCoolFor(Person person) {
                return false;
              }

              @override
              bool isTooHotFor(Person person) {
                return true;
              }
            }

            extension ${'$'}Extensions${'$'}610f18433f4785ee on Temperature {
              int lukewarmOnlyMethod() =>
                  (this._${'$'}delegate as _${'$'}Temperature${'$'}lukewarm).lukewarmOnlyMethod();
            }
            """
        )
    }

    @Test
    fun `enum with overridden members`() = assertCompile {
        kotlin(
            """
            enum class Test(val x: Int) {
                alpha(30) {
                     override val y: Int = 30  
                },
                beta(50) {
                    override val y: Int = 50
                };

                abstract val y: Int
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/function.dt.g.dart" show Function1;
            import "package:meta/meta.dart";

            enum Test {
              alpha._(30, const _${'$'}Test${'$'}alpha()),
              beta._(50, const _${'$'}Test${'$'}beta());

              const Test._(
                this.x,
                this._${'$'}delegate,
              );
              @nonVirtual
              final int x;
              int get y => this._${'$'}delegate.y;
              @nonVirtual
              final _${'$'}Test _${'$'}delegate;
            }

            Test ${'$'}Test${'$'}valueOf(String value) =>
                Test.values.firstWhere((Test v) => v.name == value);

            abstract class _${'$'}Test {
              abstract final int y;
              const _${'$'}Test();
            }

            @sealed
            class _${'$'}Test${'$'}alpha extends _${'$'}Test {
              const _${'$'}Test${'$'}alpha();
              @override
              final int y = 30;
            }

            @sealed
            class _${'$'}Test${'$'}beta extends _${'$'}Test {
              const _${'$'}Test${'$'}beta();
              @override
              final int y = 50;
            }
            """
        )
    }
}