package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * Encapsulation of constructor metadata.
 *
 * @author Ben Leitner
 */
@AutoValue
public abstract class ConstructorMetadata implements Comparable<ConstructorMetadata>, UsesTypes {

  public abstract Visibility visibility();

  public abstract ImmutableList<ParameterMetadata> parameters();

  @Override
  public Set<TypeMetadata> getAllTypes() {
    ImmutableSet.Builder<TypeMetadata> imports = ImmutableSet.builder();
    for (ParameterMetadata param : parameters()) {
      imports.addAll(param.getAllTypes());
    }
    return imports.build();
  }

  @Override
  public int compareTo(ConstructorMetadata that) {
    return ComparisonChain.start()
        .compare(visibility().ordinal(), that.visibility().ordinal())
        .compare(parameters(), that.parameters(), ParameterMetadata.IMMUTABLE_LIST_COMPARATOR)
        .result();
  }

  public String toString(String className) {
    return String.format("%s%s(%s)", visibility().prefix(), className, Joiner.on(", ").join(parameters()));
  }

  @Override
  public String toString() {
    return toString("Constructor");
  }

  public static ConstructorMetadata fromConstructor(Element element) {
    Preconditions.checkArgument(element.getKind() == ElementKind.CONSTRUCTOR,
        "Element %s is not a constructor.", element);

    ExecutableElement executable = (ExecutableElement) element;
    Builder constructor = builder()
        .visibility(Visibility.forElement(element));

    for (VariableElement parameter : executable.getParameters()) {
      constructor.addParameter(
          ParameterMetadata.of(
              TypeMetadata.fromType(parameter.asType()),
              parameter.getSimpleName().toString()));
    }

    return constructor.build();
  }

  public static Builder builder() {
    return new AutoValue_ConstructorMetadata.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder visibility(Visibility visibility);

    abstract ImmutableList.Builder<ParameterMetadata> parametersBuilder();

    public Builder addParameter(ParameterMetadata parameter) {
      parametersBuilder().add(parameter);
      return this;
    }

    public abstract ConstructorMetadata build();
  }
}
