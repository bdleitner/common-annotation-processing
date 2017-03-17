package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Encapsulation of Metadata information for an Annotation reference.
 *
 * @author Ben Leitner
 */
@AutoValue
public abstract class AnnotationMetadata implements UsesTypes {

  /** The type of the annotation. */
  public abstract TypeMetadata type();

  public abstract ImmutableMap<String, ValueMetadata> values();

  @Nullable
  public ValueMetadata value(String name) {
    return values().get(name);
  }

  @Override
  public Set<TypeMetadata> getAllTypes() {
    ImmutableSet.Builder<TypeMetadata> imports = ImmutableSet.builder();
    imports.addAll(type().getAllTypes());
    for (ValueMetadata value : values().values()) {
      imports.addAll(value.type().getAllTypes());
    }
    return imports.build();
  }

  public String toString(Imports imports) {
    StringBuilder s = new StringBuilder("@");
    s.append(type().toString(imports));
    if (values().isEmpty()) {
      return s.toString();
    }
    s.append("(");
    if (values().size() == 1 && values().get("value") != null) {
      // a single value with name "value" so we don't need the method name.
      s.append("\"").append(values().get("value").value()).append("\"");
    } else {
      s.append(
          values()
              .entrySet()
              .stream()
              .map(entry -> String.format("%s = \"%s\"", entry.getKey(), entry.getValue()))
              .collect(Collectors.joining(", ")));
    }
    s.append(")");
    return s.toString();
  }

  public static AnnotationMetadata fromType(AnnotationMirror mirror) {
    Builder metadata = AnnotationMetadata.builder();
    metadata.setType(TypeMetadata.fromType(mirror.getAnnotationType()));
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
        mirror.getElementValues().entrySet()) {
      MethodMetadata method = MethodMetadata.fromMethod(entry.getKey());
      metadata.putValue(
          method.name(),
          ValueMetadata.create(method.type(), entry.getValue().getValue().toString()));
    }
    return metadata.build();
  }

  public static Builder builder() {
    return new AutoValue_AnnotationMetadata.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setType(TypeMetadata type);

    abstract ImmutableMap.Builder<String, ValueMetadata> valuesBuilder();

    public Builder putValue(String methodName, ValueMetadata object) {
      valuesBuilder().put(methodName, object);
      return this;
    }

    public abstract AnnotationMetadata build();
  }
}
