package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Comparator;
import java.util.Set;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

/**
 * Metadata for a field within a class.
 *
 * @author Ben Leitner
 */
@AutoValue
public abstract class FieldMetadata implements Annotatable, Comparable<FieldMetadata>, UsesTypes {

  private static final Comparator<FieldMetadata> COMPARATOR = Comparator
      .comparing(FieldMetadata::visibility)
      .thenComparing(FieldMetadata::name);

  public abstract TypeMetadata containingClass();

  @Override
  public abstract ImmutableList<AnnotationMetadata> annotations();

  public abstract Visibility visibility();

  public abstract boolean isStatic();

  public abstract boolean isFinal();

  public abstract TypeMetadata type();

  public abstract String name();

  @Override
  public Set<TypeMetadata> getAllTypes() {
    ImmutableSet.Builder<TypeMetadata> imports = ImmutableSet.builder();
    for (AnnotationMetadata annotation : annotations()) {
      imports.addAll(annotation.getAllTypes());
    }
    imports.addAll(type().getAllTypes());
    return imports.build();
  }

  public String toString(Imports imports) {
    return String.format("%s%s%s%s %s",
        visibility().prefix(),
        isStatic() ? "static " : "",
        isFinal() ? "final " : "",
        type().toString(imports),
        name());
  }

  @Override
  public String toString() {
    return toString(Imports.empty());
  }

  @Override
  public int compareTo(FieldMetadata that) {
    return COMPARATOR.compare(this, that);
  }

  abstract Builder toBuilder();

  public static FieldMetadata from(Element element) {
    return from(null, element);
  }

  static FieldMetadata from(@Nullable TypeMetadata containingClass, Element element) {
    Preconditions.checkArgument(element.getKind() == ElementKind.FIELD,
        "element %s is not a field (has kind %s)", element, element.getKind());
    if (containingClass == null) {
      containingClass = TypeMetadata.fromElement(element.getEnclosingElement());
    }
    Builder field = builder()
        .containingClass(containingClass);
    for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
      field.addAnnotation(AnnotationMetadata.fromType(annotation));
    }
    field.visibility(Visibility.forElement(element))
        .name(element.getSimpleName().toString())
        .type(TypeMetadata.fromType(element.asType()));
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(Modifier.STATIC)) {
      field.isStatic(true);
    }
    if (modifiers.contains(Modifier.FINAL)) {
      field.isFinal(true);
    }
    return field.build();
  }

  public static Builder builder() {
    return new AutoValue_FieldMetadata.Builder()
        .isStatic(false)
        .isFinal(false);
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder containingClass(TypeMetadata containingClass);

    abstract ImmutableList.Builder<AnnotationMetadata> annotationsBuilder();

    public abstract Builder visibility(Visibility visibility);

    public abstract Builder isStatic(boolean isStatic);

    public abstract Builder isFinal(boolean isFinal);

    public abstract Builder type(TypeMetadata type);

    public abstract Builder name(String name);

    public Builder addAnnotation(AnnotationMetadata annotation) {
      annotationsBuilder().add(annotation);
      return this;
    }

    public abstract FieldMetadata build();
  }
}
