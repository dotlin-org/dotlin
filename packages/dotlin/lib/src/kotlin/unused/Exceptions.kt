/*
 * Copyright 2010-2020 JetBrains s.r.o.
 * Copyright 2021-2022 Wilko Manger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress(
    "EXTENSION_PROPERTY_MUST_HAVE_ACCESSORS_OR_BE_ABSTRACT",
    "SEALED_INHERITOR_IN_DIFFERENT_PACKAGE"
)

package kotlin

private open class Error() : Throwable()
private open class Exception() : Throwable()
private open class RuntimeException() : Exception()

private open  class IllegalArgumentException() : RuntimeException()
private open class IllegalStateException() : RuntimeException()

private class IndexOutOfBoundsException() : RuntimeException()

private open class ConcurrentModificationException() : RuntimeException()
private open class UnsupportedOperationException() : RuntimeException()
private open class NumberFormatException() : IllegalArgumentException()
private open class NullPointerException() : RuntimeException()
private open class ClassCastException() : RuntimeException()
private open class AssertionError() : Error()
private open class NoSuchElementException() : RuntimeException()

@SinceKotlin("1.3")
private open class ArithmeticException() : RuntimeException()