/*
 * Copyright 2021-2022 Wilko Manger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package compile

import BaseTest
import DefaultValue
import assertCompile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Compile: Class")
class Class : BaseTest {
    @Test
    fun `class`() = assertCompile {
        kotlin("class Test")

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {}
            """
        )
    }

    @Test
    fun `private class`() = assertCompile {
        kotlin("private class Test")

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class _Test {}
            """
        )
    }

    @Test
    fun `internal class`() = assertCompile {
        kotlin("internal class Test")

        dart(
            """
            import 'package:meta/meta.dart';

            @internal
            @sealed
            class Test {}
            """
        )
    }

    @Nested
    inner class Property {
        @Test
        fun `class with single property in body`() = assertCompile {
            kotlin(
                """
                class Test {
                    val property = 96
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  final int property = 96;
                }
                """
            )
        }

        @Test
        fun `class with single simple property in body and calling getter`() = assertCompile {
            kotlin(
                """
                class Test {
                    val property = 96
                }

                fun main() {
                    Test().property
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  final int property = 96;
                }

                void main() {
                  Test().property;
                }
                """
            )
        }

        @Test
        fun `class with single simple property in body and calling setter`() = assertCompile {
            kotlin(
                """
                class Test {
                    var property = 96
                }

                fun main() {
                    Test().property = 123
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  int property = 96;
                }

                void main() {
                  Test().property = 123;
                }
                """
            )
        }

        @Test
        fun `class with single property in body and calling custom setter`() = assertCompile {
            kotlin(
                """
                class Test {
                    fun sideEffect() {}

                    var property: Int = 0
                        set(value) {
                            sideEffect()
                        }
                }

                fun main() {
                    Test().property = 123
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  void sideEffect() {}
                  @nonVirtual
                  int _${'$'}propertyBackingField = 0;
                  @nonVirtual
                  int get property {
                    return this._${'$'}propertyBackingField;
                  }

                  @nonVirtual
                  void set property(int value) {
                    this.sideEffect();
                  }
                }

                void main() {
                  Test().property = 123;
                }
                """
            )
        }

        @Test
        fun `class with single simple property in body and calling private setter`() = assertCompile {
            kotlin(
                """
                class Test {
                    fun sideEffect() {}

                    var property: Int = 0
                        private set

                    fun doIt() {
                        property = 3
                    }
                }

                fun main() {
                    Test().doIt()
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  void sideEffect() {}
                  @nonVirtual
                  int _${'$'}propertyBackingField = 0;
                  @nonVirtual
                  int get property {
                    return this._${'$'}propertyBackingField;
                  }

                  @nonVirtual
                  void set _property(int ${'$'}value) {
                    this._${'$'}propertyBackingField = ${'$'}value;
                  }

                  @nonVirtual
                  void doIt() {
                    this.property = 3;
                  }
                }

                void main() {
                  Test().doIt();
                }
                """
            )
        }

        @Test
        fun `class with single property in primary constructor`() = assertCompile {
            kotlin(
                """
                class Test(val property: Int = 96)
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  Test({this.property = 96}) : super();
                  @nonVirtual
                  final int property;
                }
                """
            )
        }

        @Test
        fun `class with single property with complex default value in primary constructor`() = assertCompile {
            kotlin(
                """
                fun returnsInt(): Int {
                    return 343
                }
    
                class Test(val property: Int = returnsInt())
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                int returnsInt() {
                  return 343;
                }
                
                @sealed
                class Test {
                  Test({int? property = null})
                      : property = property == null ? returnsInt() : property,
                        super();
                  @nonVirtual
                  final int property;
                }
                """
            )
        }

        @Test
        fun `class with single nullable property with complex default value in primary constructor`() =
            assertCompile {
                kotlin(
                    """
                    fun returnsInt(): Int {
                        return 343
                    }
    
                    class Test(val property: Int? = returnsInt())
                    """
                )

                dart(
                    """
                    import 'package:meta/meta.dart';

                    int returnsInt() {
                      return 343;
                    }
                    
                    @sealed
                    class Test {
                      Test({dynamic property = const _$DefaultValue()})
                          : property = property == const _$DefaultValue()
                                ? returnsInt()
                                : property as int?,
                            super();
                      @nonVirtual
                      final int? property;
                    }
                    
                    @sealed
                    class _$DefaultValue {
                      const _$DefaultValue();
                      @nonVirtual
                      dynamic noSuchMethod(Invocation invocation) {}
                    }
                    """
                )
            }

        @Test
        fun `class with two properties in body`() = assertCompile {
            kotlin(
                """
                class Test {
                    val property1 = 19
                    val property2 = 96
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  final int property1 = 19;
                  @nonVirtual
                  final int property2 = 96;
                }
                """
            )
        }

        @Test
        fun `class with two properties in primary constructor`() = assertCompile {
            kotlin(
                """
                class Test(val property1: Int = 19, val property2: Int = 96)
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  Test({
                    this.property1 = 19,
                    this.property2 = 96,
                  }) : super();
                  @nonVirtual
                  final int property1;
                  @nonVirtual
                  final int property2;
                }
                """
            )
        }

        @Test
        fun `class with two properties with complex default values in primary constructor`() = assertCompile {
            kotlin(
                """
                fun returnsInt(): Int {
                    return 343
                }
    
                class Test(val property1: Int = returnsInt(), val property2: Int = returnsInt())
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                int returnsInt() {
                  return 343;
                }
                
                @sealed
                class Test {
                  Test({
                    int? property1 = null,
                    int? property2 = null,
                  })  : property1 = property1 == null ? returnsInt() : property1,
                        property2 = property2 == null ? returnsInt() : property2,
                        super();
                  @nonVirtual
                  final int property1;
                  @nonVirtual
                  final int property2;
                }
                """
            )
        }

        @Test
        fun `class with two nullable properties with complex default values in primary constructor`() =
            assertCompile {
                kotlin(
                    """
                    fun returnsInt(): Int {
                        return 343
                    }
    
                    class Test(val property1: Int? = returnsInt(), val property2: Int? = returnsInt())
                    """
                )

                dart(
                    """
                    import 'package:meta/meta.dart';

                    int returnsInt() {
                      return 343;
                    }
                    
                    @sealed
                    class Test {
                      Test({
                        dynamic property1 = const _$DefaultValue(),
                        dynamic property2 = const _$DefaultValue(),
                      })  : property1 = property1 == const _$DefaultValue()
                                ? returnsInt()
                                : property1 as int?,
                            property2 = property2 == const _$DefaultValue()
                                ? returnsInt()
                                : property2 as int?,
                            super();
                      @nonVirtual
                      final int? property1;
                      @nonVirtual
                      final int? property2;
                    }
                    
                    @sealed
                    class _$DefaultValue {
                      const _$DefaultValue();
                      @nonVirtual
                      dynamic noSuchMethod(Invocation invocation) {}
                    }
                    """
                )
            }

        @Test
        fun `class with property getter`() = assertCompile {
            kotlin(
                """
                class Test {
                    val property: Int
                        get() {
                            return 343
                        }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  int get property {
                    return 343;
                  }
                }
                """
            )
        }

        @Test
        fun `class with property getter and setter`() = assertCompile {
            kotlin(
                """
                class Test {
                    fun sideEffect(x: Int) {}
    
                    var property: Int
                        get() {
                            return 343
                        }
                        set(value: Int) {
                            sideEffect(value)
                        }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  void sideEffect(int x) {}
                  @nonVirtual
                  int get property {
                    return 343;
                  }
    
                  @nonVirtual
                  void set property(int value) {
                    this.sideEffect(value);
                  }
                }
                """
            )
        }

        @Test
        fun `class with property getter and setter with backing field`() = assertCompile {
            kotlin(
                """
                class Test {
                    fun sideEffect(x: Int) {}
        
                    var property: Int = 0
                        get() {
                            sideEffect(field)
                            return field
                        }
                        set(value) {
                            sideEffect(value)
                            field = value
                        }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  void sideEffect(int x) {}
                  @nonVirtual
                  int _${'$'}propertyBackingField = 0;
                  @nonVirtual
                  int get property {
                    this.sideEffect(this._${'$'}propertyBackingField);
                    return this._${'$'}propertyBackingField;
                  }
        
                  @nonVirtual
                  void set property(int value) {
                    this.sideEffect(value);
                    this._${'$'}propertyBackingField = value;
                  }
                }
                """
            )
        }

        @Test
        fun `class with body property referencing primary constructor parameter`() = assertCompile {
            kotlin(
                """
                class Test(param: Int) {
                    val property = param
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  Test(int param)
                      : property = param,
                        super();
                  @nonVirtual
                  final int property;
                }
                """
            )
        }

        @Test
        fun `class with body properties referencing primary constructor parameters`() = assertCompile {
            kotlin(
                """
                class Test(x: Int, y: Int, z: Int) {
                    val sum = x + y + z
                    val sumTimesSum = sum * sum
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  Test(
                    int x,
                    int y,
                    int z,
                  )   : sum = x + y + z,
                        super() {
                    this.sumTimesSum = this.sum * this.sum;
                  }
                  @nonVirtual
                  final int sum;
                  @nonVirtual
                  late final int sumTimesSum;
                }
                """
            )
        }

        @Test
        fun `class with body properties referencing primary constructor properties`() = assertCompile {
            kotlin(
                """
                class Test(val x: Int, val y: Int, val z: Int) {
                    val sum = x + y + z
                    val sumTimesSum = sum * sum
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  Test(
                    this.x,
                    this.y,
                    this.z,
                  ) : super() {
                    this.sum = this.x + this.y + this.z;
                    this.sumTimesSum = this.sum * this.sum;
                  }
                  @nonVirtual
                  final int x;
                  @nonVirtual
                  final int y;
                  @nonVirtual
                  final int z;
                  @nonVirtual
                  late final int sum;
                  @nonVirtual
                  late final int sumTimesSum;
                }
                """
            )
        }

        @Test
        fun `class with body properties referencing primary constructor parameter and property with same name`() =
            assertCompile {
                kotlin(
                    """
                    class Test(x: Int) {
                        val x = 34
                        val xTimesX = this.x * x
                    }
                    """
                )

                dart(
                    """
                    import 'package:meta/meta.dart';

                    @sealed
                    class Test {
                      Test(int x) : super() {
                        this.xTimesX = this.x * x;
                      }
                      @nonVirtual
                      final int x = 34;
                      @nonVirtual
                      late final int xTimesX;
                    }
                    """
                )
            }

        @Test
        fun `class with body property with complex default value`() = assertCompile {
            kotlin(
                """
                fun returnsInt(): Int {
                    return 343
                }

                class Test {
                    val x = returnsInt()
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                int returnsInt() {
                  return 343;
                }
                
                @sealed
                class Test {
                  @nonVirtual
                  final int x = returnsInt();
                }
                """
            )
        }

        @Test
        fun `class with body property with complex default value from method`() = assertCompile {
            kotlin(
                """
                class Test {
                    val x = returnsInt()
    
                    fun returnsInt(): Int {
                        return 343
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  Test() : super() {
                    this.x = this.returnsInt();
                  }
                  @nonVirtual
                  late final int x;
                  @nonVirtual
                  int returnsInt() {
                    return 343;
                  }
                }
                """
            )
        }
    }

    @Test
    fun `class with property initialized by parameter`() =
        assertCompile {
            kotlin(
                """
                    class Vector(x: Int) {
                        val y = x
                    }
                    """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Vector {
                  Vector(int x)
                      : y = x,
                        super();
                  @nonVirtual
                  final int y;
                }
                """
            )
        }

    @Test
    fun `class with property initialized by parameter or if null other value`() =
        assertCompile {
            kotlin(
                """
                    class Vector(x: Int?) {
                        val y = x ?: 3
                    }
                    """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Vector {
                  Vector(int? x)
                      : y = x ?? 3,
                        super();
                  @nonVirtual
                  final int y;
                }
                """
            )
        }

    @Nested
    inner class Inheritance {
        @Test
        fun `class that inherits class`() = assertCompile {
            kotlin(
                """
                open class A
                class B : A()
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                class A {}
                
                @sealed
                class B extends A {}
                """
            )
        }

        @Test
        fun `class that inherits class and calls super constructor with literal argument`() = assertCompile {
            kotlin(
                """
                open class A(x: Int)
                class B : A(3)
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                class A {
                  A(int x) : super();
                }
                
                @sealed
                class B extends A {
                  B() : super(3);
                }
                """
            )
        }

        @Test
        fun `class that inherits class and calls super constructor with parameter argument`() = assertCompile {
            kotlin(
                """
                open class A(x: Int)
                class B(y: Int) : A(y)
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                class A {
                  A(int x) : super();
                }
                
                @sealed
                class B extends A {
                  B(int y) : super(y);
                }
                """
            )
        }

        @Test
        fun `class that inherits class and calls super constructor with parameter argument that has default value`() =
            assertCompile {
                kotlin(
                    """
                    open class A(x: Int)
                    class B(y: Int = 0) : A(y)
                    """
                )

                dart(
                    """
                    import 'package:meta/meta.dart';

                    class A {
                      A(int x) : super();
                    }
                
                    @sealed
                    class B extends A {
                      B({int y = 0}) : super(y);
                    }
                    """
                )
            }

        @Test
        fun `class that inherits class and calls super constructor with primitive parameter argument that has complex default value`() =
            assertCompile {
                kotlin(
                    """
                    fun returnsInt(): Int {
                        return 343
                    }
    
                    open class A(x: Int)
                    class B(y: Int = returnsInt()) : A(y)
                    """
                )

                dart(
                    """
                    import 'package:meta/meta.dart';

                    int returnsInt() {
                      return 343;
                    }
                    
                    class A {
                      A(int x) : super();
                    }
                    
                    @sealed
                    class B extends A {
                      B._${'$'}(int y) : super(y);
                      factory B({int? y = null}) {
                        y = y == null ? returnsInt() : y;
                        return B._${'$'}(y);
                      }
                    }
                    """
                )
            }

        @Test
        fun `class that inherits class and calls super constructor with non-primitive parameter argument that has complex default value`() =
            assertCompile {
                kotlin(
                    """
                    class Vector

                    fun returnsVector(): Vector {
                        return Vector()
                    }
    
                    open class A(x: Vector)
                    class B(y: Vector = returnsVector()) : A(y)
                    """
                )

                dart(
                    """
                    import 'package:meta/meta.dart';

                    @sealed
                    class Vector {}
                    
                    Vector returnsVector() {
                      return Vector();
                    }
                    
                    class A {
                      A(Vector x) : super();
                    }
                    
                    @sealed
                    class B extends A {
                      B._${'$'}(Vector y) : super(y);
                      factory B({Vector? y = null}) {
                        y = y == null ? returnsVector() : y;
                        return B._${'$'}(y);
                      }
                    }
                    """
                )
            }

        @Test
        fun `class that inherits class and calls super constructor with nullable non-primitive parameter argument that has complex default value`() =
            assertCompile {
                kotlin(
                    """
                    class Vector

                    fun returnsVector(): Vector {
                        return Vector()
                    }
    
                    open class A(x: Vector?)
                    class B(y: Vector? = returnsVector()) : A(y)
                    """
                )

                dart(
                    """
                    import 'package:meta/meta.dart';

                    @sealed
                    class Vector {}
                    
                    Vector returnsVector() {
                      return Vector();
                    }
                    
                    class A {
                      A(Vector? x) : super();
                    }
                    
                    @sealed
                    class B extends A {
                      B._${'$'}(Vector? y) : super(y);
                      factory B({Vector? y = const _${'$'}DefaultVectorValue()}) {
                        y = y == const _${'$'}DefaultVectorValue() ? returnsVector() : y;
                        return B._${'$'}(y);
                      }
                    }
                    
                    @sealed
                    class _${'$'}DefaultVectorValue implements Vector {
                      const _${'$'}DefaultVectorValue();
                      @nonVirtual
                      dynamic noSuchMethod(Invocation invocation) {}
                    }
                    """
                )
            }

        @Test
        fun `class that overrides property in body`() = assertCompile {
            kotlin(
                """
                open class A {
                    open val property: Int = 0
                }

                class B : A() {
                    override val property: Int = 1
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                class A {
                  final int property = 0;
                }
                
                @sealed
                class B extends A {
                  @override
                  final int property = 1;
                }
                """
            )
        }

        @Test
        fun `class that overrides property in primary constructor`() = assertCompile {
            kotlin(
                """
                open class A {
                    open val property: Int = 0
                }

                class B(override val property: Int) : A()
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                class A {
                  final int property = 0;
                }
                
                @sealed
                class B extends A {
                  B(this.property) : super();
                  @override
                  final int property;
                }
                """
            )
        }

        @Test
        fun `class that overrides property in primary constructor but also passed through super constructor`() =
            assertCompile {
                kotlin(
                    """
                    open class A(open val property: Int = 0)
                    class B(override val property: Int) : A(property)
                    """
                )

                dart(
                    """
                    import 'package:meta/meta.dart';

                    class A {
                      A({this.property = 0}) : super();
                      final int property;
                    }
                    
                    @sealed
                    class B extends A {
                      B(this.property) : super(property: property);
                      @override
                      final int property;
                    }
                    """
                )
            }
    }

    @Nested
    inner class Interface {
        @Test
        fun `class implements interface`() = assertCompile {
            kotlin(
                """
                interface Marker
    
                class Test : Marker
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Marker {}
    
                @sealed
                class Test implements Marker {}
                """
            )
        }

        @Test
        fun `class implements two interfaces`() = assertCompile {
            kotlin(
                """
                interface Marker
                interface Marker2
    
                class Test : Marker, Marker2
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Marker {}
                
                abstract class Marker2 {}
    
                @sealed
                class Test implements Marker, Marker2 {}
                """
            )
        }

        @Test
        fun `interface implements interface with method`() = assertCompile {
            kotlin(
                """
                interface Marker {
                    fun doSomething()
                }

                interface Marked : Marker
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Marker {
                  void doSomething();
                }

                abstract class Marked implements Marker {}
                """
            )
        }

        @Test
        fun `class implements interface with method`() = assertCompile {
            kotlin(
                """
                interface Test {
                    fun method(x: Int, y: Int = 0, z: Int? = null)
                }

                class TestImpl : Test {
                    override fun method(x: Int, y: Int, z: Int?) {}
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Test {
                  void method(
                    int x, {
                    int y = 0,
                    int? z = null,
                  });
                }
                
                @sealed
                class TestImpl implements Test {
                  @override
                  void method(
                    int x, {
                    int y = 0,
                    int? z = null,
                  }) {}
                }
                """
            )
        }

        @Test
        fun `class implements interface with method with complex default values`() = assertCompile {
            kotlin(
                """
                interface Test {
                    fun method(x: Int, y: Int = returnsInt(), z: Int? = returnsInt())
                }
    
                fun returnsInt(): Int {
                    return 343
                }

                class TestImpl : Test {
                    override fun method(x: Int, y: Int, z: Int?) {}
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Test {
                  void method(
                    int x, {
                    int? y = null,
                    dynamic z = const _$DefaultValue(),
                  }) {
                    y = y == null ? returnsInt() : y;
                    z = z == const _$DefaultValue() ? returnsInt() : z as int?;
                  }
                }
                
                int returnsInt() {
                  return 343;
                }
                
                @sealed
                class TestImpl implements Test {
                  @override
                  void method(
                    int x, {
                    int? y = null,
                    dynamic z = const _$DefaultValue(),
                  }) {
                    y = y == null ? returnsInt() : y;
                    z = z == const _$DefaultValue() ? returnsInt() : z as int?;
                  }
                }
                
                @sealed
                class _$DefaultValue {
                  const _$DefaultValue();
                  @nonVirtual
                  dynamic noSuchMethod(Invocation invocation) {}
                }
                """
            )
        }

        @Test
        fun `class implements interface with default method implementation`() = assertCompile {
            kotlin(
                """
                interface Test {
                    fun method(x: Int, y: Int = 0, z: Int? = null): Int {
                        return 343
                    }
                }
    
                class TestImpl : Test
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Test {
                  int method(
                    int x, {
                    int y = 0,
                    int? z = null,
                  }) {
                    return 343;
                  }
                }
                
                @sealed
                class TestImpl implements Test {
                  @override
                  int method(
                    int x, {
                    int y = 0,
                    int? z = null,
                  }) {
                    return 343;
                  }
                }
                """
            )
        }

        @Test
        fun `class implements interface with default method implementation with complex default values`() =
            assertCompile {
                kotlin(
                    """
                    interface Test {
                        fun method(x: Int, y: Int = returnsInt(), z: Int? = returnsInt()): Int {
                            return 343
                        }
                    }
    
                    fun returnsInt(): Int {
                        return 343
                    }
    
                    class TestImpl : Test
                    """
                )

                dart(
                    """
                    import 'package:meta/meta.dart';

                    abstract class Test {
                      int method(
                        int x, {
                        int? y = null,
                        dynamic z = const _$DefaultValue(),
                      }) {
                        y = y == null ? returnsInt() : y;
                        z = z == const _$DefaultValue() ? returnsInt() : z as int?;
                        return 343;
                      }
                    }
                    
                    int returnsInt() {
                      return 343;
                    }
                    
                    @sealed
                    class TestImpl implements Test {
                      @override
                      int method(
                        int x, {
                        int? y = null,
                        dynamic z = const _$DefaultValue(),
                      }) {
                        y = y == null ? returnsInt() : y;
                        z = z == const _$DefaultValue() ? returnsInt() : z as int?;
                        return 343;
                      }
                    }
                    
                    @sealed
                    class _$DefaultValue {
                      const _$DefaultValue();
                      @nonVirtual
                      dynamic noSuchMethod(Invocation invocation) {}
                    }
                    """
                )
            }

        @Test
        fun `class implements interface with property`() = assertCompile {
            kotlin(
                """
                interface Test {
                    val property: Int
                }

                class TestImpl : Test {
                    override val property = 343
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Test {
                  abstract final int property;
                }
    
                @sealed
                class TestImpl implements Test {
                  @override
                  final int property = 343;
                }
                """
            )
        }

        @Test
        fun `class implements interface with property with default getter`() = assertCompile {
            kotlin(
                """
                interface Test {
                    val property: Int
                        get() {
                            return 343
                        }
                }
    
                class TestImpl : Test
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Test {
                  int get property {
                    return 343;
                  }
                }
    
                @sealed
                class TestImpl implements Test {
                  @override
                  int get property {
                    return 343;
                  }
                }
                """
            )
        }

        @Test
        fun `class implements interface with property with default getter and setter`() = assertCompile {
            kotlin(
                """
                fun sideEffect(x: Int) {}
    
                interface Test {
                    var property: Int
                        get() {
                            return 343
                        }
                        set(value: Int) {
                            sideEffect(value)
                        }
                }
    
                class TestImpl : Test
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                void sideEffect(int x) {}
    
                abstract class Test {
                  int get property {
                    return 343;
                  }
    
                  void set property(int value) {
                    sideEffect(value);
                  }
                }
    
                @sealed
                class TestImpl implements Test {
                  @override
                  int get property {
                    return 343;
                  }
    
                  @override
                  void set property(int value) {
                    sideEffect(value);
                  }
                }
                """
            )
        }

        @Test
        fun `diamond problem`() = assertCompile {
            kotlin(
                """
                interface Base {
                    fun sayHello(): String
                }

                interface A : Base {
                    override fun sayHello() = "I am A"
                }

                interface B : Base {
                    override fun sayHello() = "I am B"
                }

                class Test : A, B {
                    override fun sayHello() = super<B>.sayHello()
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Base {
                  String sayHello();
                }

                abstract class A implements Base {
                  @override
                  String sayHello() {
                    return 'I am A';
                  }
                }

                abstract class B implements Base {
                  @override
                  String sayHello() {
                    return 'I am B';
                  }
                }

                @sealed
                class Test implements A, B {
                  @override
                  String sayHello() {
                    return this._B${'$'}sayHello();
                  }

                  String _B${'$'}sayHello() {
                    return 'I am B';
                  }
                }
                """
            )
        }

        @Test
        fun  `diamond problem with getter`() = assertCompile {
            kotlin(
                """
                interface Base {
                    val greeting: String
                }

                interface A : Base {
                    override val greeting: String
                        get() = "I am A"
                }

                interface B : Base {
                    override val greeting: String
                        get() = "I am B"
                }

                class Test : A, B {
                    override val greeting: String
                        get() = super<B>.greeting
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Base {
                  abstract final String greeting;
                }

                abstract class A implements Base {
                  @override
                  String get greeting {
                    return 'I am A';
                  }
                }

                abstract class B implements Base {
                  @override
                  String get greeting {
                    return 'I am B';
                  }
                }

                @sealed
                class Test implements A, B {
                  @override
                  String get greeting {
                    return this._B${'$'}greeting;
                  }

                  String get _B${'$'}greeting {
                    return 'I am B';
                  }
                }
                """
            )
        }

        @Test
        fun `diamond problem with getter and setter`() = assertCompile {
            kotlin(
                """
                fun sideEffect(x: String) {}

                interface Base {
                    var greeting: String
                }

                interface A : Base {
                    override var greeting: String
                        get() = "I am A"
                        set(value) = sideEffect("I am A")
                }

                interface B : Base {
                    override var greeting: String
                        get() = "I am B"
                        set(value) = sideEffect("I am B")
                }

                class Test : A, B {
                    override var greeting: String
                        get() = super<A>.greeting
                        set(value) {
                            super<B>.greeting = value
                        }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                void sideEffect(String x) {}

                abstract class Base {
                  abstract String greeting;
                }

                abstract class A implements Base {
                  @override
                  String get greeting {
                    return 'I am A';
                  }

                  @override
                  void set greeting(String value) {
                    return sideEffect('I am A');
                  }
                }

                abstract class B implements Base {
                  @override
                  String get greeting {
                    return 'I am B';
                  }

                  @override
                  void set greeting(String value) {
                    return sideEffect('I am B');
                  }
                }

                @sealed
                class Test implements A, B {
                  @override
                  String get greeting {
                    return this._A${'$'}greeting;
                  }

                  @override
                  void set greeting(String value) {
                    this._B${'$'}greeting = value;
                  }

                  String get _A${'$'}greeting {
                    return 'I am A';
                  }

                  void set _B${'$'}greeting(String value) {
                    return sideEffect('I am B');
                  }
                }
                """
            )
        }
    }

    @Test
    fun `class with single method`() = assertCompile {
        kotlin(
            """
            class Test {
                fun doIt() {}
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              @nonVirtual
              void doIt() {}
            }
            """
        )
    }

    @Test
    fun `class with inheritance, primary constructor parameters, properties, (complex) default values and more`() =
        assertCompile {
            kotlin(
                """
                abstract class Vector(u: Int?) {
                    abstract val x: Int
                    abstract val y: Int
                    abstract val z: Int
                }
                
                class VectorImpl(
                    override val x: Int = 0,
                    override val y: Int = complexValue(0),
                    override val z: Int = 0,
                    length: Int? = complexValue(0),
                    r: Int? = z,
                    o: Int = y * 2,
                    v: Int = 3,
                ) : Vector(length) {
                    val sum = complexValue(r)
                    val sumPower = sum * 2 * o
                    val sumPowerTwo = x * sum * 2 + complexValue(r)
                
                    fun doSomething(): Int {
                        return 3
                    }
                }
                
                
                fun complexValue(x: Int?): Int {
                    return 343
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Vector {
                  Vector(int? u) : super();
                  abstract final int x;
                  abstract final int y;
                  abstract final int z;
                }

                @sealed
                class VectorImpl extends Vector {
                  VectorImpl._${'$'}(
                    this.y,
                    int? length,
                    int? r,
                    int o, {
                    this.x = 0,
                    this.z = 0,
                    int v = 3,
                  })  : sum = complexValue(r),
                        super(length) {
                    this.sumPower = this.sum * 2 * o;
                    this.sumPowerTwo = this.x * this.sum * 2 + complexValue(r);
                  }
                  factory VectorImpl({
                    int x = 0,
                    int? y = null,
                    int z = 0,
                    dynamic length = const _$DefaultValue(),
                    dynamic r = const _$DefaultValue(),
                    int? o = null,
                    int v = 3,
                  }) {
                    y = y == null ? complexValue(0) : y;
                    length =
                        length == const _$DefaultValue() ? complexValue(0) : length as int?;
                    r = r == const _$DefaultValue() ? z : r as int?;
                    o = o == null ? y * 2 : o;
                    return VectorImpl._${'$'}(y, length, r, o, x: x, z: z, v: v);
                  }
                  @override
                  final int x;
                  @override
                  final int y;
                  @override
                  final int z;
                  @nonVirtual
                  final int sum;
                  @nonVirtual
                  late final int sumPower;
                  @nonVirtual
                  late final int sumPowerTwo;
                  @nonVirtual
                  int doSomething() {
                    return 3;
                  }
                }

                int complexValue(int? x) {
                  return 343;
                }

                @sealed
                class _$DefaultValue {
                  const _$DefaultValue();
                  @nonVirtual
                  dynamic noSuchMethod(Invocation invocation) {}
                }
                """
            )
        }

    @Nested
    inner class Operators {
        @Test
        fun `class with plus operator`() = assertCompile {
            kotlin(
                """
                class Test(val n: Int = 0) {
                    operator fun plus(other: Test): Test {
                        return Test(n + other.n)
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  Test({this.n = 0}) : super();
                  @nonVirtual
                  final int n;
                  @nonVirtual
                  Test plus(Test other) {
                    return Test(n: this.n + other.n);
                  }

                  @nonVirtual
                  Test operator +(Test other) => this.plus(other);
                }
                """
            )
        }

        @Test
        fun `class with minus operator`() = assertCompile {
            kotlin(
                """
                class Test(val n: Int = 0) {
                    operator fun minus(other: Test): Test {
                        return Test(n - other.n)
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  Test({this.n = 0}) : super();
                  @nonVirtual
                  final int n;
                  @nonVirtual
                  Test minus(Test other) {
                    return Test(n: this.n - other.n);
                  }

                  @nonVirtual
                  Test operator -(Test other) => this.minus(other);
                }
                """
            )
        }

        @Test
        fun `class with times operator`() = assertCompile {
            kotlin(
                """
                class Test(val n: Int = 0) {
                    operator fun times(other: Test): Test {
                        return Test(n * other.n)
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  Test({this.n = 0}) : super();
                  @nonVirtual
                  final int n;
                  @nonVirtual
                  Test times(Test other) {
                    return Test(n: this.n * other.n);
                  }

                  @nonVirtual
                  Test operator *(Test other) => this.times(other);
                }
                """
            )
        }

        @Test
        fun `class with div operator`() = assertCompile {
            kotlin(
                """
                class Test(val n: Double = 0.0) {
                    operator fun div(other: Test): Test {
                        return Test(n / other.n)
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  Test({this.n = 0.0}) : super();
                  @nonVirtual
                  final double n;
                  @nonVirtual
                  Test div(Test other) {
                    return Test(n: this.n / other.n);
                  }

                  @nonVirtual
                  Test operator /(Test other) => this.div(other);
                }
                """
            )
        }

        @Test
        fun `class with rangeTo operator`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun rangeTo(other: Test): Test {
                        return Test()
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  Test rangeTo(Test other) {
                    return Test();
                  }
                }
                """
            )
        }

        @Test
        fun `class with in operator`() = assertCompile {
            kotlin(
                """
                class Test(val n: Int = 0) {
                    operator fun contains(other: Test): Boolean {
                        return false
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  Test({this.n = 0}) : super();
                  @nonVirtual
                  final int n;
                  @nonVirtual
                  bool contains(Test other) {
                    return false;
                  }
                }
                """
            )
        }

        @Test
        fun `class with get operator`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun get(index: Int): Int {
                        return index
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  int get(int index) {
                    return index;
                  }

                  @nonVirtual
                  int operator [](int index) => this.get(index);
                }
                """
            )
        }

        @Test
        fun `class with get operator with multiple parameters`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun get(index: Int, index2: Int): Int {
                        return index + index2
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  int get(
                    int index,
                    int index2,
                  ) {
                    return index + index2;
                  }
                }
                """
            )
        }

        @Test
        fun `class with set operator`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun set(index: Int, value: Boolean) {}
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  void set(
                    int index,
                    bool value,
                  ) {}
                  @nonVirtual
                  void operator []=(
                    int index,
                    bool value,
                  ) =>
                      this.set(index, value);
                }
                """
            )
        }

        @Test
        fun `class with set operator with multiple parameters`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun set(index: Int, index2: Int, value: Boolean) {}
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  void set(
                    int index,
                    int index2,
                    bool value,
                  ) {}
                }
                """
            )
        }

        @Test
        fun `class with invoke operator`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun invoke() {}
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  void call() {}
                }
                """
            )
        }

        @Test
        fun `class with invoke operator with two parameters and return type`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun invoke(a: Int, b: Int = 0): Int {
                        return 343
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  int call(
                    int a, {
                    int b = 0,
                  }) {
                    return 343;
                  }
                }
                """
            )
        }

        @Test
        fun `class with overridden equals operator`() = assertCompile {
            kotlin(
                """
                class Test {
                    override operator fun equals(other: Any?): Boolean {
                        return false
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  bool equals(Object? other) {
                    return false;
                  }

                  @override
                  bool operator ==(Object? other) => this.equals(other);
                }
                """
            )
        }

        @Test
        fun `class with overridden equals operator while implementing interface`() = assertCompile {
            kotlin(
                """
                interface Marker

                class Test : Marker {
                    override fun equals(other: Any?): Boolean {
                        return false
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Marker {}

                @sealed
                class Test implements Marker {
                  bool equals(Object? other) {
                    return false;
                  }

                  @override
                  bool operator ==(Object? other) => this.equals(other);
                }
                """
            )
        }

        @Test
        fun `class with compareTo operator`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun compareTo(other: Test): Int {
                        return 1
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  int compareTo(Test other) {
                    return 1;
                  }

                  @nonVirtual
                  bool operator <(Test other) => this.compareTo(other) < 0;
                  @nonVirtual
                  bool operator >(Test other) => this.compareTo(other) > 0;
                  @nonVirtual
                  bool operator <=(Test other) => this.compareTo(other) <= 0;
                  @nonVirtual
                  bool operator >=(Test other) => this.compareTo(other) >= 0;
                }
                """
            )
        }

        @Test
        fun `call compareTo operator`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun compareTo(other: Test): Int {
                        return 1
                    }
                }

                fun main() {
                    Test() >= Test()
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  int compareTo(Test other) {
                    return 1;
                  }

                  @nonVirtual
                  bool operator <(Test other) => this.compareTo(other) < 0;
                  @nonVirtual
                  bool operator >(Test other) => this.compareTo(other) > 0;
                  @nonVirtual
                  bool operator <=(Test other) => this.compareTo(other) <= 0;
                  @nonVirtual
                  bool operator >=(Test other) => this.compareTo(other) >= 0;
                }
                
                void main() {
                  Test() >= Test();
                }
                """
            )
        }

        @Test
        fun `class with compareTo operator which is overridden`() = assertCompile {
            kotlin(
                """
                abstract class A {
                    abstract operator fun compareTo(other: A): Int
                }

                class B : A() {
                    override operator fun compareTo(other: A): Int {
                        return 1
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class A {
                  int compareTo(A other);
                  bool operator <(A other) => this.compareTo(other) < 0;
                  bool operator >(A other) => this.compareTo(other) > 0;
                  bool operator <=(A other) => this.compareTo(other) <= 0;
                  bool operator >=(A other) => this.compareTo(other) >= 0;
                }

                @sealed
                class B extends A {
                  @override
                  int compareTo(A other) {
                    return 1;
                  }
                }
                """
            )
        }

        @Test
        fun `class with overloaded increment operator`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun inc(other: Test): Test {
                        return Test()
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  Test inc(Test other) {
                    return Test();
                  }
                }
                """
            )
        }

        @Test
        fun `class with decrement operator`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun dec(other: Test): Test {
                        return Test()
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  Test dec(Test other) {
                    return Test();
                  }
                }
                """
            )
        }

        @Test
        fun `class with unaryPlus operator`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun unaryPlus(): Test {
                        return Test()
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  Test unaryPlus() {
                    return Test();
                  }
                }
                """
            )
        }

        @Test
        fun `class with unaryMinus operator`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun unaryMinus(): Test {
                        return Test()
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  Test unaryMinus() {
                    return Test();
                  }
                }
                """
            )
        }

        @Test
        fun `class with not operator`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun not(): Test {
                        return Test()
                    }
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  Test not() {
                    return Test();
                  }
                }
                """
            )
        }
    }

    @Test
    fun `class with private primary constructor`() = assertCompile {
        kotlin(
            """
            class Test private constructor()
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test._() : super();
            }
            """
        )
    }

    @Test
    fun `class with init block`() = assertCompile {
        kotlin(
            """
            class Test {
                init {
                    3 + 3
                }
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test() : super() {
                3 + 3;
              }
            }
            """
        )
    }

    @Test
    fun `class with two init blocks`() = assertCompile {
        kotlin(
            """
            class Test {
                init {
                    3 + 3
                }

                init {
                    6 + 6
                }
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test() : super() {
                3 + 3;
                6 + 6;
              }
            }
            """
        )
    }

    @Test
    fun `class with init block and constructor parameter`() = assertCompile {
        kotlin(
            """
            class Test(param: Int) {
                init {
                    param + 3
                }
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test(int param) : super() {
                param + 3;
              }
            }
            """
        )
    }

    @Test
    fun `class with init block and secondary constructor`() = assertCompile {
        kotlin(
            """
            class Test(param: Int) {
                init {
                    param + 3
                }

                constructor() : this(54)
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test(int param) : super() {
                param + 3;
              }
              Test.${'$'}constructor${'$'}1() : this(54);
            }
            """
        )
    }

    @Test
    fun `class secondary constructor invoking other secondary constructor`() = assertCompile {
        kotlin(
            """
            class Test(x: Int, y: Int, z: Int) {
                @DartName("twoDimensional")
                constructor(x: Int, y: Int) : this(x, y, -1)

                @DartName("oneDimensional")
                constructor(x: Int) : this(x, -1)
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test(
                int x,
                int y,
                int z,
              ) : super();
              Test.twoDimensional(
                int x,
                int y,
              ) : this(x, y, -1);
              Test.oneDimensional(int x) : this.twoDimensional(x, -1);
            }
            """
        )
    }

    @Test
    fun `class secondary constructor invoking super secondary constructor`() = assertCompile {
        kotlin(
            """
            open class Test(x: Int, y: Int, z: Int) {
                @DartName("twoDimensional")
                constructor(x: Int, y: Int) : this(x, y, -1)

                @DartName("oneDimensional")
                constructor(x: Int) : this(x, -1)
            }

            class SubTest : Test(0)
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            class Test {
              Test(
                int x,
                int y,
                int z,
              ) : super();
              Test.twoDimensional(
                int x,
                int y,
              ) : this(x, y, -1);
              Test.oneDimensional(int x) : this.twoDimensional(x, -1);
            }

            @sealed
            class SubTest extends Test {
              SubTest() : super.oneDimensional(0);
            }
            """
        )
    }

    @Test
    fun `nested class`() = assertCompile {
        kotlin(
            """
            class Tree {
                class Branch
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Tree {}

            @sealed
            class Tree${'$'}Branch {}
            """
        )
    }

    @Test
    fun `double nested class`() = assertCompile {
        kotlin(
            """
            class Tree {
                class Branch {
                    class Leaf
                }
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Tree {}

            @sealed
            class Tree${'$'}Branch${'$'}Leaf {}

            @sealed
            class Tree${'$'}Branch {}
            """
        )
    }

    @Test
    fun `calling constructor of double nested class`() = assertCompile {
        kotlin(
            """
            class Tree {
                class Branch {
                    class Leaf
                }
            }

            fun main() {
                Tree.Branch.Leaf()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Tree {}

            void main() {
              Tree${'$'}Branch${'$'}Leaf();
            }

            @sealed
            class Tree${'$'}Branch${'$'}Leaf {}

            @sealed
            class Tree${'$'}Branch {}
            """
        )
    }

    @Test
    fun `class with type parameter`() = assertCompile {
        kotlin("class Test<T>")

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test<T> {}
            """
        )
    }

    @Test
    fun `class with Unit type argument`() = assertCompile {
        kotlin(
            """
            open class Base<T>

            class Test : Base<Unit>()
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            class Base<T> {}

            @sealed
            class Test extends Base<void> {
              Test() : super();
            }
            """
        )
    }

    @Test
    fun `class with type parameter with method`() = assertCompile {
        kotlin(
            """
            class Test<T> {
                fun method(other: T) {}
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test<T> {
              @nonVirtual
              void method(T other) {}
            }
            """
        )
    }

    @Test
    fun `class with type parameter with method with nullable value parameter with type parameter type`() =
        assertCompile {
            kotlin(
                """
                class Test<T> {
                    fun method(other: T?) {}
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test<T> {
                  @nonVirtual
                  void method(T? other) {}
                }
                """
            )
        }

    @Test
    fun `class with two type parameters`() = assertCompile {
        kotlin("class Test<T0, T1>")

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test<T0, T1> {}
            """
        )
    }

    @Test
    fun `class with type parameter bound`() = assertCompile {
        kotlin("class Test<T : Int>")

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test<T extends int> {}
            """
        )
    }

    @Test
    fun `class with multiple type parameter bounds`() = assertCompile {
        kotlin(
            """
            interface Buildable {
                fun build()
            }

            interface Identifiable {
                fun identify()
            }

            class Builder<T> where T : Buildable, T : Identifiable {
                fun startBuild(item: T) {
                    item.identify()
                    item.build()

                    identifyAndExec(item)
                }

                private fun identifyAndExec(id: Identifiable) {}
            }

            class SomeItem : Buildable, Identifiable {
                override fun build() {}

                override fun identify() {}
            }

            fun main() {
                Builder<SomeItem>().startBuild(SomeItem())
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            abstract class Buildable {
              void build();
            }

            abstract class Identifiable {
              void identify();
            }

            @sealed
            class Builder<T extends Object> {
              @nonVirtual
              void startBuild(T item) {
                (item as Identifiable).identify();
                (item as Buildable).build();
                this._identifyAndExec(item as Identifiable);
              }

              @nonVirtual
              void _identifyAndExec(Identifiable id) {}
            }

            @sealed
            class SomeItem implements Buildable, Identifiable {
              @override
              void build() {}
              @override
              void identify() {}
            }

            void main() {
              Builder<SomeItem>().startBuild(SomeItem());
            }
            """
        )
    }

    @Test
    fun `class with multiple type parameter bounds with common supertype`() = assertCompile {
        kotlin(
            """
            interface Marker

            interface Buildable : Marker {
                fun build()
            }

            interface Identifiable : Marker {
                fun identify()
            }

            class Builder<T> where T : Buildable, T : Identifiable {
                fun startBuild(item: T) {
                    item.identify()
                    item.build()

                    identifyAndExec(item)
                }

                private fun identifyAndExec(id: Identifiable) {}
            }

            class SomeItem : Buildable, Identifiable {
                override fun build() {}

                override fun identify() {}
            }

            fun main() {
                Builder<SomeItem>().startBuild(SomeItem())
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            abstract class Marker {}

            abstract class Buildable implements Marker {
              void build();
            }

            abstract class Identifiable implements Marker {
              void identify();
            }

            @sealed
            class Builder<T extends Marker> {
              @nonVirtual
              void startBuild(T item) {
                (item as Identifiable).identify();
                (item as Buildable).build();
                this._identifyAndExec(item as Identifiable);
              }

              @nonVirtual
              void _identifyAndExec(Identifiable id) {}
            }

            @sealed
            class SomeItem implements Buildable, Identifiable {
              @override
              void build() {}
              @override
              void identify() {}
            }

            void main() {
              Builder<SomeItem>().startBuild(SomeItem());
            }
            """
        )
    }

    @Test
    fun `class with multiple type parameter bounds with common supertype calling method from super type`() =
        assertCompile {
            kotlin(
                """
                interface Marker {
                    fun mark()
                }

                interface Buildable : Marker {
                    fun build()
                }

                interface Identifiable : Marker {
                    fun identify()
                }

                class Builder<T> where T : Buildable, T : Identifiable {
                    fun startBuild(item: T) {
                        item.mark()
                        item.identify()
                        item.build()

                        identifyAndExec(item)
                    }

                    private fun identifyAndExec(id: Identifiable) {}
                }

                class SomeItem : Buildable, Identifiable {
                    override fun mark() {}

                    override fun build() {}

                    override fun identify() {}
                }

                fun main() {
                    Builder<SomeItem>().startBuild(SomeItem())
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Marker {
                  void mark();
                }

                abstract class Buildable implements Marker {
                  void build();
                }

                abstract class Identifiable implements Marker {
                  void identify();
                }

                @sealed
                class Builder<T extends Marker> {
                  @nonVirtual
                  void startBuild(T item) {
                    item.mark();
                    (item as Identifiable).identify();
                    (item as Buildable).build();
                    this._identifyAndExec(item as Identifiable);
                  }

                  @nonVirtual
                  void _identifyAndExec(Identifiable id) {}
                }

                @sealed
                class SomeItem implements Buildable, Identifiable {
                  @override
                  void mark() {}
                  @override
                  void build() {}
                  @override
                  void identify() {}
                }

                void main() {
                  Builder<SomeItem>().startBuild(SomeItem());
                }
                """
            )
    }

    @Test
    fun `class with multiple type parameter bounds one which is nullable`() = assertCompile {
        kotlin(
            """
            interface Buildable {
                fun build()
            }

            interface Identifiable {
                fun identify()
            }

            class Builder<T> where T : Buildable?, T : Identifiable {
                fun startBuild(item: T) {
                    item.identify()
                    item.build()

                    identifyAndExec(item)
                }

                private fun identifyAndExec(id: Identifiable) {}
            }

            class SomeItem : Buildable, Identifiable {
                override fun build() {}

                override fun identify() {}
            }

            fun main() {
                Builder<SomeItem>().startBuild(SomeItem())
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            abstract class Buildable {
              void build();
            }

            abstract class Identifiable {
              void identify();
            }

            @sealed
            class Builder<T extends Object> {
              @nonVirtual
              void startBuild(T item) {
                (item as Identifiable).identify();
                (item as Buildable).build();
                this._identifyAndExec(item as Identifiable);
              }

              @nonVirtual
              void _identifyAndExec(Identifiable id) {}
            }

            @sealed
            class SomeItem implements Buildable, Identifiable {
              @override
              void build() {}
              @override
              void identify() {}
            }

            void main() {
              Builder<SomeItem>().startBuild(SomeItem());
            }
            """
        )
    }

    // Technically this doesn't make much sense, but good to translate it as-is, since the Kotlin compiler doesn't
    // give a warning.
    @Test
    fun `class with multiple type parameter bounds one which is nullable and a null safe operator is used on`() =
        assertCompile {
            kotlin(
                """
                interface Buildable {
                    fun build()
                }

                interface Identifiable {
                    fun identify()
                }

                class Builder<T> where T : Buildable?, T : Identifiable {
                    fun startBuild(item: T) {
                        item.identify()
                        item?.build()

                        identifyAndExec(item)
                    }

                    private fun identifyAndExec(id: Identifiable) {}
                }

                class SomeItem : Buildable, Identifiable {
                    override fun build() {}

                    override fun identify() {}
                }

                fun main() {
                    Builder<SomeItem>().startBuild(SomeItem())
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Buildable {
                  void build();
                }

                abstract class Identifiable {
                  void identify();
                }

                @sealed
                class Builder<T extends Object> {
                  @nonVirtual
                  void startBuild(T item) {
                    (item as Identifiable).identify();
                    (item as Buildable?)?.build();
                    this._identifyAndExec(item as Identifiable);
                  }

                  @nonVirtual
                  void _identifyAndExec(Identifiable id) {}
                }

                @sealed
                class SomeItem implements Buildable, Identifiable {
                  @override
                  void build() {}
                  @override
                  void identify() {}
                }

                void main() {
                  Builder<SomeItem>().startBuild(SomeItem());
                }
                """
            )
        }

    @Test
    fun `class with multiple type parameter bounds with common super type one which is nullable`() = assertCompile {
        kotlin(
            """
            interface Marker

            interface Buildable : Marker {
                fun build()
            }

            interface Identifiable : Marker {
                fun identify()
            }

            class Builder<T> where T : Buildable?, T : Identifiable {
                fun startBuild(item: T) {
                    item.identify()
                    item.build()

                    identifyAndExec(item)
                }

                private fun identifyAndExec(id: Identifiable) {}
            }

            class SomeItem : Buildable, Identifiable {
                override fun build() {}

                override fun identify() {}
            }

            fun main() {
                Builder<SomeItem>().startBuild(SomeItem())
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            abstract class Marker {}

            abstract class Buildable implements Marker {
              void build();
            }

            abstract class Identifiable implements Marker {
              void identify();
            }

            @sealed
            class Builder<T extends Marker> {
              @nonVirtual
              void startBuild(T item) {
                (item as Identifiable).identify();
                (item as Buildable).build();
                this._identifyAndExec(item as Identifiable);
              }

              @nonVirtual
              void _identifyAndExec(Identifiable id) {}
            }

            @sealed
            class SomeItem implements Buildable, Identifiable {
              @override
              void build() {}
              @override
              void identify() {}
            }

            void main() {
              Builder<SomeItem>().startBuild(SomeItem());
            }
            """
        )
    }

    @Test
    fun `class with multiple type parameter bounds both of which are nullable`() = assertCompile {
        kotlin(
            """
            interface Buildable {
                fun build()
            }

            interface Identifiable {
                fun identify()
            }

            class Builder<T> where T : Buildable?, T : Identifiable? {
                fun startBuild(item: T) {
                    item?.identify()
                    item?.build()

                    identifyAndExec(item)
                }

                private fun identifyAndExec(id: Identifiable?) {}
            }

            class SomeItem : Buildable, Identifiable {
                override fun build() {}

                override fun identify() {}
            }

            fun main() {
                Builder<SomeItem?>().startBuild(SomeItem())
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            abstract class Buildable {
              void build();
            }

            abstract class Identifiable {
              void identify();
            }

            @sealed
            class Builder<T> {
              @nonVirtual
              void startBuild(T item) {
                (item as Identifiable?)?.identify();
                (item as Buildable?)?.build();
                this._identifyAndExec(item as Identifiable?);
              }

              @nonVirtual
              void _identifyAndExec(Identifiable? id) {}
            }

            @sealed
            class SomeItem implements Buildable, Identifiable {
              @override
              void build() {}
              @override
              void identify() {}
            }

            void main() {
              Builder<SomeItem?>().startBuild(SomeItem());
            }
            """
        )
    }

    @Test
    fun `class with multiple type parameter bounds with common super type both of which are nullable`() =
        assertCompile {
            kotlin(
                """
                interface Marker

                interface Buildable : Marker {
                    fun build()
                }

                interface Identifiable : Marker {
                    fun identify()
                }

                class Builder<T> where T : Buildable?, T : Identifiable? {
                    fun startBuild(item: T) {
                        item?.identify()
                        item?.build()

                        identifyAndExec(item)
                    }

                    private fun identifyAndExec(id: Identifiable?) {}
                }

                class SomeItem : Buildable, Identifiable {
                    override fun build() {}

                    override fun identify() {}
                }

                fun main() {
                    Builder<SomeItem>().startBuild(SomeItem())
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Marker {}

                abstract class Buildable implements Marker {
                  void build();
                }

                abstract class Identifiable implements Marker {
                  void identify();
                }

                @sealed
                class Builder<T extends Marker?> {
                  @nonVirtual
                  void startBuild(T item) {
                    (item as Identifiable?)?.identify();
                    (item as Buildable?)?.build();
                    this._identifyAndExec(item as Identifiable?);
                  }

                  @nonVirtual
                  void _identifyAndExec(Identifiable? id) {}
                }

                @sealed
                class SomeItem implements Buildable, Identifiable {
                  @override
                  void build() {}
                  @override
                  void identify() {}
                }

                void main() {
                  Builder<SomeItem>().startBuild(SomeItem());
                }
                """
            )
        }

    @Test
    fun `class with multiple type parameter bounds with function call that has parameter of common super type`() =
        assertCompile {
            kotlin(
                """
                interface Buildable {
                    fun build()
                }

                interface Identifiable {
                    fun identify()
                }

                class Builder<T> where T : Buildable, T : Identifiable {
                    fun startBuild(item: T) {
                        item.identify()
                        item.build()

                        identifyAndExec(item)
                    }

                    private fun identifyAndExec(id: Any) {}
                }

                class SomeItem : Buildable, Identifiable {
                    override fun build() {}
                    override fun identify() {}
                }

                fun main() {
                    Builder<SomeItem>().startBuild(SomeItem())
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Buildable {
                  void build();
                }

                abstract class Identifiable {
                  void identify();
                }

                @sealed
                class Builder<T extends Object> {
                  @nonVirtual
                  void startBuild(T item) {
                    (item as Identifiable).identify();
                    (item as Buildable).build();
                    this._identifyAndExec(item);
                  }

                  @nonVirtual
                  void _identifyAndExec(Object id) {}
                }

                @sealed
                class SomeItem implements Buildable, Identifiable {
                  @override
                  void build() {}
                  @override
                  void identify() {}
                }

                void main() {
                  Builder<SomeItem>().startBuild(SomeItem());
                }
                """
            )
    }

    @Test
    fun `class with multiple type parameter bounds with function call that has parameter of Any type`() =
        assertCompile {
            kotlin(
                """
                interface Marker

                interface Buildable : Marker {
                    fun build()
                }

                interface Identifiable : Marker {
                    fun identify()
                }

                class Builder<T> where T : Buildable, T : Identifiable {
                    fun startBuild(item: T) {
                        item.identify()
                        item.build()

                        identifyAndExec(item)
                    }

                    private fun identifyAndExec(id: Any) {}
                }

                class SomeItem : Buildable, Identifiable {
                    override fun build() {}
                    override fun identify() {}
                }

                fun main() {
                    Builder<SomeItem>().startBuild(SomeItem())
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                abstract class Marker {}

                abstract class Buildable implements Marker {
                  void build();
                }

                abstract class Identifiable implements Marker {
                  void identify();
                }

                @sealed
                class Builder<T extends Marker> {
                  @nonVirtual
                  void startBuild(T item) {
                    (item as Identifiable).identify();
                    (item as Buildable).build();
                    this._identifyAndExec(item);
                  }

                  @nonVirtual
                  void _identifyAndExec(Object id) {}
                }

                @sealed
                class SomeItem implements Buildable, Identifiable {
                  @override
                  void build() {}
                  @override
                  void identify() {}
                }

                void main() {
                  Builder<SomeItem>().startBuild(SomeItem());
                }
                """
            )
        }
}