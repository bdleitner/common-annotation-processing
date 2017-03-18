package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.util.Comparator;
import java.util.Set;

/**
 * Metadata for a field within a class.
 *
 * @author Ben Leitner
 */
@AutoValue
public abstract class FieldMetadata implements Annotatable, Comparable<FieldMetadata>, UsesTypes {

  private static final Comparator<FieldMetadata> COMPARATOR =
      Comparator.comparing((FieldMetadata field) -> field.modifiers().visibility())
          .thenComparing(FieldMetadata::name);

  public abstract TypeMetadata containingClass();

  @Override
  public abstract ImmutableList<AnnotationMetadata> annotations();

  public abstract Modifiers modifiers();

  public abstract TypeMetadata type();

  public abstract String name();

  @Override
  public Set<TypeMetadata> getAllTypes() {
    return type().getAllTypes();
  }

  public String toString(Imports imports) {
    return String.format(
        "%s%s%s%s %s",
        modifiers().visibility().prefix(),
        modifiers().isStatic() ? "static " : "",
        modifiers().isFinal() ? "final " : "",
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
    Preconditions.checkArgument(
        element.getKind() == ElementKind.FIELD,
        "element %s is not a field (has kind %s)",
        element,
        element.getKind());
    if (containingClass == null) {
      containingClass = TypeMetadata.fromElement(element.getEnclosingElement());
    }
    Builder field = builder().containingClass(containingClass);
    for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
      field.addAnnotation(AnnotationMetadata.fromType(annotation));
    }
    Modifiers.Builder modifiersBuilder = Modifiers.builder()
        .setVisibility(Visibility.forElement(element));
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(Modifier.STATIC)) {
      modifiersBuilder.makeStatic();
    }
    if (modifiers.contains(Modifier.FINAL)) {
      modifiersBuilder.makeFinal();
    }

    field
        .modifiers(modifiersBuilder.build())
        .name(element.getSimpleName().toString())
        .type(TypeMetadata.fromType(element.asType()));
    return field.build();
  }

  public static Builder builder() {
    return new AutoValue_FieldMetadata.Builder()
        .modifiers(Modifiers.builder().build());
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder containingClass(TypeMetadata containingClass);

    abstract ImmutableList.Builder<AnnotationMetadata> annotationsBuilder();

    public abstract Builder modifiers(Modifiers modifiers);

    public abstract Builder type(TypeMetadata type);

    public abstract Builder name(String name);

    public Builder addAnnotation(AnnotationMetadata annotation) {
      annotationsBuilder().add(annotation);
      return this;
    }

    public abstract FieldMetadata build();
  }
}
