![Dotlin logo](docs/assets/dotlin.png)

Dotlin is a Kotlin to Dart compiler. The aim is to integrate Kotlin as a language
into the Dart ecosystem, combining best of both worlds: The Kotlin language & standard library,
and the Dart ecosystem & build system.

## About Dotlin

Dotlin makes use of Kotlin's IR (Immediate Representation) compiler, and uses that to generate Dart source code.
At the moment not all of Kotlin's features are supported; to see what exactly is or isn't implemented, you can
look at the [TODO](TODO.md) list.

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
also to remove some JVM-centric legacy traits.

Note that because of these changes, Dotlin code is _not_ compatible with Kotlin/JVM, or other official Kotlin
variants. Dotlin aims to intergrate the Kotlin language (and stdlib) into Dart, not the full Kotlin ecosystem.

### No type erasure

Because of the Dart runtime, there is no type erasure. This means that you will never need to use `reified`
in Dotlin.

For example, the following code, which would fail in Kotlin, works in Dotlin:

```kotlin
class MyClass<T>

fun test(arg: Any) {
    if (arg is MyClass<String>) {
        // Do something.
    }
}
```

This would've been reported in Kotlin as:

> ⚠️ Cannot check for instance of erased type: MyClass\<String\>

### Implicit interfaces & Mixins

In Dart, any class can be implemented as an interface. In Kotlin, you either have an interface or a class.

Since you can use any Dart library in Dotlin, you can also implement any Dart class as an
interface or mixin, just like in Dart. The syntax for that is as follows:

```kotlin
class MyClass : TheirDartClass(Interface), AnotherDartClass(Interface)
```

This will compile to (leaving out irrelevant code for example's sake):

```dart
class MyClass implements TheirDartClass, AnotherDartClass {}
```

Even though `TheirDartClass` is a `class` in Dotlin (not an `interface`) you can implement
it as an interface. When you implement a Dart class like this, it's implemented
as a pure interface (like in Dart), meaning you have to implement the whole interface yourself.

The same can be done for mixins:

```kotlin
class MyClass : TheirDartClass(Mixin)
```

```dart
class MyClass with TheirDartClass {}
```

This only works if `TheirDartClass` can be used as a mixin, meaing it either is declared with the `mixin` keyword, or
has no constructors and extends `Object` (`Any`). If a Dart class is not a valid mixin, the
special mixin inheritance syntax is not available.

If you want to _extend_ a Dart class, regular Kotlin syntax can be used.

The implicit interface/mixin syntax is only necessary for Dart libraries that don't have handwritten
Dotlin declarations for them. If there are Dotlin declarations, regular Kotlin `class`/`interface` rules apply.

### Const

Kotlin has a very strict concept of `const`. Only a few primitives can be declared `const`, and only as
top-level values or properties on `object`s. In Dart, on the other hand, it's possible to have `const` constructors
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

Note the `@` before `const`. This is because `@const` is an annotation, not a keyword.
The Kotlin compiler does not support keywords in front of expressions at the parser level.

The difference is easy to remember: with any _declaration_ you must use `const`, and with any
_invocation_ you must use `@const`.

Note that as in Dart, `@const` is not necessary when it's implied, e.g. by assigning to a `const val`.

### Lateinit

In Kotlin, `lateinit` is not applicable to properties with types that are primitive or nullable/have a nullable upper bound. In Dotlin, this is possible.

For example, the following code, which would fail in Kotlin, works in Dotlin:

```kotlin
class Example<T> {
    lateinit var myNullableVar: String?

    lateinit var myPrimitiveVar: Int

    lateinit var myGenericVar: T
}
```

Respectively, these declarations would've been reported in Kotlin with the following errors:

> ⚠️ 'lateinit' modifier is not allowed on properties of nullable types

> ⚠️ 'lateinit' modifier is not allowed on properties of primitive types

> ⚠️ 'lateinit' modifier is not allowed on properties of a type with nullable upper bound

But with Dotlin, this compiles to:

```dart
class Example<T> {
  late String? myNullableVar;

  late int myPrimitiveVar;

  late T myGenericVar;
}
```

#### Lateinit `isInitialized` outside class

In Kotlin, `lateinit var`s cannot be checked whether they're initialized from outside the containing class. For example, the following code:
```kotlin
class Example {
    lateinit var lateVar: String
}

fun main() {
    if (Example()::lateVar.isInitialized) {
        // Do something.
    }
}
```

The call would've been reported as:

> ⚠️ Backing field of 'var lateVar: String' is not accessible at this point

However, in Dotlin, this compiles with no issues.

### Primitives

Kotlin primitives that are not used in Dart and would only complicate code have been removed, meaning that
`Byte`, `Short`, `Long`, `Float`, and `Char` are not present. This is because Dotlin has the following
mapping of built-ins:

| Dart     | Kotlin    |
| -------- | --------- |
| `int`    | `Int`     |
| `double` | `Double`  |
| `String` | `String`  |
| `bool`   | `Boolean` |
| `Object` | `Any`     |
| `Never`  | `Nothing` |

This also means that `Int` now refers to a 64-bit integer, instead of 32-bit as in Kotlin.

### Iterator

In Kotlin, any class that implements `hasNext()` and `next()` is considered an iterator. In Dotlin,
this is not the case. Instead, it's more like Dart: A class is only an considered an iterator if it
implements `dart.core.Iterator`. This means that the Dart `Iterator` API is used: instead of
`hasNext()` and `next()`, `moveNext()` and `current` are used.


`kotlin.collections.Iterator` is not available. However, the `kotlin.collections.Iterator`
subtypes are, changed to fit `dart.core.Iterator`: `MutableIterator`, `BidirectionalIterator`,
`ListIterator`, and `MutableListIterator`.

### Errors & Exceptions

In Kotlin, you can only throw `Throwable` or its subtypes. In Dotlin, this
restriction is removed. As in Dart, you can throw anything except `null`.

```kotlin
throw "This works!"
```

To integrate better with the Dart runtime, and because Dart has better
[error](https://api.dart.dev/dart-core/Error-class.html)/[exception](https://api.dart.dev/dart-core/Exception-class.html)
defintions, they are used instead of the JVM exception classes. This also means `Throwable` is not available, since it doesn't
serve any use anymore.

## Differences from Dart

Aside from the obvious differences between the Kotlin language and stdlib, Dotlin adds
some Dart-specific enhancements. Also some other additions, because of differences between the Dart and Kotlin languages.

### Const lambdas

In Dart, you cannot pass lambda literals (function expressions) as
arguments to const constructors, only references
to top-level or static named functions.

In Dart, the following code:

```dart
class Hobbit {
  const Hobbit(this._computeName);
  final String Function() _computeName;
}

void main() {
  const bilbo = Hobbit(() => "Bilbo Baggins");
}
```

Would throw the following error, because of the lambda literal argument:

> ⚠️ Arguments of a constant creation must be constant expressions.

Even though if you passed a reference of a _named_ top-level/static function
with the exact same body, it would work.

Dotlin does this for you, so the following code compiles:

```kotlin
class Hobbit const constructor(private val computeName: () -> String)

fun main() {
    const val bilbo = Hobbit { "Bilbo Baggins" }
}
```

And results in:
```dart
class Hobbit {
  const Hobbit(this._computeName);
  final String Function() _computeName;
}

void main() {
  const Hobbit bilbo = Hobbit(_$11f4);
}

String _$11f4() {
  return 'Bilbo Baggins';
}
```

As you can see, a named function is generated based on the lambda, and passed to the
`const` constructor.

This is only possible if the lambda does not capture local or class closure values. You _can_
use top-level/global values, however.

### `const inline`

In Dotlin, you can create `const inline` functions, which can be used similarly to `const` constructors.

These functions must have a single return with a valid `const` expression, and otherwise only contain `const` variables.

An example:

```kotlin
class Hobbit const constructor(name: String, age: Int, isCurrentRingbearer: Boolean)

const inline fun bilboBaggings(): Hobbit {
  const val fullName = "Bilbo Baggings"

  return Hobbit(fullName, age = 111, isCurrentRingbearer = false)
}

fun main() {
  const val bilbo = bilboBaggings()
}
```

The `bilboBaggings()` call is inlined, meaning the called constructor
is still `const`:

```dart
@pragma('vm:always-consider-inlining')
Hobbit bilboBaggings() {
  const String fullName = 'Bilbo Baggings';
  return Hobbit(fullName, 111, false);
}

void main() {
  const Hobbit bilbo = Hobbit('Bilbo Baggings', 111, false);
}
```

You can also use arguments in `const inline` functions:

```kotlin
class Hobbit const constructor(name: String, age: Int, isCurrentRingbearer: Boolean)

const inline fun baggings(firstName: String, age: Int): Hobbit {
  const val fullName = "$firstName Baggings"
  const val hasRing = firstName == "Frodo"

  return Hobbit(fullName, age, isCurrentRingbearer = hasRing)
}

fun main() {
  const val frodo = baggings("Frodo", age = 33)
}
```

Note that if you use arguments in `const` variables, they will be made
non-const. This is because `const inline` functions can still be called
as non-const. However, if called as `const`, arguments are also `const`
inlined:

```dart
@pragma('vm:always-consider-inlining')
Hobbit baggings(String firstName, int age) {
  final String fullName = '${firstName} Baggings';
  final bool hasRing = firstName == 'Frodo';
  return Hobbit(fullName, age, hasRing);
}

void main() {
  const Hobbit frodo = Hobbit('Frodo Baggings', 33, 'Frodo' == 'Frodo');
}
```

### Type literals

Kotlin does not have type literals like Dart does. To accomodate for this, Dotlin
has a `typeOf` function, which compiles to a Dart type literal. For example, the following statement:
```kotlin
val myType = typeOf<String>()
```

Compiles to:

```dart
final myType = String;
```

### Collections

Existing Dart collections have been dissected into different interfaces
based on their mutability, just like in Kotlin.

However, `List` has been split it in more interfaces, to represent all `List`
kinds that exist in Dart runtime using types.

#### `Iterable`

Dotlin's `Iterable` is mapped directly to Dart's `Iterable`. This means that unlike in Kotlin,
`Iterable`s are _lazy_.

The `Iterable` class is significantly larger because Dart's `Iterable` contains a
lot of methods. However, they've been renamed to match Kotlin conventions, some examples:

| Dart        | Kotlin             |
| ----------- | ------------------ |
| `where`     | `filter`           |
| `whereType` | `filterIsInstance` |
| `expand`    | `flatMap`          |
| `every`     | `all`              |
| `skip`      | `drop`             |

#### `Collection`

Represents any type of collection of elements. It provides a common interface for
`List` and `Set, which in Dart don't have a common interface.

> **Note**  
> Runtime type checks work: `List`s and `Set`s are considered `Collection`s at runtime.

##### `MutableCollection`

Represents any kind of mutable collection of elements. "Mutable" specifically means
_growable_ in Dart terms, meaning elements can be added and removed.

> **Note**  
> Runtime type checks work: Dart `List`s and `Set`s are considered `MutableCollection`s,
_only if_ they are actually mutable. Examples (Dart):
>
> ```dart
> [1, 2, 3] is MutableCollection<int> == true
> ```
> ```dart
> List.unmodifiable([1, 2, 3]) is MutableCollection<int> == false
> ```
> <sup>These type checks don't work as Dart code as-is, but are compiled specially when writing a similar expression in Dotlin.</sup>

#### `List`

<sup>Dart: `List`</sup>

A read-only interface that represents any kind of Dart's `List`s. Mutating methods can be
accessed through subtypes.

##### `ImmutableList` <sup>`is List`</sup>

<sup>Dart: `List.unmodifiable`, `const [..]`</sup>

An immutable list. Same interface as `List`, but guaranteed to be immutable.

> **Note**  
> Runtime type checks work: Dart `List`s are considered `ImmutableList`s,
_only if_ they are actually immutable. Examples (Dart):
>
> ```dart
> const [1, 2, 3] is ImmutableList<int> == true
> ```
> ```dart
> List.unmodifiable([1, 2, 3]) is ImmutableList<int> == true
> ```
> ```dart
> [1, 2, 3] is ImmutableList<int> == false
> ```

##### `WriteableList` <sup>`is List`</sup>

<sup>Dart: `List` (`growable: true|false`)</sup>

An interface that supports changing elements (`list[0] = "abc"`), but not adding or removing elements. This
interface represents both `FixedSizeList`s and `MutableList`s, since they are both writeable.

> **Note**  
> Runtime type checks work: Dart `List`s are considered `WriteableList`s,
_only if_ they are actually writeable. Examples (Dart):
>
> ```dart
> [1, 2, 3] is WriteableList<int> == true
> ```
> ```dart
> List.of([1, 2, 3], growable: false) is WriteableList<int> == true
> ```
> ```dart
> List.unmodifiable([1, 2, 3]) is WriteableList<int> == false
> ```

##### `FixedSizeList` (`Array`) <sup>`is WriteableList`</sup>

<sup>Dart: `List` (`growable: false`)</sup>

An interface that represents writeable fixed-length Dart `List`s, also known as _arrays_. Elements can
be changed (`array[0] = "abc"`), but not be added or removed. Any other operation that would change
the size of the list is also not possible.

The difference between this interface and `WriteableList` is that `WriteableList` represents
any list whose elements can be changed, which also includes `MutableList`s.

> **Note**  
> Runtime type checks work: Dart `List`s are considered `FixedSizeList`s,
_only if_ they are actually writeable. Examples (Dart):
>
> ```dart
> List.of([1, 2, 3], growable: false) is FixedSizeList<int> == true
> ```
> ```dart
> [1, 2, 3] is FixedSizeList<int> == false
> ```
> ```dart
> List.unmodifiable([1, 2, 3]) is FixedSizeList<int> == false
> ```

##### `MutableList` <sup>`is WriteableList`</sup>

<sup>Dart: `List` (`growable: true`)</sup>

An interface that represents growable Dart `List`s. Elements can be changed, added and removed.

> **Note**  
> Runtime type checks work: Dart `List`s are considered `MutableList`s,
_only if_ they are actually mutable (writeable & growable). Examples (Dart):
>
> ```dart
> [1, 2, 3] is MutableList<int> == true
> ```
> ```dart
> List.of([1, 2, 3], growable: false) is MutableList<int> == false
> ```
> ```dart
> List.unmodifiable([1, 2, 3]) is MutableList<int> == false
> ```

#### `Set`

<sup>Dart: `Set`</sup>

A read-only interface that represents any kind of Dart's `Set`s. Mutating methods can be
accessed through `MutableSet`.

##### `ImmutablSet` <sup>`is Set`</sup>

<sup>Dart: `Set.unmodifiable`, `const {..}`</sup>

An immutable set. Same interface as `Set`, but guaranteed to be immutable.

> **Note**  
> Runtime type checks work: Dart `Set`s are considered `ImmutableSet`s,
_only if_ they are actually immutable. Examples (Dart):
>
> ```dart
> const {1, 2, 3} is ImmutableSet<int> == true
> ```
> ```dart
> Set.unmodifiable({1, 2, 3}) is ImmutableSet<int> == true
> ```
> ```dart
> {1, 2, 3} is ImmutableSet<int> == false
> ```

##### `MutableSet` <sup>`is Set`</sup>

<sup>Dart: `Set` (`{..}`)</sup>

An interface that represents growable Dart `Set`s. Elements can be changed, added and removed.

> **Note**  
> Runtime type checks work: Dart `Set`s are considered `MutableSet`s,
_only if_ they are actually mutable. Examples (Dart):
>
> ```dart
> {1, 2, 3} is MutableSet<int> == true
> ```
> ```dart
> Set.unmodifiable({1, 2, 3}) is MutableSet<int> == false
> ```

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
are not encouraged. However, in the future when Dotlin is in a more stable state, this will definitely change.

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

Dotlin is not associated with JetBrains or the Kotlin Foundation.
