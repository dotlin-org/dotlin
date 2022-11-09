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

@DisplayName("Compile: Dialect: Special Inheritance")
class SpecialInheritance : BaseTest {
    @Test
    fun `implement multiple implicit interfaces`() = assertCompile {
        kotlin(
            """
            open external class Pigeon {
                constructor(useAs: Interface)
            
                open fun fly(): Unit = definedExternally
            }

            open external class Carrier {
                constructor(useAs: Interface)
            
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
            import 'package:meta/meta.dart';

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
    fun `implement multiple implicit interfaces that can be mixed in`() = assertCompile {
        kotlin(
            """
            open external class Pigeon {
                constructor(useAs: InterfaceOrMixin)
            
                open fun fly(): Unit = definedExternally
            }

            open external class Carrier {
                constructor(useAs: InterfaceOrMixin)
            
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
            import 'package:meta/meta.dart';

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
    fun `implement multiple mixins`() = assertCompile {
        kotlin(
            """
            open external class Pigeon {
                constructor(useAs: InterfaceOrMixin)
            
                open fun fly(): Unit = definedExternally
            }

            open external class Carrier {
                constructor(useAs: InterfaceOrMixin)
            
                open fun send(): Unit = definedExternally
            }

            class CarrierPigeon : Pigeon(Mixin), Carrier(Mixin)
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class CarrierPigeon with Pigeon, Carrier {}
            """
        )
    }

    @Test
    fun `implement implicit interface and regular external class`() = assertCompile {
        kotlin(
            """
            open external class Pigeon {
                constructor()
                constructor(implement: Interface)
            
                open fun fly(): Unit = definedExternally
            }

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
            import 'package:meta/meta.dart';

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
    fun `implement mixin and regular external class`() = assertCompile {
        kotlin(
            """
            open external class Pigeon {
                constructor()
                constructor(implement: InterfaceOrMixin)
            
                open fun fly(): Unit = definedExternally
            }

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
            import 'package:meta/meta.dart';

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
    fun `implement implicit interface and mixin`() = assertCompile {
        kotlin(
            """
            open external class Pigeon {
                constructor()
                constructor(implement: InterfaceOrMixin)
            
                open fun fly(): Unit = definedExternally
            }

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
            import 'package:meta/meta.dart';

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
    fun `implement regular external class, implicit interface and mixin`() = assertCompile {
        kotlin(
            """
            open external class Bird {
                constructor(isCool: Boolean)
            }

            open external class Pigeon {
                constructor()
                constructor(implement: InterfaceOrMixin)
            
                open fun fly(): Unit = definedExternally
            }

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
            import 'package:meta/meta.dart';
            
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