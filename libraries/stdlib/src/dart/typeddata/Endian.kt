/*
 * Copyright 2022 Wilko Manger
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
    "NESTED_CLASS_IN_EXTERNAL_INTERFACE", // TODO: Fix in analyzer
    "WRONG_INITIALIZER_OF_EXTERNAL_DECLARATION", // TODO: Fix in analyzer
    "WRONG_BODY_OF_EXTERNAL_DECLARATION", // TODO: Fix in analyzer
    "NESTED_EXTERNAL_DECLARATION", // TODO: Fix in analyzer
    "CONST_VAL_WITHOUT_INITIALIZER", // TODO: Fix in analyzer
    "TYPE_CANT_BE_USED_FOR_CONST_VAL" // TODO: Fix in analyzer
)

package dart.typeddata

/**
 * Describes endianness to be used when accessing or updating a
 * sequence of bytes.
 */
external interface Endian {
    companion object {
        @DartStatic
        @DartName("big")
        external const val BIG: Endian

        @DartStatic
        @DartName("little")
        external const val LITTLE: Endian

        @DartStatic
        @DartName("host")
        external const val HOST: Endian
    }
}