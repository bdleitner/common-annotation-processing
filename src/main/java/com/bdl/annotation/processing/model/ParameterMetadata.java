package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import java.util.Comparator;
import java.util.Set;

/**
 * Data object for parameters.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class ParameterMetadata implements GeneratesImports {
  static final Comparator<ImmutableList<ParameterMetadata>> IMMUTABLE_LIST_COMPARATOR
      = Comparators.forLists(new Function<ParameterMetadata, TypeMetadata>() {
    @Override
    public TypeMetadata apply(ParameterMetadata input) {
      return input.type();
    }
  });

  private String name;

  abstract TypeMetadata type();

  String name() {
    return name;
  }

  @Override
  public Set<TypeMetadata> getImports() {
    return type().getImports();
  }

  static ParameterMetadata of(TypeMetadata type, String name) {
    ParameterMetadata metadata = new AutoValue_ParameterMetadata(type);
    metadata.name = name;
    return metadata;
  }

  @Override
  public String toString() {
    return String.format("%s %s",
        type().nameBuilder()
            .addPackagePrefix()
            .addNestingPrefix()
            .addSimpleName()
            .addSimpleParams()
            .toString(),
        name());
  }
}
