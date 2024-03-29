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
            import "package:meta/meta.dart" show sealed;

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
            import "package:meta/meta.dart" show sealed;

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
            import "package:meta/meta.dart" show internal, sealed;

            @internal
            @sealed
            class Test {}
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
                import "package:meta/meta.dart" show sealed;

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
                import "package:meta/meta.dart" show sealed;

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
                import "package:meta/meta.dart" show sealed;

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
                    import "package:meta/meta.dart" show sealed;

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
                    import "package:meta/meta.dart" show sealed;

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
                    import "package:meta/meta.dart" show sealed;

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
                    import "package:meta/meta.dart" show sealed, nonVirtual;

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
                import "package:meta/meta.dart" show sealed;

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
                import "package:meta/meta.dart" show sealed;

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
                    import "package:meta/meta.dart" show sealed;

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
                import "package:meta/meta.dart" show sealed;

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
                import "package:meta/meta.dart" show sealed;

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
                import "package:meta/meta.dart" show sealed;

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
                import "package:meta/meta.dart" show sealed, nonVirtual;

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
                import "package:meta/meta.dart" show sealed;

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
                    import "package:meta/meta.dart" show sealed, nonVirtual;

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
                import "package:meta/meta.dart" show sealed;

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
                import "package:meta/meta.dart" show sealed;

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
                import "package:meta/meta.dart" show sealed;

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
                import "package:meta/meta.dart" show sealed;

                abstract class Base {
                  String sayHello();
                }

                abstract class A implements Base {
                  @override
                  String sayHello() {
                    return "I am A";
                  }
                }

                abstract class B implements Base {
                  @override
                  String sayHello() {
                    return "I am B";
                  }
                }

                @sealed
                class Test implements A, B {
                  @override
                  String sayHello() {
                    return this._B${'$'}sayHello();
                  }

                  String _B${'$'}sayHello() {
                    return "I am B";
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
                import "package:meta/meta.dart" show sealed;

                abstract class Base {
                  abstract final String greeting;
                }

                abstract class A implements Base {
                  @override
                  String get greeting {
                    return "I am A";
                  }
                }

                abstract class B implements Base {
                  @override
                  String get greeting {
                    return "I am B";
                  }
                }

                @sealed
                class Test implements A, B {
                  @override
                  String get greeting {
                    return this._B${'$'}greeting;
                  }

                  String get _B${'$'}greeting {
                    return "I am B";
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
                import "package:meta/meta.dart" show sealed;

                void sideEffect(String x) {}

                abstract class Base {
                  abstract String greeting;
                }

                abstract class A implements Base {
                  @override
                  String get greeting {
                    return "I am A";
                  }

                  @override
                  void set greeting(String value) {
                    return sideEffect("I am A");
                  }
                }

                abstract class B implements Base {
                  @override
                  String get greeting {
                    return "I am B";
                  }

                  @override
                  void set greeting(String value) {
                    return sideEffect("I am B");
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
                    return "I am A";
                  }

                  void set _B${'$'}greeting(String value) {
                    return sideEffect("I am B");
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
            import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                    return VectorImpl._${'$'}(x: x, y, z: z, length, r, o, v: v);
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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                    operator fun set(index: Int, value: Boolean): Boolean = value
                }
                """
            )

            dart(
                """
                import "package:meta/meta.dart" show nonVirtual, sealed;

                @sealed
                class Test {
                  @nonVirtual
                  bool set(
                    int index,
                    bool value,
                  ) {
                    return value;
                  }

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show sealed;

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
                import "package:meta/meta.dart" show sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
            import "package:meta/meta.dart" show sealed;

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
            import "package:meta/meta.dart" show sealed;

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
            import "package:meta/meta.dart" show sealed;

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
            import "package:meta/meta.dart" show sealed;

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
            import "package:meta/meta.dart" show sealed;

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
            import "package:meta/meta.dart" show sealed;

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
            import "package:meta/meta.dart" show sealed;

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
    fun `class with redirecting constructor that has a body`() = assertCompile {
        kotlin(
            """
            class Test(val property: Int?) {
                constructor() : this(null) {
                    initialize()
                }

                private fun initialize() {}
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show nonVirtual, sealed;

            @sealed
            class Test {
              Test(this.property) : super();
              @nonVirtual
              final int? property;
              factory Test.${'$'}constructor${'$'}1() {
                final Test tmp0_instance = Test(null);
                tmp0_instance._initialize();
                return tmp0_instance;
              }
              @nonVirtual
              void _initialize() {}
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
            import "package:meta/meta.dart" show sealed;

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
            import "package:meta/meta.dart" show sealed;

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
            import "package:meta/meta.dart" show sealed;

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
            import "package:meta/meta.dart" show sealed;

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
            import "package:meta/meta.dart" show sealed;

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
            import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
            import "package:meta/meta.dart" show sealed;

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
            import "package:meta/meta.dart" show sealed;

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
            import "package:meta/meta.dart" show nonVirtual, sealed;

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
            import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
            import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
            import "package:meta/meta.dart" show nonVirtual, sealed;

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
            import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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
                import "package:meta/meta.dart" show nonVirtual, sealed;

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

    @Test
    fun `class with built-in Dart identifier in name`() = assertCompile {
        kotlin("class covariant")
        dart(
            """
            import "package:meta/meta.dart" show sealed;

            @sealed
            class ${'$'}covariant {}
            """
        )
    }

    @Test
    fun `class with reserved Dart word in name`() = assertCompile {
        kotlin("class final")
        dart(
            """
            import "package:meta/meta.dart" show sealed;

            @sealed
            class ${'$'}final {}
            """
        )
    }

    @Test
    fun `class with reserved Dart and Kotlin word in name`() = assertCompile {
        kotlin("class `class`")
        dart(
            """
            import "package:meta/meta.dart" show sealed;

            @sealed
            class ${'$'}class {}
            """
        )
    }

    @Test
    fun `class with method with default value that's overridden`() = assertCompile {
        kotlin(
            """
            interface Base {
                fun exec(arg: Int = 30)
            }

            class Impl : Base {
                override fun exec(arg: Int) {}
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;

            abstract class Base {
              void exec({int arg = 30});
            }

            @sealed
            class Impl implements Base {
              @override
              void exec({int arg = 30}) {}
            }
            """
        )
    }

    @Test
    fun `data class`() = assertCompile {
        kotlin(
            """
            data class Vector3(val x: Double, val y: Double, val z: Double)
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show nonVirtual, sealed;

            @sealed
            class Vector3 {
              Vector3(
                this.x,
                this.y,
                this.z,
              ) : super();
              @nonVirtual
              final double x;
              @nonVirtual
              final double y;
              @nonVirtual
              final double z;
              @nonVirtual
              double component1() {
                return this.x;
              }

              @nonVirtual
              double component2() {
                return this.y;
              }

              @nonVirtual
              double component3() {
                return this.z;
              }

              @nonVirtual
              Vector3 copy({
                double? x = null,
                double? y = null,
                double? z = null,
              }) {
                x = x == null ? this.x : x;
                y = y == null ? this.y : y;
                z = z == null ? this.z : z;
                return Vector3(x, y, z);
              }

              @override
              String toString() {
                return "Vector3(x=${'$'}{this.x}, y=${'$'}{this.y}, z=${'$'}{this.z})";
              }

              @override
              int get hashCode {
                int result = this.x.hashCode;
                result = result * 31 + this.y.hashCode;
                result = result * 31 + this.z.hashCode;
                return result;
              }

              bool equals(Object? other) {
                if (identical(this, other)) {
                  return true;
                }
                if (other is! Vector3) {
                  return false;
                }
                final Vector3 tmp0_other_with_cast = other as Vector3;
                if (this.x != tmp0_other_with_cast.x) {
                  return false;
                }
                if (this.y != tmp0_other_with_cast.y) {
                  return false;
                }
                if (this.z != tmp0_other_with_cast.z) {
                  return false;
                }
                return true;
              }

              @override
              bool operator ==(Object? other) => this.equals(other);
            }
            """
        )
    }

    @Test
    fun `data class with Int property`() = assertCompile {
        kotlin(
            """
            data class Something(val value: Int)
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show nonVirtual, sealed;

            @sealed
            class Something {
              Something(this.value) : super();
              @nonVirtual
              final int value;
              @nonVirtual
              int component1() {
                return this.value;
              }

              @nonVirtual
              Something copy({int? value = null}) {
                value = value == null ? this.value : value;
                return Something(value);
              }

              @override
              String toString() {
                return "Something(value=${'$'}{this.value})";
              }

              @override
              int get hashCode {
                return this.value.hashCode;
              }

              bool equals(Object? other) {
                if (identical(this, other)) {
                  return true;
                }
                if (other is! Something) {
                  return false;
                }
                final Something tmp0_other_with_cast = other as Something;
                if (this.value != tmp0_other_with_cast.value) {
                  return false;
                }
                return true;
              }

              @override
              bool operator ==(Object? other) => this.equals(other);
            }
            """
        )
    }

    @Test
    fun `data class overriding hashCode calling Int div`() = assertCompile {
        kotlin(
            """
            data class Vector3(val x: Double, val y: Double, val z: Double) {
                override fun hashCode(): Int = 3 / 5
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show nonVirtual, sealed;

            @sealed
            class Vector3 {
              Vector3(
                this.x,
                this.y,
                this.z,
              ) : super();
              @nonVirtual
              final double x;
              @nonVirtual
              final double y;
              @nonVirtual
              final double z;
              @override
              int get hashCode {
                return 3 ~/ 5;
              }

              @nonVirtual
              double component1() {
                return this.x;
              }

              @nonVirtual
              double component2() {
                return this.y;
              }

              @nonVirtual
              double component3() {
                return this.z;
              }

              @nonVirtual
              Vector3 copy({
                double? x = null,
                double? y = null,
                double? z = null,
              }) {
                x = x == null ? this.x : x;
                y = y == null ? this.y : y;
                z = z == null ? this.z : z;
                return Vector3(x, y, z);
              }

              @override
              String toString() {
                return "Vector3(x=${'$'}{this.x}, y=${'$'}{this.y}, z=${'$'}{this.z})";
              }

              bool equals(Object? other) {
                if (identical(this, other)) {
                  return true;
                }
                if (other is! Vector3) {
                  return false;
                }
                final Vector3 tmp0_other_with_cast = other as Vector3;
                if (this.x != tmp0_other_with_cast.x) {
                  return false;
                }
                if (this.y != tmp0_other_with_cast.y) {
                  return false;
                }
                if (this.z != tmp0_other_with_cast.z) {
                  return false;
                }
                return true;
              }

              @override
              bool operator ==(Object? other) => this.equals(other);
            }
            """
        )
    }
}