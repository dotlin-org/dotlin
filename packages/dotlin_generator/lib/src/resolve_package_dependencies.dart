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

import 'package:analyzer/dart/analysis/analysis_context_collection.dart';
import 'package:analyzer/dart/analysis/results.dart';
import 'package:analyzer/dart/element/element.dart';
import 'package:dartx/dartx_io.dart';
import 'package:dotlin_generator/src/dart_package.dart';

import 'package:path/path.dart' as path;

typedef ResolvedPackages = Map<DartPackage, List<LibraryElement>>;

Future<ResolvedPackages> resolvePackages(
  List<DartPackage> packages,
) async {
  return Map.fromEntries(
    await Future.wait(
      packages.map((package) async {
        // TODO: Use package.path instead of packagePath (in multiple places)
        // when we want to serialize files outside of lib/. Only necessary
        // for the current project being compiled, not dependencies.
        final contextCollections = AnalysisContextCollection(
          includedPaths: [package.packagePath],
        );

        final dartFiles = await Directory(package.packagePath)
            .list(recursive: true)
            .where(
              (f) =>
                  // Dart files compiled by Dotlin are already available
                  // through klibs.
                  path.extension(f.path, 3) != '.dt.g.dart' &&
                  path.extension(f.path) == '.dart',
            )
            .toList();

        final context = contextCollections.contexts.first;

        return MapEntry(
          package,
          await Future.wait(
            dartFiles
                .map(
              (f) => context.currentSession.getResolvedLibrary(
                path.absolute(f.path),
              ),
            )
                .map(
              (futureResult) async {
                final result = await futureResult;
                return result is ResolvedLibraryResult ? result.element : null;
              },
            ),
          ).then(
            (elements) => elements.filterNotNull().toList(growable: false),
          ),
        );
      }),
    ),
  );
}
