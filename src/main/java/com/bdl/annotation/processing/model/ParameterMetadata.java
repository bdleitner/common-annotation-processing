package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;

/**
 * Data object for parameters.
 *
 * @author Ben Leitner
 */
@AutoValue
public abstract class ParameterMetadata implements UsesTypes {
  static final Comparator<ImmutableList<ParameterMetadata>> IMMUTABLE_LIST_COMPARATOR
      = Comparators.forLists(new Function<ParameterMetadata, TypeMetadata>() {
    @Override
    public TypeMetadata apply(ParameterMetadata input) {
      return input.type();
    }
  });

  private String name;

  public abstract TypeMetadata type();

  public String name() {
    return name;
  }

  @Override
  public Set<TypeMetadata> getAllTypes() {
    return type().getAllTypes();
  }

  public static ParameterMetadata of(TypeMetadata type, String name) {
    ParameterMetadata metadata = new AutoValue_ParameterMetadata(type);
    metadata.name = name;
    return metadata;
  }

  @Override
  public String toString() {
    return toString(Imports.empty());
  }

  public String toString(Imports imports) {
    return String.format("%s %s",
        type().reference(imports),
        name());
  }
}
