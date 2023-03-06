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

package compile.dialect.collections

import BaseTest
import assertCompile
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

@DisplayName("Compile: Dialect: Collections: Collection")
class Collection : BaseTest {
    @Test
    fun `assign Dart List to Kotlin Collection variable`() = assertCompile {
        dart(
            """
            List<int> calculate() => [];
            """,
            Path("lib/calc.dart"),
            assert = false,
        )

        kotlin(
            """
            import pkg.test.calc.calculate

            fun main() {
                val myCollection: Collection<Int> = calculate()
            }
            """
        )

        dart(
            """
            import "calc.dart" show calculate;
            import "package:dotlin/src/dotlin/intrinsics/collection_type_bounds.dt.g.dart"
                show AnyCollection;

            void main() {
              final AnyCollection<int> myCollection = calculate();
            }
            """
        )
    }

    @Test
    fun `assign Dart List to Kotlin Collection property`() = assertCompile {
        dart(
            """
            List<int> calculate() => [];
            """,
            Path("lib/calc.dart"),
            assert = false,
        )

        kotlin(
            """
            import pkg.test.calc.calculate

            val myCollection: Collection<Int> = calculate()
            """
        )

        dart(
            """
            import "calc.dart" show calculate;
            import "package:dotlin/src/dotlin/intrinsics/collection_type_bounds.dt.g.dart"
                show AnyCollection;

            final AnyCollection<int> myCollection = calculate();
            """
        )
    }

    @Test
    fun `pass Dart List to Kotlin Collection parameter`() = assertCompile {
        dart(
            """
            List<int> calculate() => [];
            """,
            Path("lib/calc.dart"),
            assert = false,
        )

        // TODO: Handle case if `AnyCollection` is used in Kotlin code.
        kotlin(
            """
            import pkg.test.calc.calculate

            fun main() {
                val myList = calculate()
                process(myList)
            }

            fun process(collection: Collection<Int>) {}
            """
        )

        dart(
            """
            import "calc.dart" show calculate;
            import "package:dotlin/src/dotlin/intrinsics/collection_type_bounds.dt.g.dart"
                show AnyCollection;

            void main() {
              final List<int> myList = calculate();
              process(myList);
            }
            
            void process(AnyCollection<int> collection) {}
            """
        )
    }

    @Test
    fun `(dynamic) is Collection`() = assertCompile {
        dart(
            """
            dynamic calculate() => null;
            """,
            Path("lib/calc.dart"),
            assert = false,
        )

        kotlin(
            """
            import pkg.test.calc.calculate

            fun main() {
                val obj = calculate()
                if (obj is Collection<Int>) {
                    obj.cast<Number>()
                }
            }
            """
        )

        dart(
            """
            import "calc.dart" show calculate;
            import "package:dotlin/src/dotlin/intrinsics/collection_type_checks.dt.g.dart"
                show isCollection;
            import "package:dotlin/src/kotlin/collections/collection.dt.g.dart"
                show Collection;
            import "package:dotlin/src/dotlin/intrinsics/collection_type_bounds.dt.g.dart"
                show AnyCollection;

            void main() {
              final dynamic obj = calculate();
              if (isCollection<int>(obj)) {
                (obj as AnyCollection<int>).cast<num>();
              }
            }
            """
        )
    }

    @Test
    fun `(Any) is Collection`() = assertCompile {
        dart(
            """
            Object calculate() => 0;
            """,
            Path("lib/calc.dart"),
            assert = false,
        )

        kotlin(
            """
            import pkg.test.calc.calculate

            fun main() {
                val obj = calculate()
                if (obj is Collection<Int>) {
                    obj.cast<Number>()
                }
            }
            """
        )

        dart(
            """
            import "calc.dart" show calculate;
            import "package:dotlin/src/dotlin/intrinsics/collection_type_checks.dt.g.dart"
                show isCollection;
            import "package:dotlin/src/kotlin/collections/collection.dt.g.dart"
                show Collection;
            import "package:dotlin/src/dotlin/intrinsics/collection_type_bounds.dt.g.dart"
                show AnyCollection;

            void main() {
              final Object obj = calculate();
              if (isCollection<int>(obj)) {
                (obj as AnyCollection<int>).cast<num>();
              }
            }
            """
        )
    }

    @Test
    fun `(dynamic) !is Collection`() = assertCompile {
        dart(
            """
            dynamic calculate() => null;
            """,
            Path("lib/calc.dart"),
            assert = false,
        )

        kotlin(
            """
            import pkg.test.calc.calculate

            fun main() {
                val obj = calculate()
                if (obj !is Collection<Int>) {
                    
                }
            }
            """
        )

        dart(
            """
            import "calc.dart" show calculate;
            import "package:dotlin/src/dotlin/intrinsics/collection_type_checks.dt.g.dart"
                show isCollection;

            void main() {
              final dynamic obj = calculate();
              if (!isCollection<int>(obj)) {}
            }
            """
        )
    }

    @Test
    fun `(Any) !is Collection`() = assertCompile {
        dart(
            """
            Object calculate() => 3;
            """,
            Path("lib/calc.dart"),
            assert = false,
        )

        kotlin(
            """
            import pkg.test.calc.calculate

            fun main() {
                val obj = calculate()
                if (obj !is Collection<Int>) {

                }
            }
            """
        )

        dart(
            """
            import "calc.dart" show calculate;
            import "package:dotlin/src/dotlin/intrinsics/collection_type_checks.dt.g.dart"
                show isCollection;

            void main() {
              final Object obj = calculate();
              if (!isCollection<int>(obj)) {}
            }
            """
        )
    }

    @Test
    fun `assign Dart List subtype to Kotlin Collection`() = assertCompile {
        dart(
            """
            class MyList<E> implements List<E> {
              dynamic noSuchMethod(Invocation invocation) {}
            }
            """,
            Path("lib/my_list.dart"),
            assert = false
        )

        kotlin(
            """
            import pkg.test.my_list.MyList

            fun main() {
                val myList: Collection<Int> = MyList<Int>()
            }
            """
        )

        dart(
            """
            import "my_list.dart" show MyList;
            import "package:dotlin/src/dotlin/intrinsics/collection_type_bounds.dt.g.dart"
                show AnyCollection;

            void main() {
              final AnyCollection<int> myList = MyList<int>();
            }
            """
        )
    }

    // TODO: Extension on Collection
    @Disabled
    @Test
    fun `extension on MutableCollection`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                lateinit var bag: MutableCollection<Int>
                bag.calc()

                val list = mutableListOf(1, 2, 3)
                list.calc()
            }

            fun <E> MutableCollection<E>.calc() {}
            """
        )

        dart(
            """
            void main() {
              final dynamic obj = calculate();
              if (!obj.isCollection<int>()) {}
            }
            """
        )
    }
}