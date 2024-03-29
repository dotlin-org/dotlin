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

import 'package:analyzer/dart/element/element.dart';
import 'package:dartx/dartx_io.dart';
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
    var libPath = libUri.path;

    // On Windows, paths are separated with a forward slash instead of
    // a black slash, fix that.
    if (Platform.isWindows) {
      if (libPath.startsWith('/')) {
        libPath = libPath.replaceFirst('/', '');
      }
      libPath = libPath.replaceAll('/', r'\');
    }

    switch (libUri.scheme) {
      // If "pub get" has not been run, library.identifier will be a
      // an absolute path file URI.
      case 'file':
        libPath = path.relative(libPath, from: packagePath);
        break;
      // Otherwise, it's a package URI.
      case 'package':
        final separator = Platform.isWindows ? r'\\' : '/';
        libPath = libPath.replaceFirst(RegExp('^[A-Za-z_]+$separator'), '');
        break;
      default:
        throw StateError("Unexpected library uri scheme: ${libUri.scheme}");
    }

    return DartLibraryElement(
      location: library.encodedLocation,
      path: libPath,
      exports: library.libraryExports.map((e) => serializeLibraryExport(e)),
      units: library.units.map((u) => serializeCompilationUnit(u)),
    );
  }

  DartLibraryExportElement serializeLibraryExport(
    LibraryExportElement export,
  ) =>
      DartLibraryExportElement(
        location: export.encodedLocation,
        exportLocation: export.exportedLibrary!.encodedLocation,
        show: export.combinators.whereType<ShowElementCombinator>().flatMap(
              (c) => c.shownNames,
            ),
        hide: export.combinators.whereType<HideElementCombinator>().flatMap(
              (c) => c.hiddenNames,
            ),
      );

  DartCompilationUnitElement serializeCompilationUnit(
    CompilationUnitElement unit,
  ) =>
      DartCompilationUnitElement(
        location: unit.encodedLocation,
        classes: unit.classes.map((c) => serializeClass(c)),
        enums: unit.enums.map((e) => serializeEnum(e)),
        functions: serializeFunctions(unit.functions),
        properties: serializePropertyInducingElements(unit.topLevelVariables)
            .followedBy(
          serializePropertyInducingElements(
            unit.accessors.map((a) => a.variable),
          ),
        ),
      );

  DartClassElement serializeClass(ClassElement c) => DartClassElement(
        location: c.encodedLocation,
        name: c.name,
        isAbstract: c.isAbstract,
        properties: serializePropertyInducingElements(c.fields),
        constructors: serializeConstructors(c.constructors),
        methods: serializeFunctions(c.methods),
        typeParameters: serializeTypeParameters(c.typeParameters),
        superType: _serializeSuperTypeOf(c),
        superInterfaceTypes: _serializeSuperInterfaceTypesOf(c),
        superMixinTypes: _serializeSuperMixinTypesOf(c),
      );

  DartEnumElement serializeEnum(EnumElement c) => DartEnumElement(
        location: c.encodedLocation,
        name: c.name,
        properties: serializePropertyInducingElements(c.fields),
        constructors: serializeConstructors(c.constructors),
        methods: serializeFunctions(c.methods),
        typeParameters: serializeTypeParameters(c.typeParameters),
        superType: _serializeSuperTypeOf(c),
        superInterfaceTypes: _serializeSuperInterfaceTypesOf(c),
        superMixinTypes: _serializeSuperMixinTypesOf(c),
      );

  DartConstructorElement serializeConstructor(ConstructorElement c) =>
      DartConstructorElement(
        location: c.encodedLocation,
        name: c.name,
        isConst: c.isConst,
        isFactory: c.isFactory,
        type: _typeSerializer.serializeFunctionType(c.type),
        parameters: serializeParameters(c.parameters),
      );

  DartPropertyElement serializePropertyInducingElement(
    PropertyInducingElement prop,
  ) =>
      DartPropertyElement(
        location: prop.encodedLocation,
        name: prop.name,
        isAbstract: prop is FieldElement ? prop.isAbstract : false,
        isCovariant: prop is FieldElement ? prop.isCovariant : false,
        isConst: prop.isConst,
        isFinal: prop.isConst ? true : prop.isFinal,
        isLate: prop.isLate,
        // Enum constants should not be considered static.
        isStatic: prop is FieldElement
            ? !prop.isEnumConstant && prop.isStatic
            : prop.isStatic,
        isSynthetic: prop.isSynthetic,
        isEnumConstant: prop is FieldElement ? prop.isEnumConstant : false,
        type: _typeSerializer.serializeType(prop.type),
        getter: serializePropertyAccessor(prop.getter),
        setter: serializePropertyAccessor(prop.setter),
      );

  DartPropertyAccessorElement? serializePropertyAccessor(
    PropertyAccessorElement? accessor,
  ) =>
      accessor != null
          ? DartPropertyAccessorElement(
              location: accessor.encodedLocation,
              name: accessor.isSetter
                  ? accessor.name.reversed.replaceFirst('=', '').reversed
                  : accessor.name,
              type: _typeSerializer.serializeFunctionType(accessor.type),
              isAsync: accessor.isAsynchronous,
              isGenerator: accessor.isGenerator,
              isSynthetic: accessor.isSynthetic,
              parameters: serializeParameters(accessor.parameters),
              typeParameters: serializeTypeParameters(accessor.typeParameters),
              correspondingPropertyLocation: accessor.variable.encodedLocation,
            )
          : null;

  DartFunctionElement serializeFunction(ExecutableElement fun) =>
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

  DartInterfaceType? _serializeSuperTypeOf(InterfaceElement interface) =>
      interface.supertype != null
          ? _typeSerializer.serializeInterfaceType(interface.supertype!)
          : null;

  Iterable<DartInterfaceType> _serializeSuperInterfaceTypesOf(
    InterfaceElement interface,
  ) =>
      _typeSerializer.serializeInterfaceTypes(interface.interfaces);

  Iterable<DartInterfaceType> _serializeSuperMixinTypesOf(
    InterfaceElement interface,
  ) =>
      _typeSerializer.serializeInterfaceTypes(interface.mixins);
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

  Iterable<DartPropertyElement> serializePropertyInducingElements(
    Iterable<PropertyInducingElement> properties,
  ) =>
      properties.map((p) => serializePropertyInducingElement(p));

  Iterable<DartFunctionElement> serializeFunctions(
    Iterable<ExecutableElement> funs,
  ) =>
      funs.map((f) => serializeFunction(f));

  Iterable<DartConstructorElement> serializeConstructors(
    Iterable<ConstructorElement> ctors,
  ) =>
      ctors.map((ctor) => serializeConstructor(ctor));
}