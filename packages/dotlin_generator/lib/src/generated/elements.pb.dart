///
//  Generated code. Do not modify.
//  source: elements.proto
//
// @dart = 2.12
// ignore_for_file: annotate_overrides,camel_case_types,constant_identifier_names,directives_ordering,library_prefixes,non_constant_identifier_names,prefer_final_fields,return_of_invalid_type,unnecessary_const,unnecessary_import,unnecessary_this,unused_import,unused_shown_name

import 'dart:core' as $core;

import 'package:protobuf/protobuf.dart' as $pb;

import 'elements.pbenum.dart';

export 'elements.pbenum.dart';

class DartPackageElement extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'DartPackageElement', createEmptyInstance: create)
    ..pc<DartLibraryElement>(1, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'libraries', $pb.PbFieldType.PM, subBuilder: DartLibraryElement.create)
  ;

  DartPackageElement._() : super();
  factory DartPackageElement({
    $core.Iterable<DartLibraryElement>? libraries,
  }) {
    final _result = create();
    if (libraries != null) {
      _result.libraries.addAll(libraries);
    }
    return _result;
  }
  factory DartPackageElement.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory DartPackageElement.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  DartPackageElement clone() => DartPackageElement()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  DartPackageElement copyWith(void Function(DartPackageElement) updates) => super.copyWith((message) => updates(message as DartPackageElement)) as DartPackageElement; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static DartPackageElement create() => DartPackageElement._();
  DartPackageElement createEmptyInstance() => create();
  static $pb.PbList<DartPackageElement> createRepeated() => $pb.PbList<DartPackageElement>();
  @$core.pragma('dart2js:noInline')
  static DartPackageElement getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<DartPackageElement>(create);
  static DartPackageElement? _defaultInstance;

  @$pb.TagNumber(1)
  $core.List<DartLibraryElement> get libraries => $_getList(0);
}

class DartLibraryElement extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'DartLibraryElement', createEmptyInstance: create)
    ..aQS(1, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'location')
    ..aQS(2, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'path')
    ..pc<DartCompilationUnitElement>(3, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'units', $pb.PbFieldType.PM, subBuilder: DartCompilationUnitElement.create)
  ;

  DartLibraryElement._() : super();
  factory DartLibraryElement({
    $core.String? location,
    $core.String? path,
    $core.Iterable<DartCompilationUnitElement>? units,
  }) {
    final _result = create();
    if (location != null) {
      _result.location = location;
    }
    if (path != null) {
      _result.path = path;
    }
    if (units != null) {
      _result.units.addAll(units);
    }
    return _result;
  }
  factory DartLibraryElement.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory DartLibraryElement.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  DartLibraryElement clone() => DartLibraryElement()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  DartLibraryElement copyWith(void Function(DartLibraryElement) updates) => super.copyWith((message) => updates(message as DartLibraryElement)) as DartLibraryElement; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static DartLibraryElement create() => DartLibraryElement._();
  DartLibraryElement createEmptyInstance() => create();
  static $pb.PbList<DartLibraryElement> createRepeated() => $pb.PbList<DartLibraryElement>();
  @$core.pragma('dart2js:noInline')
  static DartLibraryElement getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<DartLibraryElement>(create);
  static DartLibraryElement? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get location => $_getSZ(0);
  @$pb.TagNumber(1)
  set location($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasLocation() => $_has(0);
  @$pb.TagNumber(1)
  void clearLocation() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get path => $_getSZ(1);
  @$pb.TagNumber(2)
  set path($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasPath() => $_has(1);
  @$pb.TagNumber(2)
  void clearPath() => clearField(2);

  @$pb.TagNumber(3)
  $core.List<DartCompilationUnitElement> get units => $_getList(2);
}

class DartCompilationUnitElement extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'DartCompilationUnitElement', createEmptyInstance: create)
    ..aQS(1, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'location')
    ..pc<DartClassElement>(2, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'classes', $pb.PbFieldType.PM, subBuilder: DartClassElement.create)
    ..pc<DartFunctionElement>(3, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'functions', $pb.PbFieldType.PM, subBuilder: DartFunctionElement.create)
  ;

  DartCompilationUnitElement._() : super();
  factory DartCompilationUnitElement({
    $core.String? location,
    $core.Iterable<DartClassElement>? classes,
    $core.Iterable<DartFunctionElement>? functions,
  }) {
    final _result = create();
    if (location != null) {
      _result.location = location;
    }
    if (classes != null) {
      _result.classes.addAll(classes);
    }
    if (functions != null) {
      _result.functions.addAll(functions);
    }
    return _result;
  }
  factory DartCompilationUnitElement.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory DartCompilationUnitElement.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  DartCompilationUnitElement clone() => DartCompilationUnitElement()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  DartCompilationUnitElement copyWith(void Function(DartCompilationUnitElement) updates) => super.copyWith((message) => updates(message as DartCompilationUnitElement)) as DartCompilationUnitElement; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static DartCompilationUnitElement create() => DartCompilationUnitElement._();
  DartCompilationUnitElement createEmptyInstance() => create();
  static $pb.PbList<DartCompilationUnitElement> createRepeated() => $pb.PbList<DartCompilationUnitElement>();
  @$core.pragma('dart2js:noInline')
  static DartCompilationUnitElement getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<DartCompilationUnitElement>(create);
  static DartCompilationUnitElement? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get location => $_getSZ(0);
  @$pb.TagNumber(1)
  set location($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasLocation() => $_has(0);
  @$pb.TagNumber(1)
  void clearLocation() => clearField(1);

  @$pb.TagNumber(2)
  $core.List<DartClassElement> get classes => $_getList(1);

  @$pb.TagNumber(3)
  $core.List<DartFunctionElement> get functions => $_getList(2);
}

class DartClassElement extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'DartClassElement', createEmptyInstance: create)
    ..aQS(1, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'location')
    ..aQS(2, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'name')
    ..pc<DartTypeParameterElement>(3, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'typeParameters', $pb.PbFieldType.PM, protoName: 'typeParameters', subBuilder: DartTypeParameterElement.create)
    ..a<$core.bool>(4, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'isAbstract', $pb.PbFieldType.QB, protoName: 'isAbstract')
    ..pc<DartConstructorElement>(5, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'constructors', $pb.PbFieldType.PM, subBuilder: DartConstructorElement.create)
    ..pc<DartFieldElement>(6, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'fields', $pb.PbFieldType.PM, subBuilder: DartFieldElement.create)
  ;

  DartClassElement._() : super();
  factory DartClassElement({
    $core.String? location,
    $core.String? name,
    $core.Iterable<DartTypeParameterElement>? typeParameters,
    $core.bool? isAbstract,
    $core.Iterable<DartConstructorElement>? constructors,
    $core.Iterable<DartFieldElement>? fields,
  }) {
    final _result = create();
    if (location != null) {
      _result.location = location;
    }
    if (name != null) {
      _result.name = name;
    }
    if (typeParameters != null) {
      _result.typeParameters.addAll(typeParameters);
    }
    if (isAbstract != null) {
      _result.isAbstract = isAbstract;
    }
    if (constructors != null) {
      _result.constructors.addAll(constructors);
    }
    if (fields != null) {
      _result.fields.addAll(fields);
    }
    return _result;
  }
  factory DartClassElement.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory DartClassElement.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  DartClassElement clone() => DartClassElement()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  DartClassElement copyWith(void Function(DartClassElement) updates) => super.copyWith((message) => updates(message as DartClassElement)) as DartClassElement; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static DartClassElement create() => DartClassElement._();
  DartClassElement createEmptyInstance() => create();
  static $pb.PbList<DartClassElement> createRepeated() => $pb.PbList<DartClassElement>();
  @$core.pragma('dart2js:noInline')
  static DartClassElement getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<DartClassElement>(create);
  static DartClassElement? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get location => $_getSZ(0);
  @$pb.TagNumber(1)
  set location($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasLocation() => $_has(0);
  @$pb.TagNumber(1)
  void clearLocation() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get name => $_getSZ(1);
  @$pb.TagNumber(2)
  set name($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasName() => $_has(1);
  @$pb.TagNumber(2)
  void clearName() => clearField(2);

  @$pb.TagNumber(3)
  $core.List<DartTypeParameterElement> get typeParameters => $_getList(2);

  @$pb.TagNumber(4)
  $core.bool get isAbstract => $_getBF(3);
  @$pb.TagNumber(4)
  set isAbstract($core.bool v) { $_setBool(3, v); }
  @$pb.TagNumber(4)
  $core.bool hasIsAbstract() => $_has(3);
  @$pb.TagNumber(4)
  void clearIsAbstract() => clearField(4);

  @$pb.TagNumber(5)
  $core.List<DartConstructorElement> get constructors => $_getList(4);

  @$pb.TagNumber(6)
  $core.List<DartFieldElement> get fields => $_getList(5);
}

class DartFieldElement extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'DartFieldElement', createEmptyInstance: create)
    ..aQS(1, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'location')
    ..aQS(2, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'name')
    ..a<$core.bool>(3, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'isAbstract', $pb.PbFieldType.QB, protoName: 'isAbstract')
    ..a<$core.bool>(4, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'isCovariant', $pb.PbFieldType.QB, protoName: 'isCovariant')
    ..a<$core.bool>(5, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'isConst', $pb.PbFieldType.QB, protoName: 'isConst')
    ..a<$core.bool>(6, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'isFinal', $pb.PbFieldType.QB, protoName: 'isFinal')
    ..a<$core.bool>(7, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'isLate', $pb.PbFieldType.QB, protoName: 'isLate')
    ..a<$core.bool>(8, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'isStatic', $pb.PbFieldType.QB, protoName: 'isStatic')
    ..aQM<DartType>(9, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'type', subBuilder: DartType.create)
  ;

  DartFieldElement._() : super();
  factory DartFieldElement({
    $core.String? location,
    $core.String? name,
    $core.bool? isAbstract,
    $core.bool? isCovariant,
    $core.bool? isConst,
    $core.bool? isFinal,
    $core.bool? isLate,
    $core.bool? isStatic,
    DartType? type,
  }) {
    final _result = create();
    if (location != null) {
      _result.location = location;
    }
    if (name != null) {
      _result.name = name;
    }
    if (isAbstract != null) {
      _result.isAbstract = isAbstract;
    }
    if (isCovariant != null) {
      _result.isCovariant = isCovariant;
    }
    if (isConst != null) {
      _result.isConst = isConst;
    }
    if (isFinal != null) {
      _result.isFinal = isFinal;
    }
    if (isLate != null) {
      _result.isLate = isLate;
    }
    if (isStatic != null) {
      _result.isStatic = isStatic;
    }
    if (type != null) {
      _result.type = type;
    }
    return _result;
  }
  factory DartFieldElement.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory DartFieldElement.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  DartFieldElement clone() => DartFieldElement()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  DartFieldElement copyWith(void Function(DartFieldElement) updates) => super.copyWith((message) => updates(message as DartFieldElement)) as DartFieldElement; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static DartFieldElement create() => DartFieldElement._();
  DartFieldElement createEmptyInstance() => create();
  static $pb.PbList<DartFieldElement> createRepeated() => $pb.PbList<DartFieldElement>();
  @$core.pragma('dart2js:noInline')
  static DartFieldElement getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<DartFieldElement>(create);
  static DartFieldElement? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get location => $_getSZ(0);
  @$pb.TagNumber(1)
  set location($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasLocation() => $_has(0);
  @$pb.TagNumber(1)
  void clearLocation() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get name => $_getSZ(1);
  @$pb.TagNumber(2)
  set name($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasName() => $_has(1);
  @$pb.TagNumber(2)
  void clearName() => clearField(2);

  @$pb.TagNumber(3)
  $core.bool get isAbstract => $_getBF(2);
  @$pb.TagNumber(3)
  set isAbstract($core.bool v) { $_setBool(2, v); }
  @$pb.TagNumber(3)
  $core.bool hasIsAbstract() => $_has(2);
  @$pb.TagNumber(3)
  void clearIsAbstract() => clearField(3);

  @$pb.TagNumber(4)
  $core.bool get isCovariant => $_getBF(3);
  @$pb.TagNumber(4)
  set isCovariant($core.bool v) { $_setBool(3, v); }
  @$pb.TagNumber(4)
  $core.bool hasIsCovariant() => $_has(3);
  @$pb.TagNumber(4)
  void clearIsCovariant() => clearField(4);

  @$pb.TagNumber(5)
  $core.bool get isConst => $_getBF(4);
  @$pb.TagNumber(5)
  set isConst($core.bool v) { $_setBool(4, v); }
  @$pb.TagNumber(5)
  $core.bool hasIsConst() => $_has(4);
  @$pb.TagNumber(5)
  void clearIsConst() => clearField(5);

  @$pb.TagNumber(6)
  $core.bool get isFinal => $_getBF(5);
  @$pb.TagNumber(6)
  set isFinal($core.bool v) { $_setBool(5, v); }
  @$pb.TagNumber(6)
  $core.bool hasIsFinal() => $_has(5);
  @$pb.TagNumber(6)
  void clearIsFinal() => clearField(6);

  @$pb.TagNumber(7)
  $core.bool get isLate => $_getBF(6);
  @$pb.TagNumber(7)
  set isLate($core.bool v) { $_setBool(6, v); }
  @$pb.TagNumber(7)
  $core.bool hasIsLate() => $_has(6);
  @$pb.TagNumber(7)
  void clearIsLate() => clearField(7);

  @$pb.TagNumber(8)
  $core.bool get isStatic => $_getBF(7);
  @$pb.TagNumber(8)
  set isStatic($core.bool v) { $_setBool(7, v); }
  @$pb.TagNumber(8)
  $core.bool hasIsStatic() => $_has(7);
  @$pb.TagNumber(8)
  void clearIsStatic() => clearField(8);

  @$pb.TagNumber(9)
  DartType get type => $_getN(8);
  @$pb.TagNumber(9)
  set type(DartType v) { setField(9, v); }
  @$pb.TagNumber(9)
  $core.bool hasType() => $_has(8);
  @$pb.TagNumber(9)
  void clearType() => clearField(9);
  @$pb.TagNumber(9)
  DartType ensureType() => $_ensure(8);
}

class DartFunctionElement extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'DartFunctionElement', createEmptyInstance: create)
    ..aQS(1, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'location')
    ..aQS(2, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'name')
    ..a<$core.bool>(3, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'isAsync', $pb.PbFieldType.QB, protoName: 'isAsync')
    ..a<$core.bool>(4, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'isGenerator', $pb.PbFieldType.QB, protoName: 'isGenerator')
    ..a<$core.bool>(5, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'isAbstract', $pb.PbFieldType.QB, protoName: 'isAbstract')
    ..a<$core.bool>(6, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'isStatic', $pb.PbFieldType.QB, protoName: 'isStatic')
    ..a<$core.bool>(7, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'isOperator', $pb.PbFieldType.QB, protoName: 'isOperator')
    ..pc<DartParameterElement>(8, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'parameters', $pb.PbFieldType.PM, subBuilder: DartParameterElement.create)
    ..pc<DartTypeParameterElement>(9, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'typeParameters', $pb.PbFieldType.PM, protoName: 'typeParameters', subBuilder: DartTypeParameterElement.create)
    ..aQM<DartFunctionType>(10, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'type', subBuilder: DartFunctionType.create)
  ;

  DartFunctionElement._() : super();
  factory DartFunctionElement({
    $core.String? location,
    $core.String? name,
    $core.bool? isAsync,
    $core.bool? isGenerator,
    $core.bool? isAbstract,
    $core.bool? isStatic,
    $core.bool? isOperator,
    $core.Iterable<DartParameterElement>? parameters,
    $core.Iterable<DartTypeParameterElement>? typeParameters,
    DartFunctionType? type,
  }) {
    final _result = create();
    if (location != null) {
      _result.location = location;
    }
    if (name != null) {
      _result.name = name;
    }
    if (isAsync != null) {
      _result.isAsync = isAsync;
    }
    if (isGenerator != null) {
      _result.isGenerator = isGenerator;
    }
    if (isAbstract != null) {
      _result.isAbstract = isAbstract;
    }
    if (isStatic != null) {
      _result.isStatic = isStatic;
    }
    if (isOperator != null) {
      _result.isOperator = isOperator;
    }
    if (parameters != null) {
      _result.parameters.addAll(parameters);
    }
    if (typeParameters != null) {
      _result.typeParameters.addAll(typeParameters);
    }
    if (type != null) {
      _result.type = type;
    }
    return _result;
  }
  factory DartFunctionElement.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory DartFunctionElement.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  DartFunctionElement clone() => DartFunctionElement()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  DartFunctionElement copyWith(void Function(DartFunctionElement) updates) => super.copyWith((message) => updates(message as DartFunctionElement)) as DartFunctionElement; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static DartFunctionElement create() => DartFunctionElement._();
  DartFunctionElement createEmptyInstance() => create();
  static $pb.PbList<DartFunctionElement> createRepeated() => $pb.PbList<DartFunctionElement>();
  @$core.pragma('dart2js:noInline')
  static DartFunctionElement getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<DartFunctionElement>(create);
  static DartFunctionElement? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get location => $_getSZ(0);
  @$pb.TagNumber(1)
  set location($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasLocation() => $_has(0);
  @$pb.TagNumber(1)
  void clearLocation() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get name => $_getSZ(1);
  @$pb.TagNumber(2)
  set name($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasName() => $_has(1);
  @$pb.TagNumber(2)
  void clearName() => clearField(2);

  @$pb.TagNumber(3)
  $core.bool get isAsync => $_getBF(2);
  @$pb.TagNumber(3)
  set isAsync($core.bool v) { $_setBool(2, v); }
  @$pb.TagNumber(3)
  $core.bool hasIsAsync() => $_has(2);
  @$pb.TagNumber(3)
  void clearIsAsync() => clearField(3);

  @$pb.TagNumber(4)
  $core.bool get isGenerator => $_getBF(3);
  @$pb.TagNumber(4)
  set isGenerator($core.bool v) { $_setBool(3, v); }
  @$pb.TagNumber(4)
  $core.bool hasIsGenerator() => $_has(3);
  @$pb.TagNumber(4)
  void clearIsGenerator() => clearField(4);

  @$pb.TagNumber(5)
  $core.bool get isAbstract => $_getBF(4);
  @$pb.TagNumber(5)
  set isAbstract($core.bool v) { $_setBool(4, v); }
  @$pb.TagNumber(5)
  $core.bool hasIsAbstract() => $_has(4);
  @$pb.TagNumber(5)
  void clearIsAbstract() => clearField(5);

  @$pb.TagNumber(6)
  $core.bool get isStatic => $_getBF(5);
  @$pb.TagNumber(6)
  set isStatic($core.bool v) { $_setBool(5, v); }
  @$pb.TagNumber(6)
  $core.bool hasIsStatic() => $_has(5);
  @$pb.TagNumber(6)
  void clearIsStatic() => clearField(6);

  @$pb.TagNumber(7)
  $core.bool get isOperator => $_getBF(6);
  @$pb.TagNumber(7)
  set isOperator($core.bool v) { $_setBool(6, v); }
  @$pb.TagNumber(7)
  $core.bool hasIsOperator() => $_has(6);
  @$pb.TagNumber(7)
  void clearIsOperator() => clearField(7);

  @$pb.TagNumber(8)
  $core.List<DartParameterElement> get parameters => $_getList(7);

  @$pb.TagNumber(9)
  $core.List<DartTypeParameterElement> get typeParameters => $_getList(8);

  @$pb.TagNumber(10)
  DartFunctionType get type => $_getN(9);
  @$pb.TagNumber(10)
  set type(DartFunctionType v) { setField(10, v); }
  @$pb.TagNumber(10)
  $core.bool hasType() => $_has(9);
  @$pb.TagNumber(10)
  void clearType() => clearField(10);
  @$pb.TagNumber(10)
  DartFunctionType ensureType() => $_ensure(9);
}

class DartConstructorElement extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'DartConstructorElement', createEmptyInstance: create)
    ..aQS(1, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'location')
    ..aQS(2, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'name')
    ..a<$core.bool>(3, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'isConst', $pb.PbFieldType.QB, protoName: 'isConst')
    ..aQM<DartFunctionType>(4, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'type', subBuilder: DartFunctionType.create)
    ..pc<DartParameterElement>(5, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'parameters', $pb.PbFieldType.PM, subBuilder: DartParameterElement.create)
  ;

  DartConstructorElement._() : super();
  factory DartConstructorElement({
    $core.String? location,
    $core.String? name,
    $core.bool? isConst,
    DartFunctionType? type,
    $core.Iterable<DartParameterElement>? parameters,
  }) {
    final _result = create();
    if (location != null) {
      _result.location = location;
    }
    if (name != null) {
      _result.name = name;
    }
    if (isConst != null) {
      _result.isConst = isConst;
    }
    if (type != null) {
      _result.type = type;
    }
    if (parameters != null) {
      _result.parameters.addAll(parameters);
    }
    return _result;
  }
  factory DartConstructorElement.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory DartConstructorElement.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  DartConstructorElement clone() => DartConstructorElement()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  DartConstructorElement copyWith(void Function(DartConstructorElement) updates) => super.copyWith((message) => updates(message as DartConstructorElement)) as DartConstructorElement; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static DartConstructorElement create() => DartConstructorElement._();
  DartConstructorElement createEmptyInstance() => create();
  static $pb.PbList<DartConstructorElement> createRepeated() => $pb.PbList<DartConstructorElement>();
  @$core.pragma('dart2js:noInline')
  static DartConstructorElement getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<DartConstructorElement>(create);
  static DartConstructorElement? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get location => $_getSZ(0);
  @$pb.TagNumber(1)
  set location($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasLocation() => $_has(0);
  @$pb.TagNumber(1)
  void clearLocation() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get name => $_getSZ(1);
  @$pb.TagNumber(2)
  set name($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasName() => $_has(1);
  @$pb.TagNumber(2)
  void clearName() => clearField(2);

  @$pb.TagNumber(3)
  $core.bool get isConst => $_getBF(2);
  @$pb.TagNumber(3)
  set isConst($core.bool v) { $_setBool(2, v); }
  @$pb.TagNumber(3)
  $core.bool hasIsConst() => $_has(2);
  @$pb.TagNumber(3)
  void clearIsConst() => clearField(3);

  @$pb.TagNumber(4)
  DartFunctionType get type => $_getN(3);
  @$pb.TagNumber(4)
  set type(DartFunctionType v) { setField(4, v); }
  @$pb.TagNumber(4)
  $core.bool hasType() => $_has(3);
  @$pb.TagNumber(4)
  void clearType() => clearField(4);
  @$pb.TagNumber(4)
  DartFunctionType ensureType() => $_ensure(3);

  @$pb.TagNumber(5)
  $core.List<DartParameterElement> get parameters => $_getList(4);
}

class DartParameterElement extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'DartParameterElement', createEmptyInstance: create)
    ..aQS(1, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'location')
    ..aQS(2, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'name')
    ..aQM<DartType>(3, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'type', subBuilder: DartType.create)
    ..a<$core.bool>(4, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'isCovariant', $pb.PbFieldType.QB, protoName: 'isCovariant')
    ..a<$core.bool>(5, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'isNamed', $pb.PbFieldType.QB, protoName: 'isNamed')
    ..a<$core.bool>(6, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'isRequired', $pb.PbFieldType.QB, protoName: 'isRequired')
    ..aOS(7, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'fieldLocation', protoName: 'fieldLocation')
    ..aOS(8, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'superConstructorParameterLocation', protoName: 'superConstructorParameterLocation')
    ..aOS(9, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'defaultValueCode', protoName: 'defaultValueCode')
  ;

  DartParameterElement._() : super();
  factory DartParameterElement({
    $core.String? location,
    $core.String? name,
    DartType? type,
    $core.bool? isCovariant,
    $core.bool? isNamed,
    $core.bool? isRequired,
    $core.String? fieldLocation,
    $core.String? superConstructorParameterLocation,
    $core.String? defaultValueCode,
  }) {
    final _result = create();
    if (location != null) {
      _result.location = location;
    }
    if (name != null) {
      _result.name = name;
    }
    if (type != null) {
      _result.type = type;
    }
    if (isCovariant != null) {
      _result.isCovariant = isCovariant;
    }
    if (isNamed != null) {
      _result.isNamed = isNamed;
    }
    if (isRequired != null) {
      _result.isRequired = isRequired;
    }
    if (fieldLocation != null) {
      _result.fieldLocation = fieldLocation;
    }
    if (superConstructorParameterLocation != null) {
      _result.superConstructorParameterLocation = superConstructorParameterLocation;
    }
    if (defaultValueCode != null) {
      _result.defaultValueCode = defaultValueCode;
    }
    return _result;
  }
  factory DartParameterElement.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory DartParameterElement.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  DartParameterElement clone() => DartParameterElement()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  DartParameterElement copyWith(void Function(DartParameterElement) updates) => super.copyWith((message) => updates(message as DartParameterElement)) as DartParameterElement; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static DartParameterElement create() => DartParameterElement._();
  DartParameterElement createEmptyInstance() => create();
  static $pb.PbList<DartParameterElement> createRepeated() => $pb.PbList<DartParameterElement>();
  @$core.pragma('dart2js:noInline')
  static DartParameterElement getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<DartParameterElement>(create);
  static DartParameterElement? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get location => $_getSZ(0);
  @$pb.TagNumber(1)
  set location($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasLocation() => $_has(0);
  @$pb.TagNumber(1)
  void clearLocation() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get name => $_getSZ(1);
  @$pb.TagNumber(2)
  set name($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasName() => $_has(1);
  @$pb.TagNumber(2)
  void clearName() => clearField(2);

  @$pb.TagNumber(3)
  DartType get type => $_getN(2);
  @$pb.TagNumber(3)
  set type(DartType v) { setField(3, v); }
  @$pb.TagNumber(3)
  $core.bool hasType() => $_has(2);
  @$pb.TagNumber(3)
  void clearType() => clearField(3);
  @$pb.TagNumber(3)
  DartType ensureType() => $_ensure(2);

  @$pb.TagNumber(4)
  $core.bool get isCovariant => $_getBF(3);
  @$pb.TagNumber(4)
  set isCovariant($core.bool v) { $_setBool(3, v); }
  @$pb.TagNumber(4)
  $core.bool hasIsCovariant() => $_has(3);
  @$pb.TagNumber(4)
  void clearIsCovariant() => clearField(4);

  @$pb.TagNumber(5)
  $core.bool get isNamed => $_getBF(4);
  @$pb.TagNumber(5)
  set isNamed($core.bool v) { $_setBool(4, v); }
  @$pb.TagNumber(5)
  $core.bool hasIsNamed() => $_has(4);
  @$pb.TagNumber(5)
  void clearIsNamed() => clearField(5);

  @$pb.TagNumber(6)
  $core.bool get isRequired => $_getBF(5);
  @$pb.TagNumber(6)
  set isRequired($core.bool v) { $_setBool(5, v); }
  @$pb.TagNumber(6)
  $core.bool hasIsRequired() => $_has(5);
  @$pb.TagNumber(6)
  void clearIsRequired() => clearField(6);

  @$pb.TagNumber(7)
  $core.String get fieldLocation => $_getSZ(6);
  @$pb.TagNumber(7)
  set fieldLocation($core.String v) { $_setString(6, v); }
  @$pb.TagNumber(7)
  $core.bool hasFieldLocation() => $_has(6);
  @$pb.TagNumber(7)
  void clearFieldLocation() => clearField(7);

  @$pb.TagNumber(8)
  $core.String get superConstructorParameterLocation => $_getSZ(7);
  @$pb.TagNumber(8)
  set superConstructorParameterLocation($core.String v) { $_setString(7, v); }
  @$pb.TagNumber(8)
  $core.bool hasSuperConstructorParameterLocation() => $_has(7);
  @$pb.TagNumber(8)
  void clearSuperConstructorParameterLocation() => clearField(8);

  @$pb.TagNumber(9)
  $core.String get defaultValueCode => $_getSZ(8);
  @$pb.TagNumber(9)
  set defaultValueCode($core.String v) { $_setString(8, v); }
  @$pb.TagNumber(9)
  $core.bool hasDefaultValueCode() => $_has(8);
  @$pb.TagNumber(9)
  void clearDefaultValueCode() => clearField(9);
}

class DartType extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'DartType', createEmptyInstance: create)
    ..aQS(1, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'type')
    ..a<$core.List<$core.int>>(2, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'value', $pb.PbFieldType.QY)
  ;

  DartType._() : super();
  factory DartType({
    $core.String? type,
    $core.List<$core.int>? value,
  }) {
    final _result = create();
    if (type != null) {
      _result.type = type;
    }
    if (value != null) {
      _result.value = value;
    }
    return _result;
  }
  factory DartType.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory DartType.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  DartType clone() => DartType()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  DartType copyWith(void Function(DartType) updates) => super.copyWith((message) => updates(message as DartType)) as DartType; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static DartType create() => DartType._();
  DartType createEmptyInstance() => create();
  static $pb.PbList<DartType> createRepeated() => $pb.PbList<DartType>();
  @$core.pragma('dart2js:noInline')
  static DartType getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<DartType>(create);
  static DartType? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get type => $_getSZ(0);
  @$pb.TagNumber(1)
  set type($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasType() => $_has(0);
  @$pb.TagNumber(1)
  void clearType() => clearField(1);

  @$pb.TagNumber(2)
  $core.List<$core.int> get value => $_getN(1);
  @$pb.TagNumber(2)
  set value($core.List<$core.int> v) { $_setBytes(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasValue() => $_has(1);
  @$pb.TagNumber(2)
  void clearValue() => clearField(2);
}

class DartTypeParameterElement extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'DartTypeParameterElement', createEmptyInstance: create)
    ..aQS(1, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'name')
    ..aQS(2, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'location')
    ..aOM<DartType>(3, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'bound', subBuilder: DartType.create)
  ;

  DartTypeParameterElement._() : super();
  factory DartTypeParameterElement({
    $core.String? name,
    $core.String? location,
    DartType? bound,
  }) {
    final _result = create();
    if (name != null) {
      _result.name = name;
    }
    if (location != null) {
      _result.location = location;
    }
    if (bound != null) {
      _result.bound = bound;
    }
    return _result;
  }
  factory DartTypeParameterElement.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory DartTypeParameterElement.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  DartTypeParameterElement clone() => DartTypeParameterElement()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  DartTypeParameterElement copyWith(void Function(DartTypeParameterElement) updates) => super.copyWith((message) => updates(message as DartTypeParameterElement)) as DartTypeParameterElement; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static DartTypeParameterElement create() => DartTypeParameterElement._();
  DartTypeParameterElement createEmptyInstance() => create();
  static $pb.PbList<DartTypeParameterElement> createRepeated() => $pb.PbList<DartTypeParameterElement>();
  @$core.pragma('dart2js:noInline')
  static DartTypeParameterElement getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<DartTypeParameterElement>(create);
  static DartTypeParameterElement? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get name => $_getSZ(0);
  @$pb.TagNumber(1)
  set name($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasName() => $_has(0);
  @$pb.TagNumber(1)
  void clearName() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get location => $_getSZ(1);
  @$pb.TagNumber(2)
  set location($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasLocation() => $_has(1);
  @$pb.TagNumber(2)
  void clearLocation() => clearField(2);

  @$pb.TagNumber(3)
  DartType get bound => $_getN(2);
  @$pb.TagNumber(3)
  set bound(DartType v) { setField(3, v); }
  @$pb.TagNumber(3)
  $core.bool hasBound() => $_has(2);
  @$pb.TagNumber(3)
  void clearBound() => clearField(3);
  @$pb.TagNumber(3)
  DartType ensureBound() => $_ensure(2);
}

class DartFunctionType extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'DartFunctionType', createEmptyInstance: create)
    ..pc<DartParameterElement>(1, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'parameters', $pb.PbFieldType.PM, subBuilder: DartParameterElement.create)
    ..pc<DartTypeParameterElement>(2, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'typeParameters', $pb.PbFieldType.PM, protoName: 'typeParameters', subBuilder: DartTypeParameterElement.create)
    ..aQM<DartType>(3, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'returnType', protoName: 'returnType', subBuilder: DartType.create)
    ..e<DartNullabilitySuffix>(4, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'nullabilitySuffix', $pb.PbFieldType.QE, protoName: 'nullabilitySuffix', defaultOrMaker: DartNullabilitySuffix.QUESTION_MARK, valueOf: DartNullabilitySuffix.valueOf, enumValues: DartNullabilitySuffix.values)
  ;

  DartFunctionType._() : super();
  factory DartFunctionType({
    $core.Iterable<DartParameterElement>? parameters,
    $core.Iterable<DartTypeParameterElement>? typeParameters,
    DartType? returnType,
    DartNullabilitySuffix? nullabilitySuffix,
  }) {
    final _result = create();
    if (parameters != null) {
      _result.parameters.addAll(parameters);
    }
    if (typeParameters != null) {
      _result.typeParameters.addAll(typeParameters);
    }
    if (returnType != null) {
      _result.returnType = returnType;
    }
    if (nullabilitySuffix != null) {
      _result.nullabilitySuffix = nullabilitySuffix;
    }
    return _result;
  }
  factory DartFunctionType.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory DartFunctionType.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  DartFunctionType clone() => DartFunctionType()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  DartFunctionType copyWith(void Function(DartFunctionType) updates) => super.copyWith((message) => updates(message as DartFunctionType)) as DartFunctionType; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static DartFunctionType create() => DartFunctionType._();
  DartFunctionType createEmptyInstance() => create();
  static $pb.PbList<DartFunctionType> createRepeated() => $pb.PbList<DartFunctionType>();
  @$core.pragma('dart2js:noInline')
  static DartFunctionType getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<DartFunctionType>(create);
  static DartFunctionType? _defaultInstance;

  @$pb.TagNumber(1)
  $core.List<DartParameterElement> get parameters => $_getList(0);

  @$pb.TagNumber(2)
  $core.List<DartTypeParameterElement> get typeParameters => $_getList(1);

  @$pb.TagNumber(3)
  DartType get returnType => $_getN(2);
  @$pb.TagNumber(3)
  set returnType(DartType v) { setField(3, v); }
  @$pb.TagNumber(3)
  $core.bool hasReturnType() => $_has(2);
  @$pb.TagNumber(3)
  void clearReturnType() => clearField(3);
  @$pb.TagNumber(3)
  DartType ensureReturnType() => $_ensure(2);

  @$pb.TagNumber(4)
  DartNullabilitySuffix get nullabilitySuffix => $_getN(3);
  @$pb.TagNumber(4)
  set nullabilitySuffix(DartNullabilitySuffix v) { setField(4, v); }
  @$pb.TagNumber(4)
  $core.bool hasNullabilitySuffix() => $_has(3);
  @$pb.TagNumber(4)
  void clearNullabilitySuffix() => clearField(4);
}

class DartDynamicType extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'DartDynamicType', createEmptyInstance: create)
    ..hasRequiredFields = false
  ;

  DartDynamicType._() : super();
  factory DartDynamicType() => create();
  factory DartDynamicType.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory DartDynamicType.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  DartDynamicType clone() => DartDynamicType()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  DartDynamicType copyWith(void Function(DartDynamicType) updates) => super.copyWith((message) => updates(message as DartDynamicType)) as DartDynamicType; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static DartDynamicType create() => DartDynamicType._();
  DartDynamicType createEmptyInstance() => create();
  static $pb.PbList<DartDynamicType> createRepeated() => $pb.PbList<DartDynamicType>();
  @$core.pragma('dart2js:noInline')
  static DartDynamicType getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<DartDynamicType>(create);
  static DartDynamicType? _defaultInstance;
}

class DartInterfaceType extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'DartInterfaceType', createEmptyInstance: create)
    ..aQS(1, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'elementLocation', protoName: 'elementLocation')
    ..pc<DartType>(2, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'typeArguments', $pb.PbFieldType.PM, protoName: 'typeArguments', subBuilder: DartType.create)
    ..aOM<DartInterfaceType>(3, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'superClass', protoName: 'superClass', subBuilder: DartInterfaceType.create)
    ..pc<DartInterfaceType>(4, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'superInterfaceTypes', $pb.PbFieldType.PM, protoName: 'superInterfaceTypes', subBuilder: DartInterfaceType.create)
    ..pc<DartInterfaceType>(5, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'superMixinTypes', $pb.PbFieldType.PM, protoName: 'superMixinTypes', subBuilder: DartInterfaceType.create)
    ..e<DartNullabilitySuffix>(6, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'nullabilitySuffix', $pb.PbFieldType.QE, protoName: 'nullabilitySuffix', defaultOrMaker: DartNullabilitySuffix.QUESTION_MARK, valueOf: DartNullabilitySuffix.valueOf, enumValues: DartNullabilitySuffix.values)
  ;

  DartInterfaceType._() : super();
  factory DartInterfaceType({
    $core.String? elementLocation,
    $core.Iterable<DartType>? typeArguments,
    DartInterfaceType? superClass,
    $core.Iterable<DartInterfaceType>? superInterfaceTypes,
    $core.Iterable<DartInterfaceType>? superMixinTypes,
    DartNullabilitySuffix? nullabilitySuffix,
  }) {
    final _result = create();
    if (elementLocation != null) {
      _result.elementLocation = elementLocation;
    }
    if (typeArguments != null) {
      _result.typeArguments.addAll(typeArguments);
    }
    if (superClass != null) {
      _result.superClass = superClass;
    }
    if (superInterfaceTypes != null) {
      _result.superInterfaceTypes.addAll(superInterfaceTypes);
    }
    if (superMixinTypes != null) {
      _result.superMixinTypes.addAll(superMixinTypes);
    }
    if (nullabilitySuffix != null) {
      _result.nullabilitySuffix = nullabilitySuffix;
    }
    return _result;
  }
  factory DartInterfaceType.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory DartInterfaceType.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  DartInterfaceType clone() => DartInterfaceType()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  DartInterfaceType copyWith(void Function(DartInterfaceType) updates) => super.copyWith((message) => updates(message as DartInterfaceType)) as DartInterfaceType; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static DartInterfaceType create() => DartInterfaceType._();
  DartInterfaceType createEmptyInstance() => create();
  static $pb.PbList<DartInterfaceType> createRepeated() => $pb.PbList<DartInterfaceType>();
  @$core.pragma('dart2js:noInline')
  static DartInterfaceType getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<DartInterfaceType>(create);
  static DartInterfaceType? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get elementLocation => $_getSZ(0);
  @$pb.TagNumber(1)
  set elementLocation($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasElementLocation() => $_has(0);
  @$pb.TagNumber(1)
  void clearElementLocation() => clearField(1);

  @$pb.TagNumber(2)
  $core.List<DartType> get typeArguments => $_getList(1);

  @$pb.TagNumber(3)
  DartInterfaceType get superClass => $_getN(2);
  @$pb.TagNumber(3)
  set superClass(DartInterfaceType v) { setField(3, v); }
  @$pb.TagNumber(3)
  $core.bool hasSuperClass() => $_has(2);
  @$pb.TagNumber(3)
  void clearSuperClass() => clearField(3);
  @$pb.TagNumber(3)
  DartInterfaceType ensureSuperClass() => $_ensure(2);

  @$pb.TagNumber(4)
  $core.List<DartInterfaceType> get superInterfaceTypes => $_getList(3);

  @$pb.TagNumber(5)
  $core.List<DartInterfaceType> get superMixinTypes => $_getList(4);

  @$pb.TagNumber(6)
  DartNullabilitySuffix get nullabilitySuffix => $_getN(5);
  @$pb.TagNumber(6)
  set nullabilitySuffix(DartNullabilitySuffix v) { setField(6, v); }
  @$pb.TagNumber(6)
  $core.bool hasNullabilitySuffix() => $_has(5);
  @$pb.TagNumber(6)
  void clearNullabilitySuffix() => clearField(6);
}

class DartNeverType extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'DartNeverType', createEmptyInstance: create)
    ..hasRequiredFields = false
  ;

  DartNeverType._() : super();
  factory DartNeverType() => create();
  factory DartNeverType.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory DartNeverType.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  DartNeverType clone() => DartNeverType()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  DartNeverType copyWith(void Function(DartNeverType) updates) => super.copyWith((message) => updates(message as DartNeverType)) as DartNeverType; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static DartNeverType create() => DartNeverType._();
  DartNeverType createEmptyInstance() => create();
  static $pb.PbList<DartNeverType> createRepeated() => $pb.PbList<DartNeverType>();
  @$core.pragma('dart2js:noInline')
  static DartNeverType getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<DartNeverType>(create);
  static DartNeverType? _defaultInstance;
}

class DartVoidType extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'DartVoidType', createEmptyInstance: create)
    ..hasRequiredFields = false
  ;

  DartVoidType._() : super();
  factory DartVoidType() => create();
  factory DartVoidType.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory DartVoidType.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  DartVoidType clone() => DartVoidType()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  DartVoidType copyWith(void Function(DartVoidType) updates) => super.copyWith((message) => updates(message as DartVoidType)) as DartVoidType; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static DartVoidType create() => DartVoidType._();
  DartVoidType createEmptyInstance() => create();
  static $pb.PbList<DartVoidType> createRepeated() => $pb.PbList<DartVoidType>();
  @$core.pragma('dart2js:noInline')
  static DartVoidType getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<DartVoidType>(create);
  static DartVoidType? _defaultInstance;
}

