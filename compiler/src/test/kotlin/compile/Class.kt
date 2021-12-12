/*
 * Copyright 2021 Wilko Manger
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
            class Test {
              Test() : super();
            }
            """
        )
    }

    @Test
    fun `private class`() = assertCompile {
        kotlin("private class Test")

        dart(
            """
            class _Test {
              _Test() : super();
            }
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
                class Test {
                  Test() : super();
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
                class Test {
                  Test() : super();
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
                class Test {
                  Test() : super();
                  int property = 96;
                }

                void main() {
                  Test().property = 123;
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
                class Test {
                  Test({this.property = 96}) : super();
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
                int returnsInt() {
                  return 343;
                }
                
                class Test {
                  Test({int? property = null})
                      : property = property == null ? returnsInt() : property,
                        super();
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
                    int returnsInt() {
                      return 343;
                    }
                    
                    class Test {
                      Test({dynamic property = const _$DefaultValue()})
                          : property = property == const _$DefaultValue()
                                ? returnsInt()
                                : property as int?,
                            super();
                      final int? property;
                    }
                    
                    class _$DefaultValue {
                      const _$DefaultValue();
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
                class Test {
                  Test() : super();
                  final int property1 = 19;
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
                class Test {
                  Test({
                    this.property1 = 19,
                    this.property2 = 96,
                  }) : super();
                  final int property1;
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
                int returnsInt() {
                  return 343;
                }
                
                class Test {
                  Test({
                    int? property1 = null,
                    int? property2 = null,
                  })  : property1 = property1 == null ? returnsInt() : property1,
                        property2 = property2 == null ? returnsInt() : property2,
                        super();
                  final int property1;
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
                    int returnsInt() {
                      return 343;
                    }
                    
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
                      final int? property1;
                      final int? property2;
                    }
                    
                    class _$DefaultValue {
                      const _$DefaultValue();
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
                class Test {
                  Test() : super();
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
                class Test {
                  Test() : super();
                  void sideEffect(int x) {}
                  int get property {
                    return 343;
                  }
    
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
                class Test {
                  Test() : super();
                  void sideEffect(int x) {}
                  int _${'$'}property = 0;
                  int get property {
                    this.sideEffect(this._${'$'}property);
                    return this._${'$'}property;
                  }
        
                  void set property(int value) {
                    this.sideEffect(value);
                    this._${'$'}property = value;
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
                class Test {
                  Test(int param) : super() {
                    this.property = param;
                  }
                  late final int property;
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
                class Test {
                  Test(
                    int x,
                    int y,
                    int z,
                  ) : super() {
                    this.sum = x + y + z;
                    this.sumTimesSum = this.sum * this.sum;
                  }
                  late final int sum;
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
                class Test {
                  Test(
                    this.x,
                    this.y,
                    this.z,
                  ) : super() {
                    this.sum = this.x + this.y + this.z;
                    this.sumTimesSum = this.sum * this.sum;
                  }
                  final int x;
                  final int y;
                  final int z;
                  late final int sum;
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
                    class Test {
                      Test(int x) : super() {
                        this.xTimesX = this.x * x;
                      }
                      final int x = 34;
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
                int returnsInt() {
                  return 343;
                }
                
                class Test {
                  Test() : super();
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
                class Test {
                  Test() : super() {
                    this.x = this.returnsInt();
                  }
                  late final int x;
                  int returnsInt() {
                    return 343;
                  }
                }
                """
            )
        }
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
                class A {
                  A() : super();
                }
                
                class B extends A {
                  B() : super();
                }
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
                class A {
                  A(int x) : super();
                }
                
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
                class A {
                  A(int x) : super();
                }
                
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
                    class A {
                      A(int x) : super();
                    }
                
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
                    int returnsInt() {
                      return 343;
                    }
                    
                    class A {
                      A(int x) : super();
                    }
                    
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
                    class Vector {
                      Vector() : super();
                    }
                    
                    Vector returnsVector() {
                      return Vector();
                    }
                    
                    class A {
                      A(Vector x) : super();
                    }
                    
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
                    class Vector {
                      Vector() : super();
                    }
                    
                    Vector returnsVector() {
                      return Vector();
                    }
                    
                    class A {
                      A(Vector? x) : super();
                    }
                    
                    class B extends A {
                      B._${'$'}(Vector? y) : super(y);
                      factory B({Vector? y = const _${'$'}DefaultVectorValue()}) {
                        y = y == const _${'$'}DefaultVectorValue() ? returnsVector() : y;
                        return B._${'$'}(y);
                      }
                    }
                    
                    class _${'$'}DefaultVectorValue implements Vector {
                      const _${'$'}DefaultVectorValue();
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
                class A {
                  A() : super();
                  final int property = 0;
                }
                
                class B extends A {
                  B() : super();
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
                class A {
                  A() : super();
                  final int property = 0;
                }
                
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
                    class A {
                      A({this.property = 0}) : super();
                      final int property;
                    }
                    
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
                abstract class Marker {}
    
                class Test implements Marker {
                  Test() : super();
                }
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
                abstract class Marker {}
                
                abstract class Marker2 {}
    
                class Test implements Marker, Marker2 {
                  Test() : super();
                }
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
                abstract class Test {
                  void method(
                    int x, {
                    int y = 0,
                    int? z = null,
                  });
                }
                
                class TestImpl implements Test {
                  TestImpl() : super();
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
                
                class TestImpl implements Test {
                  TestImpl() : super();
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
                
                class _$DefaultValue {
                  const _$DefaultValue();
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
                abstract class Test {
                  int method(
                    int x, {
                    int y = 0,
                    int? z = null,
                  }) {
                    return 343;
                  }
                }
                
                class TestImpl implements Test {
                  TestImpl() : super();
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
                    
                    class TestImpl implements Test {
                      TestImpl() : super();
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
                    
                    class _$DefaultValue {
                      const _$DefaultValue();
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
                abstract class Test {
                  abstract final int property;
                }
    
                class TestImpl implements Test {
                  TestImpl() : super();
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
                abstract class Test {
                  int get property {
                    return 343;
                  }
                }
    
                class TestImpl implements Test {
                  TestImpl() : super();
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
                void sideEffect(int x) {}
    
                abstract class Test {
                  int get property {
                    return 343;
                  }
    
                  void set property(int value) {
                    sideEffect(value);
                  }
                }
    
                class TestImpl implements Test {
                  TestImpl() : super();
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
            class Test {
              Test() : super();
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
                abstract class Vector {
                  Vector(int? u) : super();
                  abstract final int x;
                  abstract final int y;
                  abstract final int z;
                }
                
                class VectorImpl extends Vector {
                  VectorImpl._${'$'}(
                    this.y,
                    int? length,
                    int? r,
                    int o, {
                    this.x = 0,
                    this.z = 0,
                    int v = 3,
                  }) : super(length) {
                    this.sum = complexValue(r);
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
                  late final int sum;
                  late final int sumPower;
                  late final int sumPowerTwo;
                  int doSomething() {
                    return 3;
                  }
                }
                
                int complexValue(int? x) {
                  return 343;
                }
                
                class _$DefaultValue {
                  const _$DefaultValue();
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
                class Test {
                  Test({this.n = 0}) : super();
                  final int n;
                  Test plus(Test other) {
                    return Test(n: this.n + other.n);
                  }

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
                class Test {
                  Test({this.n = 0}) : super();
                  final int n;
                  Test minus(Test other) {
                    return Test(n: this.n - other.n);
                  }

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
                class Test {
                  Test({this.n = 0}) : super();
                  final int n;
                  Test times(Test other) {
                    return Test(n: this.n * other.n);
                  }

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
                class Test {
                  Test({this.n = 0.0}) : super();
                  final double n;
                  Test div(Test other) {
                    return Test(n: this.n / other.n);
                  }

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
                class Test {
                  Test() : super();
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
                class Test {
                  Test({this.n = 0}) : super();
                  final int n;
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
                class Test {
                  Test() : super();
                  int get(int index) {
                    return index;
                  }

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
                class Test {
                  Test() : super();
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
                class Test {
                  Test() : super();
                  void set(
                    int index,
                    bool value,
                  ) {}
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
                class Test {
                  Test() : super();
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
                class Test {
                  Test() : super();
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
                class Test {
                  Test() : super();
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
                class Test {
                  Test() : super();
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
                class Test {
                  Test() : super();
                  int compareTo(Test other) {
                    return 1;
                  }

                  bool operator <(Test other) => this.compareTo(other) < 0;
                  bool operator >(Test other) => this.compareTo(other) > 0;
                  bool operator <=(Test other) => this.compareTo(other) <= 0;
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
                class Test {
                  Test() : super();
                  int compareTo(Test other) {
                    return 1;
                  }

                  bool operator <(Test other) => this.compareTo(other) < 0;
                  bool operator >(Test other) => this.compareTo(other) > 0;
                  bool operator <=(Test other) => this.compareTo(other) <= 0;
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
                abstract class A {
                  A() : super();
                  int compareTo(A other);
                  bool operator <(A other) => this.compareTo(other) < 0;
                  bool operator >(A other) => this.compareTo(other) > 0;
                  bool operator <=(A other) => this.compareTo(other) <= 0;
                  bool operator >=(A other) => this.compareTo(other) >= 0;
                }

                class B extends A {
                  B() : super();
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
                class Test {
                  Test() : super();
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
                class Test {
                  Test() : super();
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
                class Test {
                  Test() : super();
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
                class Test {
                  Test() : super();
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
                class Test {
                  Test() : super();
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
            class Tree {
              Tree() : super();
            }

            class Tree${'$'}Branch {
              Tree${'$'}Branch() : super();
            }
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
            class Tree {
              Tree() : super();
            }

            class Tree${'$'}Branch${'$'}Leaf {
              Tree${'$'}Branch${'$'}Leaf() : super();
            }

            class Tree${'$'}Branch {
              Tree${'$'}Branch() : super();
            }
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
            class Tree {
              Tree() : super();
            }

            void main() {
              Tree${'$'}Branch${'$'}Leaf();
            }

            class Tree${'$'}Branch${'$'}Leaf {
              Tree${'$'}Branch${'$'}Leaf() : super();
            }

            class Tree${'$'}Branch {
              Tree${'$'}Branch() : super();
            }
            """
        )
    }

    @Test
    fun `class with type parameter`() = assertCompile {
        kotlin("class Test<T>")

        dart(
            """
            class Test<T> {
              Test() : super();
            }
            """
        )
    }

    @Test
    fun `class with two type parameters`() = assertCompile {
        kotlin("class Test<T0, T1>")

        dart(
            """
            class Test<T0, T1> {
              Test() : super();
            }
            """
        )
    }

    @Test
    fun `class with type parameter bound`() = assertCompile {
        kotlin("class Test<T : Int>")

        dart(
            """
            class Test<T extends int> {
              Test() : super();
            }
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
                override fun build() { }

                override fun identify() {}
            }

            fun main() {
                Builder<SomeItem>().startBuild(SomeItem())
            }
            """
        )

        dart(
            """
            abstract class Buildable {
              void build();
            }

            abstract class Identifiable {
              void identify();
            }

            class Builder<T extends Object> {
              Builder() : super();
              void startBuild(T item) {
                (item as Identifiable).identify();
                (item as Buildable).build();
                this._identifyAndExec(item as Identifiable);
              }

              void _identifyAndExec(Identifiable id) {}
            }

            class SomeItem implements Buildable, Identifiable {
              SomeItem() : super();
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
                override fun build() { }

                override fun identify() {}
            }

            fun main() {
                Builder<SomeItem>().startBuild(SomeItem())
            }
            """
        )

        dart(
            """
            abstract class Marker {}

            abstract class Buildable implements Marker {
              void build();
            }

            abstract class Identifiable implements Marker {
              void identify();
            }

            class Builder<T extends Marker> {
              Builder() : super();
              void startBuild(T item) {
                (item as Identifiable).identify();
                (item as Buildable).build();
                this._identifyAndExec(item as Identifiable);
              }

              void _identifyAndExec(Identifiable id) {}
            }

            class SomeItem implements Buildable, Identifiable {
              SomeItem() : super();
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
                override fun build() { }

                override fun identify() {}
            }

            fun main() {
                Builder<SomeItem>().startBuild(SomeItem())
            }
            """
        )

        dart(
            """
            abstract class Buildable {
              void build();
            }

            abstract class Identifiable {
              void identify();
            }

            class Builder<T extends Object> {
              Builder() : super();
              void startBuild(T item) {
                (item as Identifiable).identify();
                (item as Buildable).build();
                this._identifyAndExec(item as Identifiable);
              }

              void _identifyAndExec(Identifiable id) {}
            }

            class SomeItem implements Buildable, Identifiable {
              SomeItem() : super();
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
                    override fun build() { }

                    override fun identify() {}
                }

                fun main() {
                    Builder<SomeItem>().startBuild(SomeItem())
                }
                """
            )

            dart(
                """
                abstract class Buildable {
                  void build();
                }

                abstract class Identifiable {
                  void identify();
                }

                class Builder<T extends Object> {
                  Builder() : super();
                  void startBuild(T item) {
                    (item as Identifiable).identify();
                    (item as Buildable?)?.build();
                    this._identifyAndExec(item as Identifiable);
                  }

                  void _identifyAndExec(Identifiable id) {}
                }

                class SomeItem implements Buildable, Identifiable {
                  SomeItem() : super();
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
                override fun build() { }

                override fun identify() {}
            }

            fun main() {
                Builder<SomeItem>().startBuild(SomeItem())
            }
            """
        )

        dart(
            """
            abstract class Marker {}

            abstract class Buildable implements Marker {
              void build();
            }

            abstract class Identifiable implements Marker {
              void identify();
            }

            class Builder<T extends Marker> {
              Builder() : super();
              void startBuild(T item) {
                (item as Identifiable).identify();
                (item as Buildable).build();
                this._identifyAndExec(item as Identifiable);
              }

              void _identifyAndExec(Identifiable id) {}
            }

            class SomeItem implements Buildable, Identifiable {
              SomeItem() : super();
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
                override fun build() { }

                override fun identify() {}
            }

            fun main() {
                Builder<SomeItem?>().startBuild(SomeItem())
            }
            """
        )

        dart(
            """
            abstract class Buildable {
              void build();
            }

            abstract class Identifiable {
              void identify();
            }

            class Builder<T> {
              Builder() : super();
              void startBuild(T? item) {
                (item as Identifiable?)?.identify();
                (item as Buildable?)?.build();
                this._identifyAndExec(item as Identifiable?);
              }

              void _identifyAndExec(Identifiable? id) {}
            }

            class SomeItem implements Buildable, Identifiable {
              SomeItem() : super();
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
                    override fun build() { }

                    override fun identify() {}
                }

                fun main() {
                    Builder<SomeItem>().startBuild(SomeItem())
                }
                """
            )

            dart(
                """
                abstract class Marker {}

                abstract class Buildable implements Marker {
                  void build();
                }

                abstract class Identifiable implements Marker {
                  void identify();
                }

                class Builder<T extends Marker?> {
                  Builder() : super();
                  void startBuild(T? item) {
                    (item as Identifiable?)?.identify();
                    (item as Buildable?)?.build();
                    this._identifyAndExec(item as Identifiable?);
                  }

                  void _identifyAndExec(Identifiable? id) {}
                }

                class SomeItem implements Buildable, Identifiable {
                  SomeItem() : super();
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