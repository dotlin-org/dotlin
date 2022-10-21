/*
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

package dart.typeddata

/**
 * A typed view of a sequence of bytes.
 */
external interface TypedData {
    /**
     * Returns the number of bytes in the representation of each element in this
     * array.
     */
    val elementSizeInBytes: Int

    /**
     * Returns the offset in bytes into the underlying byte buffer of this view.
     */
    val offsetInBytes: Int

    /**
     * Returns the length of this view, in bytes.
     */
    val lengthInBytes: Int

    // TODO: buffer
}