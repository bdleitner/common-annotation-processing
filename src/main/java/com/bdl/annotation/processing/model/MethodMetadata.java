package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

/**
 * Holder of metadata for a method ExecutableElement.
 *
 * @author Ben Leitner
 */
@AutoValue
public abstract class MethodMetadata implements Comparable<MethodMetadata>, UsesTypes, Annotatable {

  private static final Comparator<MethodMetadata> COMPARATOR =
      comparing((MethodMetadata method) -> method.modifiers().visibility())
          .thenComparing(MethodMetadata::name)
          .thenComparing(MethodMetadata::parameters, Comparators.forLists(ParameterMetadata::type));

  @Override
  public abstract ImmutableList<AnnotationMetadata> annotations();

  public abstract Modifiers modifiers();

  public abstract ImmutableList<TypeMetadata> typeParameters();

  public abstract String name();

  public abstract TypeMetadata type();

  public abstract ImmutableList<ParameterMetadata> parameters();

  MethodMetadata convertTypeParameters(Map<String, String> paramNameMap) {
    paramNameMap = augmentParamNameMap(paramNameMap);
    Builder metadata =
        MethodMetadata.builder()
            .setModifiers(modifiers())
            .setName(name())
            .setType(type().convertTypeParams(paramNameMap));
    for (TypeMetadata typeParam : typeParameters()) {
      metadata.addTypeParameter(typeParam.convertTypeParams(paramNameMap));
    }
    for (ParameterMetadata param : parameters()) {
      metadata.addParameter(
          ParameterMetadata.of(param.type().convertTypeParams(paramNameMap), param.name()));
    }
    return metadata.build();
  }

  /**
   * Augments the parameter name map to ensure that type parameters specified for this method are
   * not overridden.
   */
  private Map<String, String> augmentParamNameMap(Map<String, String> paramNameMap) {
    NameIterator names = new NameIterator();
    ImmutableMap.Builder<String, String> augmented =
        ImmutableMap.<String, String>builder().putAll(paramNameMap);

    for (TypeMetadata typeParam : typeParameters()) {
      String name = nextClearName(paramNameMap, names, typeParam.name());
      augmented.put(typeParam.name(), name);
    }
    return augmented.build();
  }

  private String nextClearName(Map<String, String> paramNameMap, NameIterator names, String name) {
    String candidate = name;
    while (paramNameMap.containsKey(candidate) || paramNameMap.containsValue(candidate)) {
      candidate = names.next();
    }
    return candidate;
  }

  @Override
  public int compareTo(MethodMetadata that) {
    return COMPARATOR.compare(this, that);
  }

  @Override
  public Set<TypeMetadata> getAllTypes() {
    ImmutableSet.Builder<TypeMetadata> imports = ImmutableSet.builder();
    for (TypeMetadata typeParam : typeParameters()) {
      imports.addAll(typeParam.getAllTypes());
    }
    imports.addAll(type().getAllTypes());
    for (ParameterMetadata param : parameters()) {
      imports.addAll(param.getAllTypes());
    }
    return imports.build();
  }

  private String typeParametersPrefix(Imports imports) {
    if (typeParameters().isEmpty()) {
      return "";
    }
    return String.format(
        "<%s> ",
        typeParameters()
            .stream()
            .map(input -> input.toString(imports, true))
            .collect(Collectors.joining(", ")));
  }

  public String toString(Imports imports) {
    return String.format(
        "%s%s%s%s %s(%s)",
        modifiers().visibility().prefix(),
        modifiers().isAbstract() ? "abstract " : "",
        typeParametersPrefix(imports),
        type().toString(imports),
        name(),
        parameters()
            .stream()
            .map((param) -> param.toString(imports))
            .collect(Collectors.joining(", ")));
  }

  public MethodMetadata asAbstract() {
    return toBuilder().setModifiers(modifiers().makeAbstract()).build();
  }

  public MethodMetadata asConcrete() {
    return toBuilder().setModifiers(modifiers().toBuilder().setIsAbstract(false).build()).build();
  }

  public MethodMetadata withoutAnnotations() {
    return toBuilder().setAnnotations(ImmutableList.of()).build();
  }

  @Override
  public String toString() {
    return toString(Imports.empty());
  }

  abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_MethodMetadata.Builder()
        .setModifiers(Modifiers.visibility(Visibility.PACKAGE_LOCAL));
  }

  static MethodMetadata fromMethod(ExecutableElement element) {
    Preconditions.checkArgument(
        element.getKind() == ElementKind.METHOD, "Element %s is not a method.", element);
    Modifiers modifiers = Modifiers.visibility(Visibility.forElement(element));
    if (element.getModifiers().contains(Modifier.ABSTRACT)) {
      modifiers = modifiers.makeAbstract();
    }
    Builder metadata =
        builder()
            .setModifiers(modifiers)
            .setType(TypeMetadata.fromType(element.getReturnType()))
            .setName(element.getSimpleName().toString());

    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      metadata.addAnnotation(AnnotationMetadata.fromType(annotationMirror));
    }

    for (TypeParameterElement typeParam : element.getTypeParameters()) {
      metadata.addTypeParameter(TypeMetadata.fromElement(typeParam));
    }

    for (VariableElement parameter : element.getParameters()) {
      metadata.addParameter(
          ParameterMetadata.of(
              TypeMetadata.fromType(parameter.asType()), parameter.getSimpleName().toString()));
    }

    return metadata.build();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    abstract Builder setAnnotations(ImmutableList<AnnotationMetadata> annotations);

    abstract ImmutableList.Builder<AnnotationMetadata> annotationsBuilder();

    public abstract Builder setModifiers(Modifiers modifiers);

    abstract ImmutableList.Builder<TypeMetadata> typeParametersBuilder();

    public abstract Builder setName(String name);

    public abstract Builder setType(TypeMetadata Type);

    abstract ImmutableList.Builder<ParameterMetadata> parametersBuilder();

    public Builder addAnnotation(AnnotationMetadata metadata) {
      annotationsBuilder().add(metadata);
      return this;
    }

    public Builder addTypeParameter(TypeMetadata metadata) {
      Preconditions.checkArgument(
          metadata.isTypeParameter(), "Cannot add %s as a type parameter.", metadata);
      typeParametersBuilder().add(metadata);
      return this;
    }

    public Builder addParameter(ParameterMetadata parameter) {
      parametersBuilder().add(parameter);
      return this;
    }

    public abstract MethodMetadata build();
  }

  private static class NameIterator implements Iterator<String> {

    private String current;

    @Override
    public boolean hasNext() {
      return true;
    }

    private String nextString(String current) {
      if (current == null || current.length() == 0) {
        return "A";
      }
      char ch = current.charAt(current.length() - 1);
      String substring = current.substring(0, current.length() - 1);
      if (ch < 'Z') {
        return substring + String.valueOf((char) (ch + 1));
      }
      return nextString(substring) + "A";
    }

    @Override
    public String next() {
      current = nextString(current);
      return current;
    }
  }
}
