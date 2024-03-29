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

@DisplayName("Compile: Extension")
class Extension : BaseTest {
    @Test
    fun extension() = assertCompile {
        kotlin(
            """
            class Test

            fun Test.doIt() {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;

            @sealed
            class Test {}

            extension ${'$'}Extensions${'$'}e82faa8ee9f5630 on Test {
              void doIt() {}
            }
            """
        )
    }

    @Test
    fun `extension getter`() = assertCompile {
        kotlin(
            """
            class Test

            val Test.number: Int get() = 3
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;

            @sealed
            class Test {}

            extension ${'$'}Extensions${'$'}e82faa8ee9f5630 on Test {
              int get number {
                return 3;
              }
            }
            """
        )
    }

    @Test
    fun `extension getter and setter`() = assertCompile {
        kotlin(
            """
            class Test

            var Test.number: Int
                get() = 3
                set(value: Int) {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;

            @sealed
            class Test {}

            extension ${'$'}Extensions${'$'}e82faa8ee9f5630 on Test {
              int get number {
                return 3;
              }

              void set number(int value) {}
            }
            """
        )
    }

    @Test
    fun `extension on type with type parameter`() = assertCompile {
        kotlin(
            """
            class Test<T>

            fun <T> Test<T>.doIt() {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;

            @sealed
            class Test<T> {}

            extension ${'$'}Extensions${'$'}m16c83f0fbfe989b5<T> on Test<T> {
              void doIt() {}
            }
            """
        )
    }

    @Test
    fun `two extensions on type with type parameter`() = assertCompile {
        kotlin(
            """
            class Test<T>

            fun <T> Test<T>.doIt() {}

            fun <T> Test<T>.doItAgain() {}
            """
        )

        // TODO?: These could be in a single container, but technically they are different types.
        dart(
            """
            import "package:meta/meta.dart" show sealed;

            @sealed
            class Test<T> {}

            extension ${'$'}Extensions${'$'}m16c83f0fbfe989b5<T> on Test<T> {
              void doIt() {}
            }

            extension ${'$'}Extensions${'$'}37f52dfc25a24d30<T> on Test<T> {
              void doItAgain() {}
            }
            """
        )
    }

    @Test
    fun `two extensions on type with type parameter with different type arguments`() = assertCompile {
        kotlin(
            """
            class Test<T>

            fun <T> Test<T>.doIt() {}

            fun Test<Int>.doItAgain() {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;

            @sealed
            class Test<T> {}

            extension ${'$'}Extensions${'$'}m16c83f0fbfe989b5<T> on Test<T> {
              void doIt() {}
            }

            extension ${'$'}Extensions${'$'}m2cf093a2f7cb6dc9 on Test<int> {
              void doItAgain() {}
            }
            """
        )
    }

    @Test
    fun `extension with type parameter on type with type parameter`() = assertCompile {
        kotlin(
            """
            class Test<T>

            fun <A, B> Test<A>.doIt() {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;

            @sealed
            class Test<T> {}

            extension ${'$'}Extensions${'$'}1969ba987fc0610b<A> on Test<A> {
              void doIt<B>() {}
            }
            """
        )
    }

    @Test
    fun `extension on type from other file`() = assertCompile {
        kotlin(
            """
            fun String.titleCase() {}
            """
        )

        dart(
            """
            extension ${'$'}Extensions${'$'}m4003f16a123cbc3c on String {
              void titleCase() {}
            }
            """
        )
    }

    @Test
    fun `extension with generic receiver type`() = assertCompile {
        kotlin(
            """
            fun <T> T.doIt() {}
            """
        )

        dart(
            """
            extension ${'$'}Extensions${'$'}1ebfd8e5fb05c28e<T> on T {
              void doIt() {}
            }
            """
        )
    }

    @Test
    fun `extension with generic receiver type with explicit bound`() = assertCompile {
        kotlin(
            """
            class Test<T>

            fun <T : Test<T>> T.doIt() {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;

            @sealed
            class Test<T> {}

            extension ${'$'}Extensions${'$'}m7d7bc927be75976e<T extends Test<T>> on T {
              void doIt() {}
            }
            """
        )
    }

    @Test
    fun `extension with generic receiver type with explicit specified bound`() = assertCompile {
        kotlin(
            """
            class Test<T>

            fun <T : Test<String>> T.doIt() {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;

            @sealed
            class Test<T> {}

            extension ${'$'}Extensions${'$'}m39f172ecf6739208<T extends Test<String>> on T {
              void doIt() {}
            }
            """
        )
    }

    @Test
    fun `extension with multiple type parameter bounds`() = assertCompile {
        kotlin(
            """
            interface Buildable {
                fun build()
            }

            interface Identifiable {
                fun identify()
            }

            private fun identifyAndExec(id: Identifiable) {}

            fun <T> T.buildAndIdentify() where T : Buildable, T : Identifiable {
                identify()
                build()
                identifyAndExec(this)
            }

            class SomeItem : Buildable, Identifiable {
                override fun build() {}
                override fun identify() {}
            }

            fun main() {
                SomeItem().buildAndIdentify()
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;

            abstract class Buildable {
              void build();
            }

            abstract class Identifiable {
              void identify();
            }

            void _identifyAndExec(Identifiable id) {}

            @sealed
            class SomeItem implements Buildable, Identifiable {
              @override
              void build() {}
              @override
              void identify() {}
            }

            void main() {
              SomeItem().buildAndIdentify();
            }

            extension ${'$'}Extensions${'$'}5ed3d873052fe8b3<T extends Object> on T {
              void buildAndIdentify() {
                (this as Identifiable).identify();
                (this as Buildable).build();
                _identifyAndExec(this as Identifiable);
              }
            }
            """
        )
    }

    @Test
    fun `extension on Kotlin number primitive`() = assertCompile {
        kotlin(
            """
            fun Int.shift(): Int = 0
            """
        )

        dart(
            """
            extension ${'$'}Extensions${'$'}69eeea92bbe1f1f1 on int {
              int shift() {
                return 0;
              }
            }
            """
        )
    }

    @Test
    fun `calling extension from other package on Kotlin number primitive`() = assertCompile {
        kotlin(
            """
            import kotlin.math.absoluteValue

            fun main() {
                0.absoluteValue
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/math.dt.g.dart"
                show ${'$'}Extensions${'$'}69eeea92bbe1f1f1;

            void main() {
              0.absoluteValue;
            }
            """
        )
    }

    @Test
    fun `extension function with always nullable generic return type which is also the receiver type`() = assertCompile {
        kotlin(
            """
            fun <T> T.execute(): T? = null
            """
        )

        dart(
            """
            extension ${'$'}Extensions${'$'}23387b3b44ac6b48<T> on T {
              T? execute() {
                return null;
              }
            }
            """
        )
    }

    @Test
    fun `extension calling extension with receiver type parameter`() = assertCompile {
        kotlin(
            """
            class Test<T>

            fun <T> Test<T>.doIt() {}
            fun <T> Test<T>.doItAgain() = doIt()
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;

            @sealed
            class Test<T> {}

            extension ${'$'}Extensions${'$'}m16c83f0fbfe989b5<T> on Test<T> {
              void doIt() {}
            }

            extension ${'$'}Extensions${'$'}37f52dfc25a24d30<T> on Test<T> {
              void doItAgain() {
                return this.doIt();
              }
            }
            """
        )
    }

    @Test
    fun `extension calling extension with receiver and regular type parameter`() = assertCompile {
        kotlin(
            """
            class Test<T>

            fun <T, A> Test<T>.doIt() {}
            fun <T, A> Test<T>.doItAgain() = doIt<T, A>()
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;

            @sealed
            class Test<T> {}

            extension ${'$'}Extensions${'$'}1969ba987fc0610b<T> on Test<T> {
              void doIt<A>() {}
            }

            extension ${'$'}Extensions${'$'}186b8b16690e3d60<T> on Test<T> {
              void doItAgain<A>() {
                return this.doIt<A>();
              }
            }
            """
        )
    }

    @Test
    fun `calling extension with receiver and regular type parameter`() = assertCompile {
        kotlin(
            """
            class Test<T>

            fun <T, A> Test<T>.doIt() {}

            fun main() {
                Test<Int>().doIt<Int, Double>()
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;

            @sealed
            class Test<T> {}

            void main() {
              Test<int>().doIt<double>();
            }

            extension ${'$'}Extensions${'$'}1969ba987fc0610b<T> on Test<T> {
              void doIt<A>() {}
            }
            """
        )
    }

    @Test
    fun `extension on companion`() = assertCompile {
        kotlin(
            """
            class Test {
                companion object
            }

            fun Test.Companion.doSomething(): Int = 3

            fun main() {
                Test.doSomething()
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;

            @sealed
            class Test {
              static const Test${'$'}Companion ${'$'}companion = Test${'$'}Companion.${'$'}instance;
            }

            void main() {
              Test${'$'}Companion.${'$'}instance.doSomething();
            }

            extension ${'$'}Extensions${'$'}5e33dc1589283692 on Test${'$'}Companion {
              int doSomething() {
                return 3;
              }
            }

            @sealed
            class Test${'$'}Companion {
              const Test${'$'}Companion._() : super();
              static const Test${'$'}Companion ${'$'}instance = const Test${'$'}Companion._();
            }
            """
        )
    }
}