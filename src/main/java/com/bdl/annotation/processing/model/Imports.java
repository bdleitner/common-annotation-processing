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
public class Imports {

  public enum ReferenceType {
    NAME_ONLY,
    NESTED_NAME,
    FULLY_QUALIFIED_PATH_NAME
  }

  private final String packageName;
  private final Map<TypeMetadata, ReferenceType> referenceMap;

  private Imports(String packageName, Map<TypeMetadata, ReferenceType> referenceMap) {
    this.packageName = packageName;
    this.referenceMap = referenceMap;
  }

  public static Imports create(String packageName, Iterable<TypeMetadata> imports) {
    Multimap<String, TypeMetadata> namesToTypes = namesToTypes(imports);
    return new Imports(packageName, createReferenceMap(namesToTypes));
  }

  public static Imports empty() {
    return create(null, ImmutableList.of());
  }

  private static Multimap<String, TypeMetadata> namesToTypes(Iterable<TypeMetadata> imports) {
    ImmutableMultimap.Builder<String, TypeMetadata> multimap = ImmutableMultimap.builder();
    for (TypeMetadata type : imports) {
      if (neverNeedsImport(type)) {
        continue;
      }
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

  private static boolean neverNeedsImport(TypeMetadata type) {
    return type.isTypeParameter()
        || type.packageName().equals("java.lang")
        || (type.packageName().isEmpty()
        && type.nestingPrefix().isEmpty()
        && type.name().equals(type.name().toLowerCase()));
  }

  public List<String> getImports() {
    List<String> imports = Lists.newArrayList();
    for (Map.Entry<TypeMetadata, ReferenceType> entry : referenceMap.entrySet()) {
      TypeMetadata type = entry.getKey();
      if (type.packageName().equals(packageName)) {
        continue;
      }
      switch (entry.getValue()) {
        case FULLY_QUALIFIED_PATH_NAME:
          // No import, FQPN must be used.
          break;
        case NESTED_NAME:
          imports.add(packagePrefix(type) + type.outerClassNames().get(type.outerClassNames().size() - 1));
          break;
        case NAME_ONLY:
          imports.add(packagePrefix(type) + type.nestingPrefix() + type.name());
          break;
      }
    }
    Collections.sort(imports);
    return ImmutableList.copyOf(imports);
  }

  private String packagePrefix(TypeMetadata type) {
    return type.packageName().isEmpty()
        ? ""
        : type.packageName() + ".";
  }

  public ReferenceType reference(TypeMetadata type) {
    if (neverNeedsImport(type)) {
      return ReferenceType.NAME_ONLY;
    }
    if (type.packageName().equals(packageName)) {
      return type.outerClassNames().isEmpty()
          ? ReferenceType.NAME_ONLY
          : ReferenceType.NESTED_NAME;
    }
    ReferenceType referenceType = referenceMap.get(type.rawType());
    return referenceType == null
        ? ReferenceType.FULLY_QUALIFIED_PATH_NAME
        : referenceType;
  }
}
