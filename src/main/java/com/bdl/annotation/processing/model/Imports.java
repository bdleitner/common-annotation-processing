package com.bdl.annotation.processing.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Container of imports used that allows for obtaining needed import statements and
 * for determining when FQPN are needed in type references.
 *
 * @author Ben Leitner
 */
class Imports {

  enum ReferenceType {
    NAME_ONLY,
    NESTED_NAME,
    FULLY_QUALIFIED_PATH_NAME
  }

  private final Map<TypeMetadata, ReferenceType> referenceMap;

  private Imports(Map<TypeMetadata, ReferenceType> referenceMap) {
    this.referenceMap = referenceMap;
  }

  static Imports create(Iterable<TypeMetadata> imports) {
    Multimap<String, TypeMetadata> namesToTypes = namesToTypes(imports);
    return new Imports(createReferenceMap(namesToTypes));
  }

  private static Multimap<String, TypeMetadata> namesToTypes(Iterable<TypeMetadata> imports) {
    ImmutableMultimap.Builder<String, TypeMetadata> multimap = ImmutableMultimap.builder();
    for (TypeMetadata type : imports) {
      multimap.put(type.name(), type);
    }
    return multimap.build();
  }

  private static Map<TypeMetadata, ReferenceType> createReferenceMap(Multimap<String, TypeMetadata> namesToTypes) {
    ImmutableMap.Builder<TypeMetadata, ReferenceType> referenceMap = ImmutableMap.builder();
    Map<String, Collection<TypeMetadata>> map = namesToTypes.asMap();
    for (Map.Entry<String, Collection<TypeMetadata>> entry : map.entrySet()) {
      if (entry.getValue().size() == 1) {
        TypeMetadata type = Iterables.getOnlyElement(entry.getValue());
        referenceMap.put(
            type.rawType(),
            ReferenceType.NAME_ONLY);
        continue;
      }
      // TODO: improve collision handling.
      for (TypeMetadata type : entry.getValue()) {
        referenceMap.put(type, ReferenceType.FULLY_QUALIFIED_PATH_NAME);
      }
    }
    return referenceMap.build();
  }

  List<String> getImports() {
    List<String> imports = Lists.newArrayList();
    for (Map.Entry<TypeMetadata, ReferenceType> entry : referenceMap.entrySet()) {
      TypeMetadata type = entry.getKey();
      switch (entry.getValue()) {
        case FULLY_QUALIFIED_PATH_NAME:
          // No import, FQPN must be used.
          break;
        case NESTED_NAME:
          imports.add(type.nameBuilder().addPackagePrefix().addOutermostClassName().toString());
          break;
        case NAME_ONLY:
          imports.add(type.nameBuilder()
              .addPackagePrefix()
              .addNestingPrefix()
              .addSimpleName()
              .toString());
          break;
      }
    }
    Collections.sort(imports);
    return ImmutableList.copyOf(imports);
  }

  ReferenceType reference(TypeMetadata type) {
    if (type.packageName().equals("java.lang")
        || type.packageName().isEmpty()) {
      return ReferenceType.NAME_ONLY;
    }
    return referenceMap.get(type.rawType());
  }
}
