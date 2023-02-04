import 'package:dotlin_generator/src/dart_package.dart';
import 'package:dotlin_generator/src/resolve_package_dependencies.dart';
import 'package:dotlin_generator/src/write_dlibs.dart';
import 'package:package_config/package_config.dart';

PackageConfig get pkgConfig => throw UnsupportedError("asd");

void main(List<String> arguments) async {
  final resolvedPackages = await resolvePackages(_packagesFromArgs(arguments));
  await writeDlibs(resolvedPackages);
}

List<DartPackage> _packagesFromArgs(List<String> arguments) {
  final packages = <DartPackage>[];
  for (var i = 0; i < arguments.length; i += 2) {
    packages.add(
      DartPackage(
        path: arguments[i],
        packagePath: arguments[i + 1],
      ),
    );
  }
  return packages;
}
