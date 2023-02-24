/*
 * Copyright 2023 Wilko Manger
 *
 * This file is part of Dotlin Generator.
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

import 'package:analyzer/dart/element/element.dart';
import 'package:protobuf/protobuf.dart';

B polymorphicOf<B extends GeneratedMessage>(
  B Function({required String type, required List<int> value}) constructor,
  GeneratedMessage element,
) {
  return constructor(
    type: 'org.dotlin.compiler.dart.element.${element.runtimeType}',
    value: element.writeToBuffer(),
  );
}

extension EncodedElementLocation on Element {
  String get encodedLocation => location!.encoding;
}

