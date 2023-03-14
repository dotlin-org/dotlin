///
//  Generated code. Do not modify.
//  source: elements.proto
//
// @dart = 2.12
// ignore_for_file: annotate_overrides,camel_case_types,constant_identifier_names,deprecated_member_use_from_same_package,directives_ordering,library_prefixes,non_constant_identifier_names,prefer_final_fields,return_of_invalid_type,unnecessary_const,unnecessary_import,unnecessary_this,unused_import,unused_shown_name

import 'dart:core' as $core;
import 'dart:convert' as $convert;
import 'dart:typed_data' as $typed_data;
@$core.Deprecated('Use dartNullabilitySuffixDescriptor instead')
const DartNullabilitySuffix$json = const {
  '1': 'DartNullabilitySuffix',
  '2': const [
    const {'1': 'QUESTION_MARK', '2': 0},
    const {'1': 'STAR', '2': 1},
    const {'1': 'NONE', '2': 2},
  ],
};

/// Descriptor for `DartNullabilitySuffix`. Decode as a `google.protobuf.EnumDescriptorProto`.
final $typed_data.Uint8List dartNullabilitySuffixDescriptor = $convert.base64Decode('ChVEYXJ0TnVsbGFiaWxpdHlTdWZmaXgSEQoNUVVFU1RJT05fTUFSSxAAEggKBFNUQVIQARIICgROT05FEAI=');
@$core.Deprecated('Use dartPackageElementDescriptor instead')
const DartPackageElement$json = const {
  '1': 'DartPackageElement',
  '2': const [
    const {'1': 'libraries', '3': 1, '4': 3, '5': 11, '6': '.DartLibraryElement', '10': 'libraries'},
  ],
};

/// Descriptor for `DartPackageElement`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartPackageElementDescriptor = $convert.base64Decode('ChJEYXJ0UGFja2FnZUVsZW1lbnQSMQoJbGlicmFyaWVzGAEgAygLMhMuRGFydExpYnJhcnlFbGVtZW50UglsaWJyYXJpZXM=');
@$core.Deprecated('Use dartLibraryElementDescriptor instead')
const DartLibraryElement$json = const {
  '1': 'DartLibraryElement',
  '2': const [
    const {'1': 'location', '3': 1, '4': 2, '5': 9, '10': 'location'},
    const {'1': 'path', '3': 2, '4': 2, '5': 9, '10': 'path'},
    const {'1': 'exports', '3': 3, '4': 3, '5': 11, '6': '.DartLibraryExportElement', '10': 'exports'},
    const {'1': 'units', '3': 4, '4': 3, '5': 11, '6': '.DartCompilationUnitElement', '10': 'units'},
  ],
};

/// Descriptor for `DartLibraryElement`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartLibraryElementDescriptor = $convert.base64Decode('ChJEYXJ0TGlicmFyeUVsZW1lbnQSGgoIbG9jYXRpb24YASACKAlSCGxvY2F0aW9uEhIKBHBhdGgYAiACKAlSBHBhdGgSMwoHZXhwb3J0cxgDIAMoCzIZLkRhcnRMaWJyYXJ5RXhwb3J0RWxlbWVudFIHZXhwb3J0cxIxCgV1bml0cxgEIAMoCzIbLkRhcnRDb21waWxhdGlvblVuaXRFbGVtZW50UgV1bml0cw==');
@$core.Deprecated('Use dartCompilationUnitElementDescriptor instead')
const DartCompilationUnitElement$json = const {
  '1': 'DartCompilationUnitElement',
  '2': const [
    const {'1': 'location', '3': 1, '4': 2, '5': 9, '10': 'location'},
    const {'1': 'properties', '3': 2, '4': 3, '5': 11, '6': '.DartPropertyElement', '10': 'properties'},
    const {'1': 'classes', '3': 3, '4': 3, '5': 11, '6': '.DartClassElement', '10': 'classes'},
    const {'1': 'functions', '3': 4, '4': 3, '5': 11, '6': '.DartFunctionElement', '10': 'functions'},
    const {'1': 'enums', '3': 5, '4': 3, '5': 11, '6': '.DartEnumElement', '10': 'enums'},
  ],
};

/// Descriptor for `DartCompilationUnitElement`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartCompilationUnitElementDescriptor = $convert.base64Decode('ChpEYXJ0Q29tcGlsYXRpb25Vbml0RWxlbWVudBIaCghsb2NhdGlvbhgBIAIoCVIIbG9jYXRpb24SNAoKcHJvcGVydGllcxgCIAMoCzIULkRhcnRQcm9wZXJ0eUVsZW1lbnRSCnByb3BlcnRpZXMSKwoHY2xhc3NlcxgDIAMoCzIRLkRhcnRDbGFzc0VsZW1lbnRSB2NsYXNzZXMSMgoJZnVuY3Rpb25zGAQgAygLMhQuRGFydEZ1bmN0aW9uRWxlbWVudFIJZnVuY3Rpb25zEiYKBWVudW1zGAUgAygLMhAuRGFydEVudW1FbGVtZW50UgVlbnVtcw==');
@$core.Deprecated('Use dartClassElementDescriptor instead')
const DartClassElement$json = const {
  '1': 'DartClassElement',
  '2': const [
    const {'1': 'location', '3': 1, '4': 2, '5': 9, '10': 'location'},
    const {'1': 'name', '3': 2, '4': 2, '5': 9, '10': 'name'},
    const {'1': 'typeParameters', '3': 3, '4': 3, '5': 11, '6': '.DartTypeParameterElement', '10': 'typeParameters'},
    const {'1': 'isAbstract', '3': 4, '4': 2, '5': 8, '10': 'isAbstract'},
    const {'1': 'constructors', '3': 5, '4': 3, '5': 11, '6': '.DartConstructorElement', '10': 'constructors'},
    const {'1': 'properties', '3': 6, '4': 3, '5': 11, '6': '.DartPropertyElement', '10': 'properties'},
    const {'1': 'methods', '3': 7, '4': 3, '5': 11, '6': '.DartFunctionElement', '10': 'methods'},
    const {'1': 'superType', '3': 8, '4': 1, '5': 11, '6': '.DartInterfaceType', '10': 'superType'},
    const {'1': 'superInterfaceTypes', '3': 9, '4': 3, '5': 11, '6': '.DartInterfaceType', '10': 'superInterfaceTypes'},
    const {'1': 'superMixinTypes', '3': 10, '4': 3, '5': 11, '6': '.DartInterfaceType', '10': 'superMixinTypes'},
  ],
};

/// Descriptor for `DartClassElement`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartClassElementDescriptor = $convert.base64Decode('ChBEYXJ0Q2xhc3NFbGVtZW50EhoKCGxvY2F0aW9uGAEgAigJUghsb2NhdGlvbhISCgRuYW1lGAIgAigJUgRuYW1lEkEKDnR5cGVQYXJhbWV0ZXJzGAMgAygLMhkuRGFydFR5cGVQYXJhbWV0ZXJFbGVtZW50Ug50eXBlUGFyYW1ldGVycxIeCgppc0Fic3RyYWN0GAQgAigIUgppc0Fic3RyYWN0EjsKDGNvbnN0cnVjdG9ycxgFIAMoCzIXLkRhcnRDb25zdHJ1Y3RvckVsZW1lbnRSDGNvbnN0cnVjdG9ycxI0Cgpwcm9wZXJ0aWVzGAYgAygLMhQuRGFydFByb3BlcnR5RWxlbWVudFIKcHJvcGVydGllcxIuCgdtZXRob2RzGAcgAygLMhQuRGFydEZ1bmN0aW9uRWxlbWVudFIHbWV0aG9kcxIwCglzdXBlclR5cGUYCCABKAsyEi5EYXJ0SW50ZXJmYWNlVHlwZVIJc3VwZXJUeXBlEkQKE3N1cGVySW50ZXJmYWNlVHlwZXMYCSADKAsyEi5EYXJ0SW50ZXJmYWNlVHlwZVITc3VwZXJJbnRlcmZhY2VUeXBlcxI8Cg9zdXBlck1peGluVHlwZXMYCiADKAsyEi5EYXJ0SW50ZXJmYWNlVHlwZVIPc3VwZXJNaXhpblR5cGVz');
@$core.Deprecated('Use dartEnumElementDescriptor instead')
const DartEnumElement$json = const {
  '1': 'DartEnumElement',
  '2': const [
    const {'1': 'location', '3': 1, '4': 2, '5': 9, '10': 'location'},
    const {'1': 'name', '3': 2, '4': 2, '5': 9, '10': 'name'},
    const {'1': 'typeParameters', '3': 3, '4': 3, '5': 11, '6': '.DartTypeParameterElement', '10': 'typeParameters'},
    const {'1': 'constructors', '3': 4, '4': 3, '5': 11, '6': '.DartConstructorElement', '10': 'constructors'},
    const {'1': 'properties', '3': 5, '4': 3, '5': 11, '6': '.DartPropertyElement', '10': 'properties'},
    const {'1': 'methods', '3': 6, '4': 3, '5': 11, '6': '.DartFunctionElement', '10': 'methods'},
    const {'1': 'superType', '3': 7, '4': 1, '5': 11, '6': '.DartInterfaceType', '10': 'superType'},
    const {'1': 'superInterfaceTypes', '3': 8, '4': 3, '5': 11, '6': '.DartInterfaceType', '10': 'superInterfaceTypes'},
    const {'1': 'superMixinTypes', '3': 9, '4': 3, '5': 11, '6': '.DartInterfaceType', '10': 'superMixinTypes'},
  ],
};

/// Descriptor for `DartEnumElement`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartEnumElementDescriptor = $convert.base64Decode('Cg9EYXJ0RW51bUVsZW1lbnQSGgoIbG9jYXRpb24YASACKAlSCGxvY2F0aW9uEhIKBG5hbWUYAiACKAlSBG5hbWUSQQoOdHlwZVBhcmFtZXRlcnMYAyADKAsyGS5EYXJ0VHlwZVBhcmFtZXRlckVsZW1lbnRSDnR5cGVQYXJhbWV0ZXJzEjsKDGNvbnN0cnVjdG9ycxgEIAMoCzIXLkRhcnRDb25zdHJ1Y3RvckVsZW1lbnRSDGNvbnN0cnVjdG9ycxI0Cgpwcm9wZXJ0aWVzGAUgAygLMhQuRGFydFByb3BlcnR5RWxlbWVudFIKcHJvcGVydGllcxIuCgdtZXRob2RzGAYgAygLMhQuRGFydEZ1bmN0aW9uRWxlbWVudFIHbWV0aG9kcxIwCglzdXBlclR5cGUYByABKAsyEi5EYXJ0SW50ZXJmYWNlVHlwZVIJc3VwZXJUeXBlEkQKE3N1cGVySW50ZXJmYWNlVHlwZXMYCCADKAsyEi5EYXJ0SW50ZXJmYWNlVHlwZVITc3VwZXJJbnRlcmZhY2VUeXBlcxI8Cg9zdXBlck1peGluVHlwZXMYCSADKAsyEi5EYXJ0SW50ZXJmYWNlVHlwZVIPc3VwZXJNaXhpblR5cGVz');
@$core.Deprecated('Use dartPropertyElementDescriptor instead')
const DartPropertyElement$json = const {
  '1': 'DartPropertyElement',
  '2': const [
    const {'1': 'location', '3': 1, '4': 2, '5': 9, '10': 'location'},
    const {'1': 'name', '3': 2, '4': 2, '5': 9, '10': 'name'},
    const {'1': 'isAbstract', '3': 3, '4': 2, '5': 8, '10': 'isAbstract'},
    const {'1': 'isCovariant', '3': 4, '4': 2, '5': 8, '10': 'isCovariant'},
    const {'1': 'isConst', '3': 5, '4': 2, '5': 8, '10': 'isConst'},
    const {'1': 'isFinal', '3': 6, '4': 2, '5': 8, '10': 'isFinal'},
    const {'1': 'isLate', '3': 7, '4': 2, '5': 8, '10': 'isLate'},
    const {'1': 'isStatic', '3': 8, '4': 2, '5': 8, '10': 'isStatic'},
    const {'1': 'isSynthetic', '3': 9, '4': 2, '5': 8, '10': 'isSynthetic'},
    const {'1': 'isEnumConstant', '3': 10, '4': 2, '5': 8, '10': 'isEnumConstant'},
    const {'1': 'type', '3': 11, '4': 2, '5': 11, '6': '.DartType', '10': 'type'},
    const {'1': 'getter', '3': 12, '4': 1, '5': 11, '6': '.DartPropertyAccessorElement', '10': 'getter'},
    const {'1': 'setter', '3': 13, '4': 1, '5': 11, '6': '.DartPropertyAccessorElement', '10': 'setter'},
  ],
};

/// Descriptor for `DartPropertyElement`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartPropertyElementDescriptor = $convert.base64Decode('ChNEYXJ0UHJvcGVydHlFbGVtZW50EhoKCGxvY2F0aW9uGAEgAigJUghsb2NhdGlvbhISCgRuYW1lGAIgAigJUgRuYW1lEh4KCmlzQWJzdHJhY3QYAyACKAhSCmlzQWJzdHJhY3QSIAoLaXNDb3ZhcmlhbnQYBCACKAhSC2lzQ292YXJpYW50EhgKB2lzQ29uc3QYBSACKAhSB2lzQ29uc3QSGAoHaXNGaW5hbBgGIAIoCFIHaXNGaW5hbBIWCgZpc0xhdGUYByACKAhSBmlzTGF0ZRIaCghpc1N0YXRpYxgIIAIoCFIIaXNTdGF0aWMSIAoLaXNTeW50aGV0aWMYCSACKAhSC2lzU3ludGhldGljEiYKDmlzRW51bUNvbnN0YW50GAogAigIUg5pc0VudW1Db25zdGFudBIdCgR0eXBlGAsgAigLMgkuRGFydFR5cGVSBHR5cGUSNAoGZ2V0dGVyGAwgASgLMhwuRGFydFByb3BlcnR5QWNjZXNzb3JFbGVtZW50UgZnZXR0ZXISNAoGc2V0dGVyGA0gASgLMhwuRGFydFByb3BlcnR5QWNjZXNzb3JFbGVtZW50UgZzZXR0ZXI=');
@$core.Deprecated('Use dartPropertyAccessorElementDescriptor instead')
const DartPropertyAccessorElement$json = const {
  '1': 'DartPropertyAccessorElement',
  '2': const [
    const {'1': 'location', '3': 1, '4': 2, '5': 9, '10': 'location'},
    const {'1': 'name', '3': 2, '4': 2, '5': 9, '10': 'name'},
    const {'1': 'type', '3': 3, '4': 2, '5': 11, '6': '.DartFunctionType', '10': 'type'},
    const {'1': 'isAsync', '3': 4, '4': 2, '5': 8, '10': 'isAsync'},
    const {'1': 'isGenerator', '3': 5, '4': 2, '5': 8, '10': 'isGenerator'},
    const {'1': 'isSynthetic', '3': 6, '4': 2, '5': 8, '10': 'isSynthetic'},
    const {'1': 'parameters', '3': 7, '4': 3, '5': 11, '6': '.DartParameterElement', '10': 'parameters'},
    const {'1': 'typeParameters', '3': 8, '4': 3, '5': 11, '6': '.DartTypeParameterElement', '10': 'typeParameters'},
    const {'1': 'correspondingPropertyLocation', '3': 9, '4': 2, '5': 9, '10': 'correspondingPropertyLocation'},
  ],
};

/// Descriptor for `DartPropertyAccessorElement`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartPropertyAccessorElementDescriptor = $convert.base64Decode('ChtEYXJ0UHJvcGVydHlBY2Nlc3NvckVsZW1lbnQSGgoIbG9jYXRpb24YASACKAlSCGxvY2F0aW9uEhIKBG5hbWUYAiACKAlSBG5hbWUSJQoEdHlwZRgDIAIoCzIRLkRhcnRGdW5jdGlvblR5cGVSBHR5cGUSGAoHaXNBc3luYxgEIAIoCFIHaXNBc3luYxIgCgtpc0dlbmVyYXRvchgFIAIoCFILaXNHZW5lcmF0b3ISIAoLaXNTeW50aGV0aWMYBiACKAhSC2lzU3ludGhldGljEjUKCnBhcmFtZXRlcnMYByADKAsyFS5EYXJ0UGFyYW1ldGVyRWxlbWVudFIKcGFyYW1ldGVycxJBCg50eXBlUGFyYW1ldGVycxgIIAMoCzIZLkRhcnRUeXBlUGFyYW1ldGVyRWxlbWVudFIOdHlwZVBhcmFtZXRlcnMSRAodY29ycmVzcG9uZGluZ1Byb3BlcnR5TG9jYXRpb24YCSACKAlSHWNvcnJlc3BvbmRpbmdQcm9wZXJ0eUxvY2F0aW9u');
@$core.Deprecated('Use dartFunctionElementDescriptor instead')
const DartFunctionElement$json = const {
  '1': 'DartFunctionElement',
  '2': const [
    const {'1': 'location', '3': 1, '4': 2, '5': 9, '10': 'location'},
    const {'1': 'name', '3': 2, '4': 2, '5': 9, '10': 'name'},
    const {'1': 'isAsync', '3': 3, '4': 2, '5': 8, '10': 'isAsync'},
    const {'1': 'isGenerator', '3': 4, '4': 2, '5': 8, '10': 'isGenerator'},
    const {'1': 'isAbstract', '3': 5, '4': 2, '5': 8, '10': 'isAbstract'},
    const {'1': 'isStatic', '3': 6, '4': 2, '5': 8, '10': 'isStatic'},
    const {'1': 'isOperator', '3': 7, '4': 2, '5': 8, '10': 'isOperator'},
    const {'1': 'parameters', '3': 8, '4': 3, '5': 11, '6': '.DartParameterElement', '10': 'parameters'},
    const {'1': 'typeParameters', '3': 9, '4': 3, '5': 11, '6': '.DartTypeParameterElement', '10': 'typeParameters'},
    const {'1': 'type', '3': 10, '4': 2, '5': 11, '6': '.DartFunctionType', '10': 'type'},
  ],
};

/// Descriptor for `DartFunctionElement`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartFunctionElementDescriptor = $convert.base64Decode('ChNEYXJ0RnVuY3Rpb25FbGVtZW50EhoKCGxvY2F0aW9uGAEgAigJUghsb2NhdGlvbhISCgRuYW1lGAIgAigJUgRuYW1lEhgKB2lzQXN5bmMYAyACKAhSB2lzQXN5bmMSIAoLaXNHZW5lcmF0b3IYBCACKAhSC2lzR2VuZXJhdG9yEh4KCmlzQWJzdHJhY3QYBSACKAhSCmlzQWJzdHJhY3QSGgoIaXNTdGF0aWMYBiACKAhSCGlzU3RhdGljEh4KCmlzT3BlcmF0b3IYByACKAhSCmlzT3BlcmF0b3ISNQoKcGFyYW1ldGVycxgIIAMoCzIVLkRhcnRQYXJhbWV0ZXJFbGVtZW50UgpwYXJhbWV0ZXJzEkEKDnR5cGVQYXJhbWV0ZXJzGAkgAygLMhkuRGFydFR5cGVQYXJhbWV0ZXJFbGVtZW50Ug50eXBlUGFyYW1ldGVycxIlCgR0eXBlGAogAigLMhEuRGFydEZ1bmN0aW9uVHlwZVIEdHlwZQ==');
@$core.Deprecated('Use dartConstructorElementDescriptor instead')
const DartConstructorElement$json = const {
  '1': 'DartConstructorElement',
  '2': const [
    const {'1': 'location', '3': 1, '4': 2, '5': 9, '10': 'location'},
    const {'1': 'name', '3': 2, '4': 2, '5': 9, '10': 'name'},
    const {'1': 'isConst', '3': 3, '4': 2, '5': 8, '10': 'isConst'},
    const {'1': 'isFactory', '3': 4, '4': 2, '5': 8, '10': 'isFactory'},
    const {'1': 'type', '3': 5, '4': 2, '5': 11, '6': '.DartFunctionType', '10': 'type'},
    const {'1': 'parameters', '3': 6, '4': 3, '5': 11, '6': '.DartParameterElement', '10': 'parameters'},
  ],
};

/// Descriptor for `DartConstructorElement`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartConstructorElementDescriptor = $convert.base64Decode('ChZEYXJ0Q29uc3RydWN0b3JFbGVtZW50EhoKCGxvY2F0aW9uGAEgAigJUghsb2NhdGlvbhISCgRuYW1lGAIgAigJUgRuYW1lEhgKB2lzQ29uc3QYAyACKAhSB2lzQ29uc3QSHAoJaXNGYWN0b3J5GAQgAigIUglpc0ZhY3RvcnkSJQoEdHlwZRgFIAIoCzIRLkRhcnRGdW5jdGlvblR5cGVSBHR5cGUSNQoKcGFyYW1ldGVycxgGIAMoCzIVLkRhcnRQYXJhbWV0ZXJFbGVtZW50UgpwYXJhbWV0ZXJz');
@$core.Deprecated('Use dartParameterElementDescriptor instead')
const DartParameterElement$json = const {
  '1': 'DartParameterElement',
  '2': const [
    const {'1': 'location', '3': 1, '4': 2, '5': 9, '10': 'location'},
    const {'1': 'name', '3': 2, '4': 2, '5': 9, '10': 'name'},
    const {'1': 'type', '3': 3, '4': 2, '5': 11, '6': '.DartType', '10': 'type'},
    const {'1': 'isCovariant', '3': 4, '4': 2, '5': 8, '10': 'isCovariant'},
    const {'1': 'isNamed', '3': 5, '4': 2, '5': 8, '10': 'isNamed'},
    const {'1': 'isRequired', '3': 6, '4': 2, '5': 8, '10': 'isRequired'},
    const {'1': 'fieldLocation', '3': 7, '4': 1, '5': 9, '10': 'fieldLocation'},
    const {'1': 'superConstructorParameterLocation', '3': 8, '4': 1, '5': 9, '10': 'superConstructorParameterLocation'},
    const {'1': 'defaultValueCode', '3': 9, '4': 1, '5': 9, '10': 'defaultValueCode'},
  ],
};

/// Descriptor for `DartParameterElement`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartParameterElementDescriptor = $convert.base64Decode('ChREYXJ0UGFyYW1ldGVyRWxlbWVudBIaCghsb2NhdGlvbhgBIAIoCVIIbG9jYXRpb24SEgoEbmFtZRgCIAIoCVIEbmFtZRIdCgR0eXBlGAMgAigLMgkuRGFydFR5cGVSBHR5cGUSIAoLaXNDb3ZhcmlhbnQYBCACKAhSC2lzQ292YXJpYW50EhgKB2lzTmFtZWQYBSACKAhSB2lzTmFtZWQSHgoKaXNSZXF1aXJlZBgGIAIoCFIKaXNSZXF1aXJlZBIkCg1maWVsZExvY2F0aW9uGAcgASgJUg1maWVsZExvY2F0aW9uEkwKIXN1cGVyQ29uc3RydWN0b3JQYXJhbWV0ZXJMb2NhdGlvbhgIIAEoCVIhc3VwZXJDb25zdHJ1Y3RvclBhcmFtZXRlckxvY2F0aW9uEioKEGRlZmF1bHRWYWx1ZUNvZGUYCSABKAlSEGRlZmF1bHRWYWx1ZUNvZGU=');
@$core.Deprecated('Use dartTypeDescriptor instead')
const DartType$json = const {
  '1': 'DartType',
  '2': const [
    const {'1': 'type', '3': 1, '4': 2, '5': 9, '10': 'type'},
    const {'1': 'value', '3': 2, '4': 2, '5': 12, '10': 'value'},
  ],
};

/// Descriptor for `DartType`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartTypeDescriptor = $convert.base64Decode('CghEYXJ0VHlwZRISCgR0eXBlGAEgAigJUgR0eXBlEhQKBXZhbHVlGAIgAigMUgV2YWx1ZQ==');
@$core.Deprecated('Use dartLibraryExportElementDescriptor instead')
const DartLibraryExportElement$json = const {
  '1': 'DartLibraryExportElement',
  '2': const [
    const {'1': 'location', '3': 1, '4': 2, '5': 9, '10': 'location'},
    const {'1': 'exportLocation', '3': 2, '4': 2, '5': 9, '10': 'exportLocation'},
    const {'1': 'show', '3': 3, '4': 3, '5': 9, '10': 'show'},
    const {'1': 'hide', '3': 4, '4': 3, '5': 9, '10': 'hide'},
  ],
};

/// Descriptor for `DartLibraryExportElement`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartLibraryExportElementDescriptor = $convert.base64Decode('ChhEYXJ0TGlicmFyeUV4cG9ydEVsZW1lbnQSGgoIbG9jYXRpb24YASACKAlSCGxvY2F0aW9uEiYKDmV4cG9ydExvY2F0aW9uGAIgAigJUg5leHBvcnRMb2NhdGlvbhISCgRzaG93GAMgAygJUgRzaG93EhIKBGhpZGUYBCADKAlSBGhpZGU=');
@$core.Deprecated('Use dartInterfaceTypeDescriptor instead')
const DartInterfaceType$json = const {
  '1': 'DartInterfaceType',
  '2': const [
    const {'1': 'elementLocation', '3': 1, '4': 2, '5': 9, '10': 'elementLocation'},
    const {'1': 'typeArguments', '3': 2, '4': 3, '5': 11, '6': '.DartType', '10': 'typeArguments'},
    const {'1': 'superClass', '3': 3, '4': 1, '5': 11, '6': '.DartInterfaceType', '10': 'superClass'},
    const {'1': 'superInterfaceTypes', '3': 4, '4': 3, '5': 11, '6': '.DartInterfaceType', '10': 'superInterfaceTypes'},
    const {'1': 'superMixinTypes', '3': 5, '4': 3, '5': 11, '6': '.DartInterfaceType', '10': 'superMixinTypes'},
    const {'1': 'nullabilitySuffix', '3': 6, '4': 2, '5': 14, '6': '.DartNullabilitySuffix', '10': 'nullabilitySuffix'},
  ],
};

/// Descriptor for `DartInterfaceType`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartInterfaceTypeDescriptor = $convert.base64Decode('ChFEYXJ0SW50ZXJmYWNlVHlwZRIoCg9lbGVtZW50TG9jYXRpb24YASACKAlSD2VsZW1lbnRMb2NhdGlvbhIvCg10eXBlQXJndW1lbnRzGAIgAygLMgkuRGFydFR5cGVSDXR5cGVBcmd1bWVudHMSMgoKc3VwZXJDbGFzcxgDIAEoCzISLkRhcnRJbnRlcmZhY2VUeXBlUgpzdXBlckNsYXNzEkQKE3N1cGVySW50ZXJmYWNlVHlwZXMYBCADKAsyEi5EYXJ0SW50ZXJmYWNlVHlwZVITc3VwZXJJbnRlcmZhY2VUeXBlcxI8Cg9zdXBlck1peGluVHlwZXMYBSADKAsyEi5EYXJ0SW50ZXJmYWNlVHlwZVIPc3VwZXJNaXhpblR5cGVzEkQKEW51bGxhYmlsaXR5U3VmZml4GAYgAigOMhYuRGFydE51bGxhYmlsaXR5U3VmZml4UhFudWxsYWJpbGl0eVN1ZmZpeA==');
@$core.Deprecated('Use dartFunctionTypeDescriptor instead')
const DartFunctionType$json = const {
  '1': 'DartFunctionType',
  '2': const [
    const {'1': 'parameters', '3': 1, '4': 3, '5': 11, '6': '.DartParameterElement', '10': 'parameters'},
    const {'1': 'typeParameters', '3': 2, '4': 3, '5': 11, '6': '.DartTypeParameterElement', '10': 'typeParameters'},
    const {'1': 'returnType', '3': 3, '4': 2, '5': 11, '6': '.DartType', '10': 'returnType'},
    const {'1': 'nullabilitySuffix', '3': 4, '4': 2, '5': 14, '6': '.DartNullabilitySuffix', '10': 'nullabilitySuffix'},
  ],
};

/// Descriptor for `DartFunctionType`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartFunctionTypeDescriptor = $convert.base64Decode('ChBEYXJ0RnVuY3Rpb25UeXBlEjUKCnBhcmFtZXRlcnMYASADKAsyFS5EYXJ0UGFyYW1ldGVyRWxlbWVudFIKcGFyYW1ldGVycxJBCg50eXBlUGFyYW1ldGVycxgCIAMoCzIZLkRhcnRUeXBlUGFyYW1ldGVyRWxlbWVudFIOdHlwZVBhcmFtZXRlcnMSKQoKcmV0dXJuVHlwZRgDIAIoCzIJLkRhcnRUeXBlUgpyZXR1cm5UeXBlEkQKEW51bGxhYmlsaXR5U3VmZml4GAQgAigOMhYuRGFydE51bGxhYmlsaXR5U3VmZml4UhFudWxsYWJpbGl0eVN1ZmZpeA==');
@$core.Deprecated('Use dartDynamicTypeDescriptor instead')
const DartDynamicType$json = const {
  '1': 'DartDynamicType',
};

/// Descriptor for `DartDynamicType`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartDynamicTypeDescriptor = $convert.base64Decode('Cg9EYXJ0RHluYW1pY1R5cGU=');
@$core.Deprecated('Use dartNeverTypeDescriptor instead')
const DartNeverType$json = const {
  '1': 'DartNeverType',
};

/// Descriptor for `DartNeverType`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartNeverTypeDescriptor = $convert.base64Decode('Cg1EYXJ0TmV2ZXJUeXBl');
@$core.Deprecated('Use dartTypeParameterTypeDescriptor instead')
const DartTypeParameterType$json = const {
  '1': 'DartTypeParameterType',
  '2': const [
    const {'1': 'elementLocation', '3': 1, '4': 2, '5': 9, '10': 'elementLocation'},
    const {'1': 'bound', '3': 2, '4': 2, '5': 11, '6': '.DartType', '10': 'bound'},
    const {'1': 'nullabilitySuffix', '3': 3, '4': 2, '5': 14, '6': '.DartNullabilitySuffix', '10': 'nullabilitySuffix'},
  ],
};

/// Descriptor for `DartTypeParameterType`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartTypeParameterTypeDescriptor = $convert.base64Decode('ChVEYXJ0VHlwZVBhcmFtZXRlclR5cGUSKAoPZWxlbWVudExvY2F0aW9uGAEgAigJUg9lbGVtZW50TG9jYXRpb24SHwoFYm91bmQYAiACKAsyCS5EYXJ0VHlwZVIFYm91bmQSRAoRbnVsbGFiaWxpdHlTdWZmaXgYAyACKA4yFi5EYXJ0TnVsbGFiaWxpdHlTdWZmaXhSEW51bGxhYmlsaXR5U3VmZml4');
@$core.Deprecated('Use dartVoidTypeDescriptor instead')
const DartVoidType$json = const {
  '1': 'DartVoidType',
};

/// Descriptor for `DartVoidType`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartVoidTypeDescriptor = $convert.base64Decode('CgxEYXJ0Vm9pZFR5cGU=');
@$core.Deprecated('Use dartTypeParameterElementDescriptor instead')
const DartTypeParameterElement$json = const {
  '1': 'DartTypeParameterElement',
  '2': const [
    const {'1': 'name', '3': 1, '4': 2, '5': 9, '10': 'name'},
    const {'1': 'location', '3': 2, '4': 2, '5': 9, '10': 'location'},
    const {'1': 'bound', '3': 3, '4': 1, '5': 11, '6': '.DartType', '10': 'bound'},
  ],
};

/// Descriptor for `DartTypeParameterElement`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List dartTypeParameterElementDescriptor = $convert.base64Decode('ChhEYXJ0VHlwZVBhcmFtZXRlckVsZW1lbnQSEgoEbmFtZRgBIAIoCVIEbmFtZRIaCghsb2NhdGlvbhgCIAIoCVIIbG9jYXRpb24SHwoFYm91bmQYAyABKAsyCS5EYXJ0VHlwZVIFYm91bmQ=');
