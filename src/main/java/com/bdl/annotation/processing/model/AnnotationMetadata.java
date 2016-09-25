package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * Encapsulation of Metadata information for an Annotation reference.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class AnnotationMetadata implements GeneratesImports {

  /** The type of the annotation. */
  abstract TypeMetadata type();

  abstract ImmutableMap<MethodMetadata, ValueMetadata> values();

  @Override
  public Set<TypeMetadata> getImports() {
    ImmutableSet.Builder<TypeMetadata> imports = ImmutableSet.builder();
    imports.addAll(type().getImports());
    for (Map.Entry<MethodMetadata, ValueMetadata> entry : values().entrySet()) {
      imports.addAll(entry.getKey().getImports());
      imports.addAll(entry.getValue().type().getImports());
    }
    return imports.build();
  }

  static AnnotationMetadata fromType(AnnotationMirror mirror) {
    Builder metadata = AnnotationMetadata.builder();
    metadata.setType(TypeMetadata.fromType(mirror.getAnnotationType()));
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
        : mirror.getElementValues().entrySet()) {
      MethodMetadata method = MethodMetadata.fromMethod(entry.getKey());
      metadata.putValue(
          method,
          ValueMetadata.create(method.type(), entry.getValue().getValue().toString()));
    }
    return metadata.build();
  }

  private static String valueFor(Object value) {
    return value instanceof Element
        ? ((Element) value).getSimpleName().toString()
        : value.toString();
  }

  static Builder builder() {
    return new AutoValue_AnnotationMetadata.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setType(TypeMetadata type);
    abstract ImmutableMap.Builder<MethodMetadata, ValueMetadata> valuesBuilder();

    Builder putValue(MethodMetadata metadata, ValueMetadata object) {
      valuesBuilder().put(metadata, object);
      return this;
    }

    abstract AnnotationMetadata build();
  }
}
