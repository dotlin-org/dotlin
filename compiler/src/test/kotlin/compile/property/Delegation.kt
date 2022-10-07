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

package compile.property

import BaseTest
import assertCompile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compile: Property: Delegation")
class Delegation : BaseTest {
    @Test
    fun `delegated top-level property val`() = assertCompile {
        kotlin(
            """
            import kotlin.reflect.KProperty
            
            class Delegate {
                operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
                    return "${'$'}thisRef has ${'$'}{property.name}"
                }
            
                operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
                    "${'$'}{property.name} = ${'$'}value in ${'$'}thisRef"
                }
            }

            val x by Delegate()
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Delegate {
              @nonVirtual
              String getValue(
                Object? thisRef,
                KProperty<dynamic> property,
              ) {
                return '${'$'}{thisRef} has ${'$'}{property.name}';
              }

              @nonVirtual
              void setValue(
                Object? thisRef,
                KProperty<dynamic> property,
                String value,
              ) {
                '${'$'}{property.name} = ${'$'}{value} in ${'$'}{thisRef}';
              }
            }

            String get x {
              return _x${'$'}delegate.getValue(null, x${'$'}kProperty);
            }
            
            final Delegate _x${'$'}delegate = Delegate();
            const KProperty0Impl<String> x${'$'}kProperty = KProperty0Impl<String>('x', _${'$'}381);
            String _${'$'}381() => x;
            """
        )
    }

    @Test
    fun `delegated top-level property var`() = assertCompile {
        kotlin(
            """
            import kotlin.reflect.KProperty
            
            class Delegate {
                operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
                    return "${'$'}thisRef has ${'$'}{property.name}"
                }
            
                operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
                    "${'$'}{property.name} = ${'$'}value in ${'$'}thisRef"
                }
            }

            var x by Delegate()
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Delegate {
              @nonVirtual
              String getValue(
                Object? thisRef,
                KProperty<dynamic> property,
              ) {
                return '${'$'}{thisRef} has ${'$'}{property.name}';
              }

              @nonVirtual
              void setValue(
                Object? thisRef,
                KProperty<dynamic> property,
                String value,
              ) {
                '${'$'}{property.name} = ${'$'}{value} in ${'$'}{thisRef}';
              }
            }

            String get x {
              return _x${'$'}delegate.getValue(null, x${'$'}kProperty);
            }

            void set x(String ${'$'}value) {
              return _x${'$'}delegate.setValue(null, x${'$'}kProperty, ${'$'}value);
            }

            final Delegate _x${'$'}delegate = Delegate();
            const KMutableProperty0Impl<String> x${'$'}kProperty =
                KMutableProperty0Impl<String>('x', _${'$'}381, _${'$'}381With${'$'}value);
            String _${'$'}381() => x;
            String _${'$'}381With${'$'}value(String ${'$'}value) => x = ${'$'}value;
            """
        )
    }

    @Test
    fun `delegated class property val`() = assertCompile {
        kotlin(
            """
            import kotlin.reflect.KProperty
            
            class Delegate {
                operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
                    return "${'$'}thisRef has ${'$'}{property.name}"
                }
            
                operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
                    "${'$'}{property.name} = ${'$'}value in ${'$'}thisRef"
                }
            }

            class Test {
                val x by Delegate()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Delegate {
              @nonVirtual
              String getValue(
                Object? thisRef,
                KProperty<dynamic> property,
              ) {
                return '${'$'}{thisRef} has ${'$'}{property.name}';
              }

              @nonVirtual
              void setValue(
                Object? thisRef,
                KProperty<dynamic> property,
                String value,
              ) {
                '${'$'}{property.name} = ${'$'}{value} in ${'$'}{thisRef}';
              }
            }

            @sealed
            class Test {
              @nonVirtual
              String get x {
                return this._x${'$'}delegate.getValue(this, this.x${'$'}kProperty);
              }

              final Delegate _x${'$'}delegate = Delegate();
              @nonVirtual
              late final KProperty0Impl<String> x${'$'}kProperty =
                  KProperty0Impl<String>('x', () => this.x);
            }
            """
        )
    }

    @Test
    fun `delegated class property var`() = assertCompile {
        kotlin(
            """
            import kotlin.reflect.KProperty
            
            class Delegate {
                operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
                    return "${'$'}thisRef has ${'$'}{property.name}"
                }
            
                operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
                    "${'$'}{property.name} = ${'$'}value in ${'$'}thisRef"
                }
            }

            class Test {
                var x by Delegate()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Delegate {
              @nonVirtual
              String getValue(
                Object? thisRef,
                KProperty<dynamic> property,
              ) {
                return '${'$'}{thisRef} has ${'$'}{property.name}';
              }

              @nonVirtual
              void setValue(
                Object? thisRef,
                KProperty<dynamic> property,
                String value,
              ) {
                '${'$'}{property.name} = ${'$'}{value} in ${'$'}{thisRef}';
              }
            }

            @sealed
            class Test {
              @nonVirtual
              String get x {
                return this._x${'$'}delegate.getValue(this, this.x${'$'}kProperty);
              }

              @nonVirtual
              void set x(String ${'$'}value) {
                return this._x${'$'}delegate.setValue(this, this.x${'$'}kProperty, ${'$'}value);
              }

              final Delegate _x${'$'}delegate = Delegate();
              @nonVirtual
              late final KMutableProperty0Impl<String> x${'$'}kProperty =
                  KMutableProperty0Impl<String>(
                      'x', () => this.x, (String ${'$'}value) => this.x = ${'$'}value);
            }
            """
        )
    }

    @Test
    fun `delegated local property val`() = assertCompile {
        kotlin(
            """
            import kotlin.reflect.KProperty
            
            class Delegate {
                operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
                    return "${'$'}thisRef has ${'$'}{property.name}"
                }
            
                operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
                    "${'$'}{property.name} = ${'$'}value in ${'$'}thisRef"
                }
            }

            fun main() {
                val x by Delegate()

                val y = x
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Delegate {
              @nonVirtual
              String getValue(
                Object? thisRef,
                KProperty<dynamic> property,
              ) {
                return '${'$'}{thisRef} has ${'$'}{property.name}';
              }

              @nonVirtual
              void setValue(
                Object? thisRef,
                KProperty<dynamic> property,
                String value,
              ) {
                '${'$'}{property.name} = ${'$'}{value} in ${'$'}{thisRef}';
              }
            }

            void main() {
              final Delegate x${'$'}delegate = Delegate();
              String get${'$'}x() {
                late final KProperty0Impl<String> x${'$'}kProperty = KProperty0Impl<String>(
                    'x',
                    () =>
                        throw UnsupportedError('Cannot call getter for this declaration'));
                return x${'$'}delegate.getValue(null, x${'$'}kProperty);
              }

              final String y = get${'$'}x();
            }
            """
        )
    }

    @Test
    fun `delegated local property var`() = assertCompile {
        kotlin(
            """
            import kotlin.reflect.KProperty
            
            class Delegate {
                operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
                    return "${'$'}thisRef has ${'$'}{property.name}"
                }
            
                operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
                    "${'$'}{property.name} = ${'$'}value in ${'$'}thisRef"
                }
            }

            fun main() {
                var x by Delegate()
                val y = x
                x = y
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Delegate {
              @nonVirtual
              String getValue(
                Object? thisRef,
                KProperty<dynamic> property,
              ) {
                return '${'$'}{thisRef} has ${'$'}{property.name}';
              }

              @nonVirtual
              void setValue(
                Object? thisRef,
                KProperty<dynamic> property,
                String value,
              ) {
                '${'$'}{property.name} = ${'$'}{value} in ${'$'}{thisRef}';
              }
            }

            void main() {
              final Delegate x${'$'}delegate = Delegate();
              String get${'$'}x() {
                late final KMutableProperty0Impl<String> x${'$'}kProperty =
                    KMutableProperty0Impl<String>(
                        'x',
                        () => throw UnsupportedError(
                            'Cannot call getter for this declaration'),
                        (String ${'$'}value) => throw UnsupportedError(
                            'Cannot call setter for this declaration'));
                return x${'$'}delegate.getValue(null, x${'$'}kProperty);
              }

              void set${'$'}x(String value) {
                late final KMutableProperty0Impl<String> x${'$'}kProperty =
                    KMutableProperty0Impl<String>(
                        'x',
                        () => throw UnsupportedError(
                            'Cannot call getter for this declaration'),
                        (String ${'$'}value) => throw UnsupportedError(
                            'Cannot call setter for this declaration'));
                return x${'$'}delegate.setValue(null, x${'$'}kProperty, value);
              }

              final String y = get${'$'}x();
              set${'$'}x(y);
            }
            """
        )
    }

    @Test
    fun `single expression lazy property`() = assertCompile {
        kotlin(
            """
            val myLazy by lazy { 100 }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            late final int myLazy = 100;
            """
        )
    }

    @Test
    fun `multiple expressions lazy property`() = assertCompile {
        kotlin(
            """
            val myLazy by lazy {
                val x = 340
                x + x * 8
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            late final int myLazy = () {
              final int x = 340;
              return x + x * 8;
            }.call();
            """
        )
    }

    @Test
    fun `single expression local lazy property`() = assertCompile {
        kotlin(
            """
            fun main() {
                val myLazy by lazy { 100 }

                myLazy
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              late final int myLazy = 100;
              myLazy;
            }
            """
        )
    }

    @Test
    fun `multiple expressions local lazy property`() = assertCompile {
        kotlin(
            """
            fun main() {
                val myLazy by lazy {
                    val x = 340
                    x + x * 8
                }

                myLazy
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              late final int myLazy = () {
                final int x = 340;
                return x + x * 8;
              }.call();
              myLazy;
            }
            """
        )
    }
}