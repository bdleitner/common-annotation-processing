package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

/**
 * Encapsulation of Metadata information for an Annotation reference.
 *
 * @author Ben Leitner
 */
@AutoValue
public abstract class AnnotationMetadata implements UsesTypes {

  /** The type of the annotation. */
  public abstract TypeMetadata type();

  public abstract ImmutableMap<MethodMetadata, ValueMetadata> values();

  @Override
  public Set<TypeMetadata> getAllTypes() {
    ImmutableSet.Builder<TypeMetadata> imports = ImmutableSet.builder();
    imports.addAll(type().getAllTypes());
    for (Map.Entry<MethodMetadata, ValueMetadata> entry : values().entrySet()) {
      imports.addAll(entry.getKey().getAllTypes());
      imports.addAll(entry.getValue().type().getAllTypes());
    }
    return imports.build();
  }

  public static AnnotationMetadata fromType(AnnotationMirror mirror) {
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
