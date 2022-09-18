![Dotlin logo](docs/assets/dotlin.png)

Dotlin is a Kotlin to Dart compiler. The aim is to integrate Kotlin as a language
into the Dart ecosystem, combining best of both worlds: The Kotlin language & standard library,
and the Dart ecosystem & build system.

## About Dotlin

Dotlin makes use of Kotlin's IR (Immediate Representation) compiler, and uses that to generate Dart source code.
At the moment some but not all of Kotlin's features are supported, to see what exactly, you can
look at the [TODO](TODO.md).

## Goals

- Support all Kotlin language features
- Support the Kotlin standard library
- Allow the use of any library written in Dart by generating `external` Kotlin APIs
- Generate code that is still readable and pleasant to use for Dart consumers
- Integrate with Dart's build system (e.g. use `pubspec.yaml` to define dependencies)
- Create an IntelliJ/Fleet plugin
- Support Flutter
    - Flutter: Create a fluent widget builder DSL using Kotlin's scope functions on top of the generated `external` API

## Differences from Kotlin

Dotlin is a _dialect_ of Kotlin. Some changes have been made to better integrate into the Dart runtime, and
also to remove some JVM legacy Kotlin contained.

Note that because of these changes, Dotlin code is not compatible with Kotlin/JVM, or other official Kotlin
variants. Dotlin aims to intergrate the Kotlin language (and stdlib) into Dart, not the full Kotlin ecosystem.

### No type erasure

Because of the Dart runtime, there is no type erasure. This means that you will never need to use `reified`
in Dotlin.

For example, the following code that would fail in Kotlin, works in Dotlin:

```kotlin
class MyClass<T>

fun test(arg: Any) {
    if (arg is MyClass<String>) {
        // Do something.
    }
}
```

This would've been reported in Kotlin as:

> Cannot check for instance of erased type: MyClass\<String\>

### Implicit interfaces & Mixins

In Dart, any class can be implemented as an interface. In Kotlin, you either have an interface or a class.

Since you can use any Dart library in Dotlin, there's also the ability to implement any Dart class as an
interface or mixin, just like in Dart. The syntax for that is as follows:

```kotlin
class MyClass : TheirDartClass(Interface), AnotherDartClass(Interface)
```

This will compile to (leaving out irrelevant code for example's sake):

```dart
class MyClass implements TheirDartClass, AnotherDartClass {}
```

Even though `TheirDartClass` is a `class` in Dotlin (not an `interface`) you can implement
these classes as interfaces. When you implement a Dart class like this, it's imlemented
as a pure interface (like in Dart), meaning you have to implement the whole interface yourself.

The same can be done for mixins:

```kotlin
class MyClass : TheirDartMixin(Mixin)
```

```dart
class MyClass with TheirDartMixin {}
```

If you want to _extend_ a Dart class, regular Kotlin syntax can be used.

The implicit interface/mixin syntax is only necessary for Dart libraries that don't have handwritten
Dotlin declarations for them. If there are Dotlin declarations, regular Kotlin `class`/`interface` rules apply.

### Const

Kotlin has a very strict concept of `const`. Only a few primitives can be declared `const`, and only as
top-level or `object` properties. In Dart on the other hand, it's possible to have `const` constructors
for classes and collection literals, and have local `const` variables.

To facilitate this, `const` is also more lenient and Dart-like in Dotlin. This means that the following Dotlin code:

```kotlin
class MyClass const constructor(private val message: String)

const val myFirstClass = MyClass("First")

fun main() {
    const val mySecondClass = MyClass("Second")
}
```

Compiles to:

```dart
class MyClass {
  const MyClass(this._message);
  final String _message;
}

const MyClass myFirstClass = MyClass('First');

void main() {
  const MyClass mySecondClass = MyClass('Second');
}
```

You can use all Dart `const` features in Dotlin.

If you want to explicitly invoke a `const` constructor, you can use the following syntax:
```kotlin
@const MyClass("Something")
```

Note the `@` before `const`. This is because `@const` is an annotation, not a keyword. This is because
the Kotlin compiler does not support keywords in front of expressions at the parser level.

The difference is easy to remember, with any _declaration_ you must use `const`, and with any
_invocation_ you must use `@const`.

Note that `@const` is not necessary when it's implied by e.g. assigning to a `const val`,
similar to Dart.

### Primitives

Kotlin primitives that are not used in Dart and would only complicate code, have been removed. This means that
`Byte`, `Short`, `Long`, `Float`, and `Char` are not present in Dotlin. This is because Dotlin has the following
mapping of primitives:

| Kotlin    | Dart     |
| --------- | -------- |
| `Int`     | `int`    |
| `Double`  | `double` |
| `String`  | `String` |
| `Boolean` | `bool`   |
| `Nothing` | `Never`  |

This means that `Int` now refers to a 64-bit integer, instead of 32-bit as in Kotlin.

### Errors & Exceptions

In Kotlin, you can only throw `Throwable` or its subtypes. In Dotlin, this
restriction is removed. As in Dart, you can throw anything except `null`.

```kotlin
throw "This works!"
```

To integrate better with the Dart runtime, and because Dart has better
[error](https://api.dart.dev/dart-core/Error-class.html)/[exception](https://api.dart.dev/dart-core/Exception-class.html)
defintions, they are used instead of the JVM exceptions. This also means `Throwable` is not available, since it doesn't
server any use anymore.

## Usage

Dotlin, at this point in time, should not be used for any production projects. If you want to try
it out, clone the repo and you can then build it with
```sh
./gradlew build distZip
```
Then you can find Dotlin in `build/distributions/dotlin-<version>.zip`.

In there, there's a `bin/dotlin` executable you can try out.

## Contributing

Since the project is at an early stage, a lot is still changing and therefore — **for now** — code contributions
are not encouraged. However, in the future when Dotlin is in a more stable state this will definitely change.

When code contributions are encouraged, you are required to sign off all of your commits:
```
My commit message

Signed-off-by: Jan Jansen <jan@jansen.dev>
```

By contributing and signing off your commits, you agree to the Developer Certificate of Origin (DCO), which you can
[read here](https://developercertificate.org/).

For now however, it is encouraged to try Dotlin out, and if you notice anything odd, or want to request a feature/improvement, to create
an issue.

## License

Dotlin itself is licensed under the [AGPL](https://www.gnu.org/licenses/agpl-3.0.en.html).

Note that this does not apply to code generated by Dotlin. Code generated by Dotlin can be used in projects of any license.

All libraries used by consumers (e.g. the Kotlin standard library implementation, the Dart core Kotlin definitions) are
licensed under the [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0).

The Dotlin logo (`docs/assets/dotlin.png`) is licensed under [CC BY-NC-ND 4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/).

## Disclaimer

Dotlin is not associated with the Kotlin Foundation.
