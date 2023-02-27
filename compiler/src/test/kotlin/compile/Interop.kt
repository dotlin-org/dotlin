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
import kotlin.io.path.*

@DisplayName("Compile: Interop")
class Interop : BaseTest {
    @Test
    fun `using class from Dart's typed_data`() = assertCompile {
        kotlin(
            """
            import dart.typed_data.ByteData

            fun main() {
                val buffer = ByteData(8)
            }
            """
        )

        dart(
            """
            import "dart:typed_data" show ByteData;
            import "package:meta/meta.dart";

            void main() {
              final ByteData buffer = ByteData(8);
            }
            """
        )
    }

    @Test
    fun `call Dart function from dependency`() = assertCompile {
        dependency {
            name = "yad"

            dart(
                """
                int yetAnotherFunction() {
                  return 123;
                }
                """,
                Path("lib/test.dart")
            )
        }

        kotlin(
            """
            import dev.pub.yad.test.yetAnotherFunction

            fun main() {
                val result = yetAnotherFunction()
            }
            """
        )

        dart(
            """
            import "package:yad/test.dart" show yetAnotherFunction;
            import "package:meta/meta.dart";

            void main() {
              final int result = yetAnotherFunction();
            }
            """
        )
    }

    @Test
    fun `reference Dart class from dependency`() = assertCompile {
        dependency {
            name = "yad"

            dart(
                """
                class YetAnotherClass {
                  final yetAnotherField = 3;
                }
                """,
                Path("lib/yet_another_class.dart")
            )
        }

        kotlin(
            """
            import dev.pub.yad.yet_another_class.YetAnotherClass

            fun main() {
                lateinit var x: YetAnotherClass
            }
            """
        )

        dart(
            """
            import "package:yad/yet_another_class.dart" show YetAnotherClass;
            import "package:meta/meta.dart";

            void main() {
              late YetAnotherClass x;
            }
            """
        )
    }

    @Test
    fun `import multiple Dart functions with wildcard`() = assertCompile {
        dependency {
            name = "yad"

            dart(
                """
                int yetAnotherFunction1() {
                  return 123;
                }

                int yetAnotherFunction2() {
                  return 456;
                }
                """,
                Path("lib/test.dart")
            )
        }

        kotlin(
            """
            import dev.pub.yad.test.*

            fun main() {
                val result1 = yetAnotherFunction1()
                val result2 = yetAnotherFunction2()
            }
            """
        )

        dart(
            """
            import "package:yad/test.dart" show yetAnotherFunction1, yetAnotherFunction2;
            import "package:meta/meta.dart";

            void main() {
              final int result1 = yetAnotherFunction1();
              final int result2 = yetAnotherFunction2();
            }
            """
        )
    }

    @Test
    fun `call Dart constructor from dependency`() = assertCompile {
        dependency {
            name = "mathz"

            dart(
                """
                class Point {
                  Point(int x, int y);
                }
                """,
                Path("lib/point.dart")
            )
        }

        kotlin(
            """
            import dev.pub.mathz.point.Point

            fun main() {
                val loc = Point(1, 2)
            }
            """
        )

        dart(
            """
            import "package:mathz/point.dart" show Point;
            import "package:meta/meta.dart";

            void main() {
              final Point loc = Point(1, 2);
            }
            """
        )
    }

    @Test
    fun `call Dart const constructor from dependency`() = assertCompile {
        dependency {
            name = "mathz"

            dart(
                """
                class Point {
                  const Point(int x, int y);
                }
                """,
                Path("lib/point.dart")
            )
        }

        kotlin(
            """
            import dev.pub.mathz.point.Point

            fun main() {
                const val loc = Point(1, 2)
            }
            """
        )

        dart(
            """
            import "package:mathz/point.dart" show Point;
            import "package:meta/meta.dart";

            void main() {
              const Point loc = Point(1, 2);
            }
            """
        )
    }

    @Test
    fun `call Dart constructor`() = assertCompile {
        dart(
            """
            class Point {
              Point(int x, int y);
            }
            """,
            Path("lib/point.dart"),
            assert = false,
        )

        kotlin(
            """
            import dev.pub.test.point.Point

            fun main() {
                val loc = Point(1, 2)
            }
            """
        )

        dart(
            """
            import "point.dart" show Point;
            import "package:meta/meta.dart";

            void main() {
              final Point loc = Point(1, 2);
            }
            """
        )
    }

    @Test
    fun `use Dart top-level field`() = assertCompile {
        dart(
            """
            final int myField = 346;
            """,
            Path("lib/fields.dart"),
            assert = false,
        )

        kotlin(
            """
            import dev.pub.test.fields.myField

            fun main() {
                val x = myField * 3
            }
            """
        )

        dart(
            """
            import "fields.dart" show myField;
            import "package:meta/meta.dart";

            void main() {
              final int x = myField * 3;
            }
            """
        )
    }

    @Test
    fun `use Dart const top-level field`() = assertCompile {
        dart(
            """
            const int myField = 346;
            """,
            Path("lib/fields.dart"),
            assert = false,
        )

        kotlin(
            """
            import dev.pub.test.fields.myField

            fun main() {
                const val x = myField * 3
            }
            """
        )

        dart(
            """
            import "fields.dart" show myField;
            import "package:meta/meta.dart";

            void main() {
              const int x = myField * 3;
            }
            """
        )
    }

    @Test
    fun `use Dart top-level getter`() = assertCompile {
        dart(
            """
            int get myGetter => 345634;
            """,
            Path("lib/getters.dart"),
            assert = false,
        )

        kotlin(
            """
            import dev.pub.test.getters.myGetter

            fun main() {
                val x = myGetter * 3
            }
            """
        )

        dart(
            """
            import "getters.dart" show myGetter;
            import "package:meta/meta.dart";

            void main() {
              final int x = myGetter * 3;
            }
            """
        )
    }

    @Test
    fun `use Dart top-level setter`() = assertCompile {
        dart(
            """
            void set mySetter(value) {}
            """,
            Path("lib/setters.dart"),
            assert = false,
        )

        kotlin(
            """
            import dev.pub.test.setters.mySetter

            fun main() {
                mySetter = 3
            }
            """
        )

        dart(
            """
            import "setters.dart" show mySetter;
            import "package:meta/meta.dart";

            void main() {
              mySetter = 3;
            }
            """
        )
    }

    @Test
    fun `use Dart class field`() = assertCompile {
        dart(
            """
            class MyClass {
              final field = 3;
            }
            """,
            Path("lib/my_class.dart"),
            assert = false,
        )

        kotlin(
            """
            import dev.pub.test.my_class.MyClass

            fun main() {
                val x = MyClass().field
            }
            """
        )

        dart(
            """
            import "my_class.dart" show MyClass;
            import "package:meta/meta.dart";

            void main() {
              final int x = MyClass().field;
            }
            """
        )
    }

    @Test
    fun `use Dart class getter`() = assertCompile {
        dart(
            """
            class MyClass {
              int get myGetter => 345634;
            }
            """,
            Path("lib/my_class.dart"),
            assert = false,
        )

        kotlin(
            """
            import dev.pub.test.my_class.MyClass

            fun main() {
                val x = MyClass().myGetter * 3
            }
            """
        )

        dart(
            """
            import "my_class.dart" show MyClass;
            import "package:meta/meta.dart";

            void main() {
              final int x = MyClass().myGetter * 3;
            }
            """
        )
    }

    @Test
    fun `use Dart class setter`() = assertCompile {
        dart(
            """
            class MyClass {
              void set mySetter(value) {}
            }
            """,
            Path("lib/my_class.dart"),
            assert = false,
        )

        kotlin(
            """
            import dev.pub.test.my_class.MyClass

            fun main() {
                MyClass().mySetter = 3
            }
            """
        )

        dart(
            """
            import "my_class.dart" show MyClass;
            import "package:meta/meta.dart";

            void main() {
              MyClass().mySetter = 3;
            }
            """
        )
    }

    @Test
    fun `use Dart class as annotation`() = assertCompile {
        dart(
            """
            class Fragile {
              const Fragile();
            }
            """,
            Path("lib/fragile.dart"),
            assert = false,
        )

        kotlin(
            """
            import dev.pub.test.fragile.annotations.Fragile

            @Fragile
            class Box
            """
        )

        dart(
            """
            import "fragile.dart" show Fragile;
            import "package:meta/meta.dart";

            @Fragile()
            @sealed
            class Box {}
            """
        )
    }

    @Test
    fun `use Dart field as annotation`() = assertCompile {
        dart(
            """
            class Fragile {
              const Fragile._();
            }

            const fragile = Fragile._();
            """,
            Path("lib/fragile.dart"),
            assert = false,
        )

        kotlin(
            """
            import dev.pub.test.fragile.annotations.fragile

            @fragile
            class Box
            """
        )

        dart(
            """
            import "fragile.dart" show fragile;
            import "package:meta/meta.dart";

            @fragile
            @sealed
            class Box {}
            """
        )
    }

    @Test
    fun `use Dart class from export as annotation`() = assertCompile {
        dart(
            """
            class Fragile {
              const Fragile();
            }
            """,
            Path("lib/src/fragile.dart"),
            assert = false,
        )

        dart(
            """
            export "src/fragile.dart";
            """,
            Path("lib/markers.dart"),
            assert = false
        )

        kotlin(
            """
            import dev.pub.test.markers.annotations.Fragile

            @Fragile
            class Box
            """
        )

        dart(
            """
            import "markers.dart" show Fragile;
            import "package:meta/meta.dart";

            @Fragile()
            @sealed
            class Box {}
            """
        )
    }

    @Test
    fun `use Dart field from export as annotation`() = assertCompile {
        dart(
            """
            class Fragile {
              const Fragile._();
            }

            const fragile = Fragile._();
            """,
            Path("lib/src/fragile.dart"),
            assert = false,
        )

        dart(
            """
            export "src/fragile.dart";
            """,
            Path("lib/markers.dart"),
            assert = false
        )

        kotlin(
            """
            import dev.pub.test.markers.annotations.fragile

            @fragile
            class Box
            """
        )

        dart(
            """
            import "markers.dart" show fragile;
            import "package:meta/meta.dart";

            @fragile
            @sealed
            class Box {}
            """
        )
    }

    @Test
    fun `use Dart class from export`() = assertCompile {
        dart(
            """
            class BlackBird {}
            """,
            Path("lib/src/black_bird.dart"),
            assert = false
        )

        dart(
            """
            export "src/black_bird.dart";
            """,
            Path("lib/birds.dart"),
            assert = false
        )

        kotlin(
            """
            import dev.pub.test.birds.BlackBird

            fun main() {
                val myBird = BlackBird()
            }
            """
        )

        dart(
            """
            import "birds.dart" show BlackBird;
            import "package:meta/meta.dart";

            void main() {
              final BlackBird myBird = BlackBird();
            }
            """
        )
    }

    @Test
    fun `use Dart class from export (elaborate)`() = assertCompile {
        dart(
            """
            class BlackBird {}
            """,
            Path("lib/src/black_bird.dart"),
            assert = false
        )

        dart(
            """
            export "src/black_bird.dart";
            """,
            Path("lib/birds.dart"),
            assert = false
        )

        kotlin(
            """
            import dev.pub.test.birds.BlackBird

            fun getBird(): BlackBird = BlackBird()

            val theBird = BlackBird()

            fun main() {
                val x = BlackBird()
                val y = getBird()
                val z = theBird
            }
            """
        )

        dart(
            """
            import "birds.dart" show BlackBird;
            import "package:meta/meta.dart";

            BlackBird getBird() {
              return BlackBird();
            }

            final BlackBird theBird = BlackBird();
            void main() {
              final BlackBird x = BlackBird();
              final BlackBird y = getBird();
              final BlackBird z = theBird;
            }
            """
        )
    }

    @Test
    fun `call Dart function with value parameter with exported Dart type`() = assertCompile {
        dart(
            """
            class BlackBird {}
            """,
            Path("lib/src/black_bird.dart"),
            assert = false
        )

        dart(
            """
            export "src/black_bird.dart";
            """,
            Path("lib/birds.dart"),
            assert = false
        )

        dart(
            """
            import "birds.dart";

            bool isTheWord(BlackBird bird) {
              return true;
            }
            """,
            Path("lib/word.dart"),
            assert = false
        )

        kotlin(
            """
            import dev.pub.test.birds.BlackBird
            import dev.pub.test.word.isTheWord

            fun main() {
                isTheWord(BlackBird())
            }
            """
        )

        dart(
            """
            import "birds.dart" show BlackBird;
            import "word.dart" show isTheWord;
            import "package:meta/meta.dart";

            void main() {
              isTheWord(BlackBird());
            }
            """
        )
    }

    @Test
    fun `use Dart class from package export convention from dependency`() = assertCompile {
        dependency {
            name = "aviation"

            dart(
                """
                class Bird {}
                """,
                Path("lib/src/bird.dart"),
            )

            dart(
                """
                export "src/bird.dart";
                """,
                Path("lib/aviation.dart"),
            )
        }

        kotlin(
            """
            import dev.pub.aviation.Bird

            fun main() {
                val myBird = Bird()
            }
            """
        )

        dart(
            """
            import "package:aviation/aviation.dart" show Bird;
            import "package:meta/meta.dart";

            void main() {
              final Bird myBird = Bird();
            }
            """
        )
    }
}