package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Encapsulation of metadata from the inheritance of an abstract class or interface.
 *
 * @author Ben Leitner
 */
@AutoValue
public abstract class InheritanceMetadata implements UsesTypes {

  private ImmutableList<FieldMetadata> allFields;
  private ImmutableList<MethodMetadata> allMethods;

  /** The type parameters given in the {@code extends} or {@code implements} clause. */
  public abstract ImmutableList<TypeMetadata> inheritanceParams();

  public abstract ClassMetadata classMetadata();

  @Override
  public Set<TypeMetadata> getAllTypes() {
    return classMetadata().getAllTypes();
  }

  ImmutableList<FieldMetadata> getAllFields() {
    if (allFields == null) {
      final Map<String, String> paramNamesMap = getParamNamesMap();
      allFields =
          ImmutableList.copyOf(
              classMetadata()
                  .getAllFields()
                  .stream()
                  .map(
                      input ->
                          input
                              .toBuilder()
                              .containingClass(
                                  input.containingClass().convertTypeParams(paramNamesMap))
                              .type(input.type().convertTypeParams(paramNamesMap))
                              .build())
                  .collect(Collectors.toList()));
    }
    return allFields;
  }

  ImmutableList<MethodMetadata> getAllMethods() {
    if (allMethods == null) {
      final Map<String, String> paramNamesMap = getParamNamesMap();
      allMethods =
          ImmutableList.copyOf(
              classMetadata()
                  .getAllMethods()
                  .stream()
                  .map(input -> input.convertTypeParameters(paramNamesMap))
                  .collect(Collectors.toList()));
    }
    return allMethods;
  }

  private Map<String, String> getParamNamesMap() {
    ImmutableMap.Builder<String, String> paramNamesMap = ImmutableMap.builder();
    int i = 0;
    for (TypeMetadata typeParam : inheritanceParams()) {
      paramNamesMap.put(classMetadata().type().params().get(i).name(), typeParam.name());
      i++;
    }
    return paramNamesMap.build();
  }

  public static InheritanceMetadata fromType(DeclaredType type) {
    Builder metadata = InheritanceMetadata.builder();
    for (TypeMirror typeParam : type.getTypeArguments()) {
      metadata.addInheritanceParam(TypeMetadata.fromType(typeParam));
    }
    metadata.setClassMetadata(ClassMetadata.fromElement(type.asElement()));
    return metadata.build();
  }

  static Builder builder() {
    return new AutoValue_InheritanceMetadata.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract ImmutableList.Builder<TypeMetadata> inheritanceParamsBuilder();

    abstract Builder setClassMetadata(ClassMetadata classMetadata);

    Builder addInheritanceParam(TypeMetadata type) {
      inheritanceParamsBuilder().add(type);
      return this;
    }

    abstract InheritanceMetadata autoBuild();

    InheritanceMetadata build() {
      InheritanceMetadata metadata = autoBuild();
      Preconditions.checkState(
          metadata.inheritanceParams().size() == metadata.classMetadata().type().params().size(),
          "Cannot inherit %s with type params <%s>, the sizes do not match.",
          metadata.classMetadata().type().toString(Imports.empty(), true),
          metadata
              .inheritanceParams()
              .stream()
              .map(TypeMetadata::toString)
              .collect(Collectors.joining(", ")));

      return metadata;
    }
  }
}
