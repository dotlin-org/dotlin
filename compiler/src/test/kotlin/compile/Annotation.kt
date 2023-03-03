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

@DisplayName("Compile: Annotation")
class Annotation : BaseTest {
    @Test
    fun `annotation`() = assertCompile {
        kotlin(
            """
            annotation class Test
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @sealed
            class Test implements Annotation {
              const Test() : super();
            }
            """
        )
    }

    @Test
    fun `use annotation on class`() = assertCompile {
        kotlin(
            """
            annotation class Sensitive

            @Sensitive
            class Bird
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @sealed
            class Sensitive implements Annotation {
              const Sensitive() : super();
            }

            @Sensitive()
            @sealed
            class Bird {}
            """
        )
    }

    @Test
    fun `use annotation  with parameters on class`() = assertCompile {
        kotlin(
            """
            annotation class Sensitive(val x: Int, val y: Int)

            @Sensitive(0, 1)
            class Bird
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show nonVirtual, sealed;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @sealed
            class Sensitive implements Annotation {
              const Sensitive(
                this.x,
                this.y,
              ) : super();
              @nonVirtual
              final int x;
              @nonVirtual
              final int y;
            }

            @Sensitive(0, 1)
            @sealed
            class Bird {}
            """
        )
    }

    @Test
    fun `use annotation on function`() = assertCompile {
        kotlin(
            """
            annotation class Sensitive

            @Sensitive
            fun fly() {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @sealed
            class Sensitive implements Annotation {
              const Sensitive() : super();
            }

            @Sensitive()
            void fly() {}
            """
        )
    }

    @Test
    fun `use annotation with parameters on function`() = assertCompile {
        kotlin(
            """
            annotation class Sensitive(val x: Int, val y: Int)

            @Sensitive(0, 1)
            fun fly() {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show nonVirtual, sealed;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @sealed
            class Sensitive implements Annotation {
              const Sensitive(
                this.x,
                this.y,
              ) : super();
              @nonVirtual
              final int x;
              @nonVirtual
              final int y;
            }

            @Sensitive(0, 1)
            void fly() {}
            """
        )
    }

    @Test
    fun `use annotation on property`() = assertCompile {
        kotlin(
            """
            annotation class Sensitive

            @Sensitive
            val isSwallow = true
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @sealed
            class Sensitive implements Annotation {
              const Sensitive() : super();
            }

            @Sensitive()
            final bool isSwallow = true;
            """
        )
    }

    @Test
    fun `use annotation on property with parameters`() = assertCompile {
        kotlin(
            """
            annotation class Sensitive(val x: Int, val y: Int)

            @Sensitive(0, 1)
            val isSwallow = true
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show nonVirtual, sealed;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @sealed
            class Sensitive implements Annotation {
              const Sensitive(
                this.x,
                this.y,
              ) : super();
              @nonVirtual
              final int x;
              @nonVirtual
              final int y;
            }

            @Sensitive(0, 1)
            final bool isSwallow = true;
            """
        )
    }

    @Test
    fun `use annotation on simple property getter`() = assertCompile {
        kotlin(
            """
            annotation class Sensitive

            @get:Sensitive
            val isSwallow = true
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @sealed
            class Sensitive implements Annotation {
              const Sensitive() : super();
            }

            @Sensitive()
            final bool isSwallow = true;
            """
        )
    }

    @Test
    fun `use annotation on simple property setter`() = assertCompile {
        kotlin(
            """
            annotation class Sensitive

            @set:Sensitive
            var isSwallow = true
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @sealed
            class Sensitive implements Annotation {
              const Sensitive() : super();
            }

            @Sensitive()
            bool isSwallow = true;
            """
        )
    }

    @Test
    fun `use annotation on property without backing field getter`() = assertCompile {
        kotlin(
            """
            annotation class Sensitive

            @get:Sensitive
            val isSwallow
                get() = true
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @sealed
            class Sensitive implements Annotation {
              const Sensitive() : super();
            }

            @Sensitive()
            bool get isSwallow {
              return true;
            }
            """
        )
    }

    @Test
    fun `use annotation on property without backing field setter`() = assertCompile {
        kotlin(
            """
            annotation class Sensitive

            @set:Sensitive
            var isSwallow
                get() = true
                set(value) {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @sealed
            class Sensitive implements Annotation {
              const Sensitive() : super();
            }

            bool get isSwallow {
              return true;
            }

            @Sensitive()
            void set isSwallow(bool value) {}
            """
        )
    }

    @Test
    fun `use annotation on property with backing field getter`() = assertCompile {
        kotlin(
            """
            annotation class Sensitive

            fun sideEffect() {}

            @get:Sensitive
            val isSwallow: Boolean = false
                get() {
                    sideEffect()
                    return field
                }
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @sealed
            class Sensitive implements Annotation {
              const Sensitive() : super();
            }

            void sideEffect() {}
            final bool _${'$'}isSwallowBackingField = false;
            @Sensitive()
            bool get isSwallow {
              sideEffect();
              return _${'$'}isSwallowBackingField;
            }
            """
        )
    }

    @Test
    fun `use annotation on property with backing field setter`() = assertCompile {
        kotlin(
            """
            annotation class Sensitive

            fun sideEffect() {}

            @set:Sensitive
            var isSwallow: Boolean = false
                get() {
                    sideEffect()
                    return field
                }
                set(value) {
                    sideEffect()
                    field = value
                }
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @sealed
            class Sensitive implements Annotation {
              const Sensitive() : super();
            }

            void sideEffect() {}
            bool _${'$'}isSwallowBackingField = false;
            bool get isSwallow {
              sideEffect();
              return _${'$'}isSwallowBackingField;
            }

            @Sensitive()
            void set isSwallow(bool value) {
              sideEffect();
              _${'$'}isSwallowBackingField = value;
            }
            """
        )
    }

    @Test
    fun `use annotation on property backing field`() = assertCompile {
        kotlin(
            """
            annotation class Sensitive

            fun sideEffect() {}

            @field:Sensitive
            var isSwallow: Boolean = false
                get() {
                    sideEffect()
                    return field
                }
                set(value) {
                    sideEffect()
                    field = value
                }
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @sealed
            class Sensitive implements Annotation {
              const Sensitive() : super();
            }

            void sideEffect() {}
            @Sensitive()
            bool _${'$'}isSwallowBackingField = false;
            bool get isSwallow {
              sideEffect();
              return _${'$'}isSwallowBackingField;
            }

            void set isSwallow(bool value) {
              sideEffect();
              _${'$'}isSwallowBackingField = value;
            }
            """
        )
    }

    @Test
    fun `use annotation on method`() = assertCompile {
        kotlin(
            """
            annotation class Sensitive

            class Bird {
                @Sensitive
                fun fly() {}
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed, nonVirtual;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @sealed
            class Sensitive implements Annotation {
              const Sensitive() : super();
            }

            @sealed
            class Bird {
              @Sensitive()
              @nonVirtual
              void fly() {}
            }
            """
        )
    }

    @Test
    fun `use annotation with SOURCE retention`() = assertCompile {
        kotlin(
            """
            @Retention(AnnotationRetention.SOURCE)
            annotation class Sensitive

            class Bird {
                @Sensitive
                fun fly() {}
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/annotation/annotations.dt.g.dart"
                show AnnotationRetention, Retention;
            import "package:meta/meta.dart" show sealed, nonVirtual;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @Retention(value: AnnotationRetention.SOURCE)
            @sealed
            class Sensitive implements Annotation {
              const Sensitive() : super();
            }

            @sealed
            class Bird {
              @nonVirtual
              void fly() {}
            }
            """
        )
    }

    @Test
    fun `use annotation with RUNTIME retention`() = assertCompile {
        kotlin(
            """
            @Retention(AnnotationRetention.RUNTIME)
            annotation class Sensitive

            class Bird {
                @Sensitive
                fun fly() {}
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/annotation/annotations.dt.g.dart"
                show AnnotationRetention, Retention;
            import "package:meta/meta.dart" show sealed, nonVirtual;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @Retention(value: AnnotationRetention.RUNTIME)
            @sealed
            class Sensitive implements Annotation {
              const Sensitive() : super();
            }

            @sealed
            class Bird {
              @Sensitive()
              @nonVirtual
              void fly() {}
            }
            """
        )
    }

    @Test
    fun `use annotation from different file`() = assertCompile {
        kotlin(
            """
            @Retention(AnnotationRetention.RUNTIME)
            annotation class Sensitive
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/annotation/annotations.dt.g.dart"
                show AnnotationRetention, Retention;
            import "package:meta/meta.dart" show sealed;
            import "package:dotlin/src/kotlin/native/annotation.dt.g.dart" show Annotation;

            @Retention(value: AnnotationRetention.RUNTIME)
            @sealed
            class Sensitive implements Annotation {
              const Sensitive() : super();
            }
            """
        )

        kotlin(
            """
            class Bird {
                @Sensitive
                fun fly() {}
            }
            """
        )

        dart(
            """
            import "0.dt.g.dart" show Sensitive;
            import "package:meta/meta.dart" show nonVirtual, sealed;

            @sealed
            class Bird {
              @Sensitive()
              @nonVirtual
              void fly() {}
            }
            """
        )
    }
}