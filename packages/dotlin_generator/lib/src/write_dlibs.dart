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

import 'dart:io';

import 'package:dotlin_generator/src/serialize/type_serializer.dart';

import 'generated/elements.pb.dart';
import 'serialize/element_serializer.dart';

import 'resolve_package_dependencies.dart';

Future<void> writeDlibs(ResolvedPackages resolvedPackages) async {
  await Future.wait(
    resolvedPackages.entries.map((entry) async {
      final package = entry.key;
      final libraries = entry.value;

      await Directory(package.dotlinPath).create(recursive: true);

      final dlib = File(package.dlibPath);

      final serializer = DartElementSerializer(
        package.packagePath,
        DartTypeSerializer(),
      );

      final packageElement = DartPackageElement(
        libraries: libraries
            .map(
              (lib) => serializer.serializeLibrary(lib),
            )
            .toList(growable: false),
      );

      await dlib.writeAsBytes(packageElement.writeToBuffer());
    }),
  );
}
