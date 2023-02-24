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

import 'package:analyzer/dart/element/element.dart';
import 'package:dotlin_generator/src/generated/elements.pb.dart';
import 'package:dotlin_generator/src/serialize/type_serializer.dart';
import 'package:dotlin_generator/src/serialize/util.dart';

import 'package:path/path.dart' as path;

class DartElementSerializer {
  final String packagePath;

  final DartTypeSerializer _typeSerializer;

  DartElementSerializer(this.packagePath, this._typeSerializer) {
    _typeSerializer.elementSerializer = this;
  }

  DartLibraryElement serializeLibrary(LibraryElement library) {
    final libUri = Uri.parse(library.identifier);
    // ignore: unnecessary_string_escapes
    var libPath = libUri.path;

    switch (libUri.scheme) {
      // If "pub get" has not been run, library.identifier will be a
      // an absolute path file URI.
      case 'file':
        libPath = path.relative(libPath, from: packagePath);
        break;
      // Otherwise, it's a package URI.
      case "package":
        libPath = libPath.replaceFirst(RegExp("^[A-Za-z_]+\/"), "");
        break;
      default:
        throw StateError("Unexpected library uri scheme: ${libUri.scheme}");
    }

    return DartLibraryElement(
      location: library.encodedLocation,
      path: libPath,
      units: library.units.map((u) => serializeCompilationUnit(u)),
    );
  }

  DartCompilationUnitElement serializeCompilationUnit(
    CompilationUnitElement unit,
  ) =>
      DartCompilationUnitElement(
        location: unit.encodedLocation,
        classes: unit.classes.map((c) => serializeClass(c)),
        functions: unit.functions.map((f) => serializeFunction(f)),
      );

  DartClassElement serializeClass(ClassElement c) => DartClassElement(
        location: c.encodedLocation,
        name: c.name,
        isAbstract: c.isAbstract,
        fields: c.fields.map((f) => serializeField(f)),
        constructors: c.constructors.map((ctor) => serializeConstructor(ctor)),
      );

  DartConstructorElement serializeConstructor(ConstructorElement c) =>
      DartConstructorElement(
        location: c.encodedLocation,
        name: c.name,
        isConst: c.isConst,
        type: _typeSerializer.serializeFunctionType(c.type),
        parameters: serializeParameters(c.parameters),
      );

  DartFieldElement serializeField(FieldElement field) => DartFieldElement(
        location: field.encodedLocation,
        name: field.name,
        isAbstract: field.isAbstract,
        isCovariant: field.isCovariant,
        isConst: field.isConst,
        isFinal: field.isFinal,
        isLate: field.isLate,
        isStatic: field.isStatic,
        type: _typeSerializer.serializeType(field.type),
      );

  DartFunctionElement serializeFunction(FunctionElement fun) =>
      DartFunctionElement(
        location: fun.encodedLocation,
        name: fun.name,
        isAsync: fun.isAsynchronous,
        isGenerator: fun.isGenerator,
        isAbstract: fun.isAbstract,
        isOperator: fun.isOperator,
        isStatic: fun.isStatic,
        parameters: serializeParameters(fun.parameters),
        typeParameters: serializeTypeParameters(fun.typeParameters),
        type: _typeSerializer.serializeFunctionType(fun.type),
      );

  DartParameterElement serializeParameter(ParameterElement param) =>
      DartParameterElement(
        location: param.encodedLocation,
        name: param.name,
        type: _typeSerializer.serializeType(param.type),
        isCovariant: param.isCovariant,
        isNamed: param.isNamed,
        isRequired: param.isRequired,
        fieldLocation: param is FieldFormalParameterElement
            ? param.field?.encodedLocation
            : null,
        superConstructorParameterLocation: param is SuperFormalParameterElement
            ? param.superConstructorParameter?.encodedLocation
            : null,
        defaultValueCode: param.defaultValueCode,
      );

  DartTypeParameterElement serializeTypeParameter(TypeParameterElement param) =>
      DartTypeParameterElement(
        location: param.encodedLocation,
        name: param.name,
        bound: param.bound != null
            ? _typeSerializer.serializeType(param.bound!)
            : null,
      );
}

extension DartMultiElementSerializer on DartElementSerializer {
  Iterable<DartParameterElement> serializeParameters(
    Iterable<ParameterElement> params,
  ) =>
      params.map((p) => serializeParameter(p));

  Iterable<DartTypeParameterElement> serializeTypeParameters(
    Iterable<TypeParameterElement> params,
  ) =>
      params.map((p) => serializeTypeParameter(p));
}
