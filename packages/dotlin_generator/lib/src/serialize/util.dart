import 'package:analyzer/dart/element/element.dart';
import 'package:dotlin_generator/src/generated/elements.pb.dart';
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

