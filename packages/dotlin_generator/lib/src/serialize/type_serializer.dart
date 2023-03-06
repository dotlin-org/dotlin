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

import 'dart:collection';

import 'package:analyzer/dart/element/element.dart';
import 'package:analyzer/dart/element/nullability_suffix.dart';
import 'package:analyzer/dart/element/type.dart' as og;
import 'package:analyzer/dart/element/type.dart' hide DartType;
import 'package:dartx/dartx_io.dart';
import 'package:dotlin_generator/src/serialize/element_serializer.dart';
import 'package:dotlin_generator/src/serialize/util.dart';
import 'package:protobuf/protobuf.dart';

import '../generated/elements.pb.dart';

class DartTypeSerializer {
  /// This cache is used to prevent stack overflows when dealing with
  /// circular types (e.g. `String implements Comparable<String>`).
  final _typeCache = Queue<DartInterfaceType>();

  late final DartElementSerializer elementSerializer;

  DartInterfaceType? _cachedTypeOf(Element element) {
    final location = element.encodedLocation;
    return _typeCache.lastOrNullWhere((t) => t.elementLocation == location);
  }

  T _withCached<T>(DartInterfaceType type, T Function() block) {
    _typeCache.addLast(type);
    final result = block();
    _typeCache.removeLast();
    return result;
  }

  DartType serializeType(og.DartType type) {
    late final GeneratedMessage proto;

    if (type is DynamicType) {
      proto = serializeDynamicType(type);
    } else if (type is FunctionType) {
      proto = serializeFunctionType(type);
    } else if (type is NeverType) {
      proto = serializeNeverType(type);
    } else if (type is InterfaceType) {
      proto = serializeInterfaceType(type);
    } else if (type is RecordType) {
      // TODO
      proto = DartNeverType();
    } else if (type is TypeParameterType) {
      proto = serializeTypeParameterType(type);
    } else if (type is VoidType) {
      proto = serializeVoidType(type);
    } else {
      throw UnsupportedError("Unsupported DartType: $this");
    }

    return polymorphicOf(DartType.new, proto);
  }

  late final _dynamicType = DartDynamicType();

  DartDynamicType serializeDynamicType(DynamicType type) => _dynamicType;

  DartFunctionType serializeFunctionType(FunctionType type) =>
      DartFunctionType(
          parameters: elementSerializer.serializeParameters(type.parameters),
          typeParameters: elementSerializer.serializeTypeParameters(
            type.typeFormals,
          ),
          returnType: serializeType(type.returnType),
          nullabilitySuffix: serializeNullabilitySuffix(type.nullabilitySuffix),
      );

  DartTypeParameterType serializeTypeParameterType(TypeParameterType type) =>
      DartTypeParameterType(
        elementLocation: type.element.encodedLocation,
        bound: serializeType(type.bound),
        nullabilitySuffix: serializeNullabilitySuffix(type.nullabilitySuffix)
      );

  late final _neverType = DartNeverType();

  DartNeverType serializeNeverType(NeverType type) => _neverType;

  DartInterfaceType serializeInterfaceType(InterfaceType type) {
    final cached = _cachedTypeOf(type.element);
    if (cached != null) return cached;

    final proto = DartInterfaceType(
      elementLocation: type.element.encodedLocation,
      nullabilitySuffix: serializeNullabilitySuffix(type.nullabilitySuffix),
    );

    _withCached(proto, () {
      proto
        ..typeArguments.addAll(serializeTypes(type.typeArguments))
        ..superInterfaceTypes.addAll(
          serializeInterfaceTypes(type.interfaces),
        )
        ..superMixinTypes.addAll(
          serializeInterfaceTypes(type.mixins),
        );

      if (type.superclass != null) {
        serializeInterfaceType(type.superclass!);
      }
    });

    return proto;
  }

  DartVoidType serializeVoidType(VoidType type) => DartVoidType();

  DartNullabilitySuffix serializeNullabilitySuffix(NullabilitySuffix suffix) {
    switch (suffix) {
      case NullabilitySuffix.question:
        return DartNullabilitySuffix.QUESTION_MARK;
      case NullabilitySuffix.star:
        return DartNullabilitySuffix.STAR;
      case NullabilitySuffix.none:
        return DartNullabilitySuffix.NONE;
    }
  }
}

extension DartMultiTypeSerializer on DartTypeSerializer {
  Iterable<DartType> serializeTypes(Iterable<og.DartType> types) =>
      types.map((t) => serializeType(t));

  Iterable<DartInterfaceType> serializeInterfaceTypes(
      Iterable<InterfaceType> types,) =>
      types.map((t) => serializeInterfaceType(t));
}