import 'dart:convert';
import 'dart:io';

import 'package:dartx/dartx_io.dart';
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
