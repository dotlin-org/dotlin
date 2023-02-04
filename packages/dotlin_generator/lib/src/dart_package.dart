import 'package:path/path.dart' as p;

class DartPackage {
  final String path;

  /// Most of the time `lib/`.
  final String packagePath;

  DartPackage({required this.path, required this.packagePath});

  late final String dotlinPath = p.join(path, '.dotlin');

  late final String dlibPath = p.join(dotlinPath, 'dlib');
}