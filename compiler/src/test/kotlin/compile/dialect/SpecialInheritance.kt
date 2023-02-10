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

package compile.dialect

import BaseTest
import assertCompile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

@DisplayName("Compile: Dialect: Special Inheritance")
class SpecialInheritance : BaseTest {
    @Test
    fun `interface can implement Kotlin external implicit interface`() = assertCompile {
        kotlin(
            """
            @DartLibrary("pigeon.dart")
            open external class Pigeon {
                constructor(implement: InterfaceOrMixin)
                open fun fly(): Unit = definedExternally
            }

            interface CarrierPigeon : Pigeon(Interface)
            """
        )

        dart(
            """
            class Pigeon {
              void fly() {}
            }
            """,
            Path("lib/pigeon.dart"),
            assert = false
        )

        dart(
            """
            import "pigeon.dart" show Pigeon;
            import "package:meta/meta.dart";

            abstract class CarrierPigeon implements Pigeon {}
            """
        )
    }

    @Test
    fun `implement multiple Kotlin external implicit interfaces`() = assertCompile {
        kotlin(
            """
            @DartLibrary("pigeons.dart")
            open external class Pigeon {
                constructor(implement: Interface)

                open fun fly(): Unit = definedExternally
            }

            @DartLibrary("pigeons.dart")
            open external class Carrier {
                constructor(implement: Interface)

                constructor(message: String)

                open fun send(): Unit = definedExternally
            }

            class CarrierPigeon : Pigeon(Interface), Carrier(Interface) {
                override fun send() {}
                override fun fly() {}
            }
            """
        )

        dart(
            """
            class Pigeon {
              void fly() {}
            }

            class Carrier {
              Carrier(String message);

              void send() {}
            }
            """,
            Path("lib/pigeons.dart"),
            assert = false
        )

        dart(
            """
            import "pigeons.dart" show Pigeon, Carrier;
            import "package:meta/meta.dart";

            @sealed
            class CarrierPigeon implements Pigeon, Carrier {
              @override
              void send() {}
              @override
              void fly() {}
            }
            """
        )
    }

    @Test
    fun `implement multiple Kotlin external implicit interfaces that can be mixed in`() = assertCompile {
        kotlin(
            """
            @DartLibrary("pigeons.dart")
            open external class Pigeon {
                constructor(useAs: InterfaceOrMixin)
            
                open fun fly(): Unit = definedExternally
            }

            @DartLibrary("pigeons.dart")
            open external class Carrier {
                constructor(useAs: InterfaceOrMixin)

                open fun send(): Unit = definedExternally
            }

            class CarrierPigeon : Pigeon(Interface), Carrier(Interface) {
                override fun send() {}
                override fun fly() {}
            }
            """
        )

        dart(
            """
            class Pigeon {
              void fly() {}
            }

            class Carrier {
              void send() {}
            }
            """,
            Path("lib/pigeons.dart"),
            assert = false
        )

        dart(
            """
            import "pigeons.dart" show Pigeon, Carrier;
            import "package:meta/meta.dart";

            @sealed
            class CarrierPigeon implements Pigeon, Carrier {
              @override
              void send() {}
              @override
              void fly() {}
            }
            """
        )
    }

    @Test
    fun `implement multiple Kotlin external mixins`() = assertCompile {
        kotlin(
            """
            @DartLibrary("pigeons.dart")
            open external class Pigeon {
                constructor(useAs: InterfaceOrMixin)

                open fun fly(): Unit = definedExternally
            }

            @DartLibrary("pigeons.dart")
            open external class Carrier {
                constructor(useAs: InterfaceOrMixin)

                open fun send(): Unit = definedExternally
            }

            class CarrierPigeon : Pigeon(Mixin), Carrier(Mixin)
            """
        )

        dart(
            """
            class Pigeon {
              void fly() {}
            }

            class Carrier {
              void send() {}
            }
            """,
            Path("lib/pigeons.dart"),
            assert = false
        )

        dart(
            """
            import "pigeons.dart" show Pigeon, Carrier;
            import "package:meta/meta.dart";

            @sealed
            class CarrierPigeon with Pigeon, Carrier {}
            """
        )
    }

    @Test
    fun `implement Kotlin external implicit interface and Kotlin external regular class`() = assertCompile {
        kotlin(
            """
            @DartLibrary("pigeons.dart")
            open external class Pigeon {
                constructor()
                constructor(implement: Interface)

                open fun fly(): Unit = definedExternally
            }

            @DartLibrary("pigeons.dart")
            open external class Carrier {
                constructor()
                constructor(implement: Interface)

                constructor(message: String)

                open fun send(): Unit = definedExternally
            }

            class CarrierPigeon : Pigeon(), Carrier(Interface) {
                override fun send() {}
                override fun fly() {}
            }
            """
        )

        dart(
            """
            class Pigeon {
              void fly() {}
            }

            class Carrier {
              Carrier(String message);

              void send() {}
            }
            """,
            Path("lib/pigeons.dart"),
            assert = false
        )

        dart(
            """
            import "pigeons.dart" show Pigeon, Carrier;
            import "package:meta/meta.dart";

            @sealed
            class CarrierPigeon extends Pigeon implements Carrier {
              @override
              void send() {}
              @override
              void fly() {}
            }
            """
        )
    }

    @Test
    fun `implement Kotlin external mixin and Kotlin external regular class`() = assertCompile {
        kotlin(
            """
            @DartLibrary("pigeons.dart")
            open external class Pigeon {
                constructor()
                constructor(implement: InterfaceOrMixin)

                open fun fly(): Unit = definedExternally
            }

            @DartLibrary("pigeons.dart")
            open external class Carrier {
                constructor(implement: InterfaceOrMixin)

                open fun send(): Unit = definedExternally
            }

            class CarrierPigeon : Pigeon(), Carrier(Mixin) {
                override fun send() {}
                override fun fly() {}
            }
            """
        )

        dart(
            """
            class Pigeon {
              void fly() {}
            }

            class Carrier {
              void send() {}
            }
            """,
            Path("lib/pigeons.dart"),
            assert = false
        )

        dart(
            """
            import "pigeons.dart" show Pigeon, Carrier;
            import "package:meta/meta.dart";

            @sealed
            class CarrierPigeon extends Pigeon with Carrier {
              @override
              void send() {}
              @override
              void fly() {}
            }
            """
        )
    }

    @Test
    fun `implement Kotlin external implicit interface and Kotlin external mixin`() = assertCompile {
        kotlin(
            """
            @DartLibrary("pigeons.dart")
            open external class Pigeon {
                constructor()
                constructor(implement: InterfaceOrMixin)

                open fun fly(): Unit = definedExternally
            }

            @DartLibrary("pigeons.dart")
            open external class Carrier {
                constructor(implement: InterfaceOrMixin)

                open fun send(): Unit = definedExternally
            }

            class CarrierPigeon : Pigeon(Interface), Carrier(Mixin) {
                override fun send() {}
                override fun fly() {}
            }
            """
        )

        dart(
            """
            class Pigeon {
              void fly() {}
            }

            class Carrier {
              void send() {}
            }
            """,
            Path("lib/pigeons.dart"),
            assert = false
        )

        dart(
            """
            import "pigeons.dart" show Pigeon, Carrier;
            import "package:meta/meta.dart";

            @sealed
            class CarrierPigeon with Carrier implements Pigeon {
              @override
              void send() {}
              @override
              void fly() {}
            }
            """
        )
    }

    @Test
    fun `implement Kotlin external regular class, Kotlin external implicit interface and mixin`() = assertCompile {
        kotlin(
            """
            @DartLibrary("pigeons.dart")
            open external class Bird(isCool: Boolean)

            @DartLibrary("pigeons.dart")
            open external class Pigeon {
                constructor()
                constructor(implement: InterfaceOrMixin)
            
                open fun fly(): Unit = definedExternally
            }

            @DartLibrary("pigeons.dart")
            open external class Carrier {
                constructor(implement: InterfaceOrMixin)

                open fun send(): Unit = definedExternally
            }

            class CarrierPigeon : Bird(isCool = true), Pigeon(Interface), Carrier(Mixin) {
                override fun send() {}
                override fun fly() {}
            }
            """
        )

        dart(
            """
            class Bird {
              Bird(bool isCool);
            }

            class Pigeon {
              void fly() {}
            }

            class Carrier {
              void send() {}
            }
            """,
            Path("lib/pigeons.dart"),
            assert = false
        )

        dart(
            """
            import "pigeons.dart" show Bird, Pigeon, Carrier;
            import "package:meta/meta.dart";

            @sealed
            class CarrierPigeon extends Bird with Carrier implements Pigeon {
              CarrierPigeon() : super(true);
              @override
              void send() {}
              @override
              void fly() {}
            }
            """
        )
    }
}