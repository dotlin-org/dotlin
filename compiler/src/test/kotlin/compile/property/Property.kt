package compile.property

import DefaultValue
import assertCompile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compile: Property")
class Property {
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
            import "package:meta/meta.dart";

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
            import "package:meta/meta.dart";

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
            import "package:meta/meta.dart";

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
            import "package:meta/meta.dart";

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
            import "package:meta/meta.dart";

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
                this._property = 3;
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
            import "package:meta/meta.dart";

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
            import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
            import "package:meta/meta.dart";

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
            import "package:meta/meta.dart";

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
            import "package:meta/meta.dart";

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
                    import "package:meta/meta.dart";

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
            import "package:meta/meta.dart";

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
            import "package:meta/meta.dart";

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
            import "package:meta/meta.dart";

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
            import "package:meta/meta.dart";

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
            import "package:meta/meta.dart";

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
            import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
            import "package:meta/meta.dart";

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
            import "package:meta/meta.dart";

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

    @Test
    fun `class with property initialized by parameter`() = assertCompile {
        kotlin(
            """
            class Vector(x: Int) {
                val y = x
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

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
    fun `class with property initialized by parameter or if null other value`() = assertCompile {
        kotlin(
            """
            class Vector(x: Int?) {
                val y = x ?: 3
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

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

    @Test
    fun `class with private constructor property`() = assertCompile {
        kotlin(
            """
            class Vector(private val x: Int?)
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            @sealed
            class Vector {
              Vector(this._x) : super();
              @nonVirtual
              final int? _x;
            }
            """
        )
    }

    @Test
    fun `class with private constructor property with default value`() = assertCompile {
        kotlin(
            """
            class Vector(private val x: Int = 3)
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            @sealed
            class Vector {
              Vector({int x = 3})
                  : _x = x,
                    super();
              @nonVirtual
              final int _x;
            }
            """
        )
    }

    @Test
    fun `top-level property with explicit getter`() = assertCompile {
        kotlin(
            """
            val x: Boolean
                get() = false
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            bool get x {
              return false;
            }
            """
        )
    }

    @Test
    fun `top-level lateinit property call isInitialized`() = assertCompile {
        kotlin(
            """
            lateinit var test: Boolean

            fun main() {
                ::test.isInitialized
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/util/lateinit.dt.g.dart"
                show ${'$'}Extensions${'$'}a70fef564787a44;
            import "package:dotlin/src/dotlin/reflect/kproperty_impl.dt.g.dart"
                show KMutableProperty0Impl;
            import "package:meta/meta.dart";

            late bool test;
            void main() {
              test${'$'}kProperty0.isInitialized;
            }

            const KMutableProperty0Impl<bool> test${'$'}kProperty0 =
                KMutableProperty0Impl<bool>("test", _${'$'}381, _${'$'}381${'$'}m38206bd7a26110db);
            bool _${'$'}381() => test;
            bool _${'$'}381${'$'}m38206bd7a26110db(bool ${'$'}value) => test = ${'$'}value;
            """
        )
    }

    @Test
    fun `class lateinit property call isInitialized`() = assertCompile {
        kotlin(
            """
            class Test {
                lateinit var enabled: Boolean
            }

            fun main() {
                val x = Test()
                x::enabled.isInitialized
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/reflect/kproperty_impl.dt.g.dart"
                show KMutableProperty0Impl;
            import "package:dotlin/src/kotlin/util/lateinit.dt.g.dart"
                show ${'$'}Extensions${'$'}a70fef564787a44;
            import "package:meta/meta.dart";

            @sealed
            class Test {
              @nonVirtual
              late bool enabled;
              @nonVirtual
              late final KMutableProperty0Impl<bool> enabled${'$'}kProperty0 =
                  KMutableProperty0Impl<bool>(
                      "enabled", () => enabled, (bool ${'$'}value) => enabled = ${'$'}value);
            }

            void main() {
              final Test x = Test();
              x.enabled${'$'}kProperty0.isInitialized;
            }
            """
        )
    }

    @Test
    fun `class with inline property getter`() = assertCompile {
        kotlin(
            """
            class Test {
                val property
                    inline get() = 34
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            @sealed
            class Test {
              @nonVirtual
              @pragma("vm:always-consider-inlining")
              int get property {
                return 34;
              }
            }
            """
        )
    }

    @Test
    fun `class with inline property getter and setter`() = assertCompile {
        kotlin(
            """
            class Test {
                var property
                    inline get() = 34
                    inline set(value) {}
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            @sealed
            class Test {
              @nonVirtual
              @pragma("vm:always-consider-inlining")
              int get property {
                return 34;
              }

              @nonVirtual
              @pragma("vm:always-consider-inlining")
              void set property(int value) {}
            }
            """
        )
    }

    @Test
    fun `top-level val property`() = assertCompile {
        kotlin(
            """
            val aNumber = 30
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            final int aNumber = 30;
            """
        )
    }
}