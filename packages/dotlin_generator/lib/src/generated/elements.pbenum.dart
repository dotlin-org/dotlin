///
//  Generated code. Do not modify.
//  source: elements.proto
//
// @dart = 2.12
// ignore_for_file: annotate_overrides,camel_case_types,constant_identifier_names,directives_ordering,library_prefixes,non_constant_identifier_names,prefer_final_fields,return_of_invalid_type,unnecessary_const,unnecessary_import,unnecessary_this,unused_import,unused_shown_name

// ignore_for_file: UNDEFINED_SHOWN_NAME
import 'dart:core' as $core;
import 'package:protobuf/protobuf.dart' as $pb;

class DartNullabilitySuffix extends $pb.ProtobufEnum {
  static const DartNullabilitySuffix QUESTION_MARK = DartNullabilitySuffix._(0, const $core.bool.fromEnvironment('protobuf.omit_enum_names') ? '' : 'QUESTION_MARK');
  static const DartNullabilitySuffix STAR = DartNullabilitySuffix._(1, const $core.bool.fromEnvironment('protobuf.omit_enum_names') ? '' : 'STAR');
  static const DartNullabilitySuffix NONE = DartNullabilitySuffix._(2, const $core.bool.fromEnvironment('protobuf.omit_enum_names') ? '' : 'NONE');

  static const $core.List<DartNullabilitySuffix> values = <DartNullabilitySuffix> [
    QUESTION_MARK,
    STAR,
    NONE,
  ];

  static final $core.Map<$core.int, DartNullabilitySuffix> _byValue = $pb.ProtobufEnum.initByValue(values);
  static DartNullabilitySuffix? valueOf($core.int value) => _byValue[value];

  const DartNullabilitySuffix._($core.int v, $core.String n) : super(v, n);
}

