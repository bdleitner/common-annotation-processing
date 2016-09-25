package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

/**
 * Encapsulation of Metadata information for a Type reference.
 *
 * @author Ben Leitner
 */
@AutoValue
public abstract class TypeMetadata implements UsesTypes, Comparable<TypeMetadata> {

  public static final TypeMetadata VOID = builder()
      .setName("void")
      .build();
  public static final TypeMetadata INT = builder()
      .setName("int")
      .build();
  public static final TypeMetadata LONG = builder()
      .setName("long")
      .build();
  public static final TypeMetadata BOOLEAN = builder()
      .setName("boolean")
      .build();
  public static final TypeMetadata STRING = builder()
      .setPackageName("java.lang")
      .setName("String")
      .build();
  public static final TypeMetadata OBJECT = builder()
      .setPackageName("java.lang")
      .setName("Object")
      .build();
  public static final TypeMetadata CLASS = builder()
      .setPackageName("java.lang")
      .setName("Class")
      .addParam(simpleTypeParam("?"))
      .build();
  private ImmutableSet<TypeMetadata> imports;

  /** The package in which the type lives. */
  public abstract String packageName();

  /** If {@code true}, the type is a Generic type parameter. */
  public abstract boolean isTypeParameter();

  /** The names of any outer classes enclosing this type, from innermost to outermost. */
  public abstract ImmutableList<String> outerClassNames();

  /** The name of the type. */
  public abstract String name();

  /** Type parameters for a generic type. */
  public abstract ImmutableList<TypeMetadata> params();

  /** Bounds for a type parameter type. */
  public abstract ImmutableList<TypeMetadata> bounds();

  @Override
  public int compareTo(TypeMetadata that) {
    return ComparisonChain.start()
        .compare(name(), that.name())
        .compare(outerClassNames(), that.outerClassNames(), Comparators.forLists())
        .compare(packageName(), that.packageName())
        .result();
  }

  /** Get a builder to construct a type name. */
  public TypeNameBuilder nameBuilder() {
    return new TypeNameBuilder();
  }

  @Override
  public Set<TypeMetadata> getAllTypes() {
    if (imports == null) {
      ImmutableSet.Builder<TypeMetadata> allImports = ImmutableSet.builder();
      if (!isTypeParameter()) {
        // TODO: Remove Nesting Prefix and force qualified class names for inner classes?
        allImports.add(rawType());
      }
      for (TypeMetadata param : params()) {
        allImports.addAll(param.getAllTypes());
      }
      for (TypeMetadata bound : bounds()) {
        allImports.addAll(bound.getAllTypes());
      }

      imports = allImports.build();
    }
    return imports;
  }

  public String fullDescription() {
    return nameBuilder()
        .addPackagePrefix()
        .addNestingPrefix()
        .addSimpleName()
        .addFullParams()
        .addBounds()
        .toString();
  }

  public Kind kind() {
    switch (nameBuilder().addPackagePrefix().addNestingPrefix().addSimpleName().toString()) {
      case "java.lang.Integer":
      case "java.lang.Long":
      case "java.lang.Double":
      case "java.lang.Float":
      case "java.lang.Short":
      case "java.lang.Byte":
      case "java.lang.Character":
      case "Integer":
      case "Long":
      case "Double":
      case "Float":
      case "Short":
      case "Byte":
      case "Character":
      case "int":
      case "long":
      case "double":
      case "float":
      case "short":
      case "byte":
      case "char":
        return Kind.NUMERIC;
      case "String":
      case "java.lang.String":
        return Kind.STRING;
      case "Boolean":
      case "java.lang.Boolean":
      case "boolean":
        return Kind.BOOLEAN;
      case "void":
      case "java.lang.Void":
        return Kind.VOID;
      default:
        return Kind.OBJECT;
    }
  }

  TypeMetadata convertTypeParams(List<TypeMetadata> newParams) {
    if (!isTypeParameter()) {
      Preconditions.checkArgument(newParams.size() == params().size(),
          "Cannot convert %s to using type params <%s>, the number of params does not match.",
          fullDescription(),
          newParams.stream().map(TypeMetadata::name).collect(Collectors.joining(", ")));
      ImmutableMap.Builder<String, String> paramNameMapBuilder = ImmutableMap.builder();
      int i = 0;
      for (TypeMetadata param : params()) {
        paramNameMapBuilder.put(param.name(), newParams.get(i).name());
        i++;
      }
      return convertTypeParams(paramNameMapBuilder.build());
    } else {
      Preconditions.checkArgument(newParams.size() == 1,
          "Cannot convert %s to type params <%s>, exactly 1 type parameter is required.",
          fullDescription(),
          newParams.stream().map(TypeMetadata::name).collect(Collectors.joining(", ")));
      return convertTypeParams(ImmutableMap.of(name(), newParams.get(0).name()));
    }
  }

  TypeMetadata convertTypeParams(Map<String, String> paramNameMap) {
    Builder builder = builder()
        .setPackageName(packageName())
        .setIsTypeParameter(isTypeParameter())
        .setName(name());
    builder.outerClassNamesBuilder().addAll(outerClassNames());

    if (!isTypeParameter()) {
      for (TypeMetadata param : params()) {
        builder.addParam(param.convertTypeParams(paramNameMap));
      }
      return builder.build();
    }

    if (paramNameMap.containsKey(name())) {
      builder.setName(paramNameMap.get(name()));
    }
    for (TypeMetadata bound : bounds()) {
      builder.addBound(bound.convertTypeParams(paramNameMap));
    }
    return builder.build();
  }

  TypeMetadata rawType() {
    Preconditions.checkState(!isTypeParameter(), "Cannot take the raw type of type parameter %s", this);
    Builder builder = builder()
        .setPackageName(packageName())
        .setName(name());
    builder.outerClassNamesBuilder().addAll(outerClassNames());
    return builder.build();
  }

  @Override
  public String toString() {
    return fullDescription();
  }

  private static String getSimpleName(TypeMirror type) {
    if (type instanceof DeclaredType) {
      return ((DeclaredType) type).asElement().getSimpleName().toString();
    }
    if (type instanceof TypeVariable) {
      return ((TypeVariable) type).asElement().getSimpleName().toString();
    }
    if (type instanceof PrimitiveType
        || type instanceof NoType
        || type instanceof NullType
        || type instanceof WildcardType) {
      return type.toString();
    }
    throw new IllegalArgumentException(String.format("Cannot determine name for type: %s (%s)",
        type, type.getClass()));
  }

  private static String getQualifiedName(TypeMirror type) {
    if (type instanceof DeclaredType) {
      return ((QualifiedNameable) ((DeclaredType) type).asElement()).getQualifiedName().toString();
    }
    if (type instanceof TypeVariable) {
      return ((QualifiedNameable) ((TypeVariable) type).asElement()).getQualifiedName().toString();
    }
    throw new IllegalArgumentException("Cannot determine name for type: " + type);
  }

  private static TypeMetadata fromType(TypeMirror type, boolean withBounds) {
    Builder builder = builder().setName(getSimpleName(type));

    if (type.getKind() == TypeKind.WILDCARD) {
      builder.setIsTypeParameter(true);
    }
    if (type.getKind() == TypeKind.TYPEVAR) {
      builder.setIsTypeParameter(true);
      TypeVariable typeVar = (TypeVariable) type;
      if (withBounds) {
        TypeMirror upperBound = typeVar.getUpperBound();
        if (upperBound instanceof IntersectionType) {
          for (TypeMirror bound : ((IntersectionType) upperBound).getBounds()) {
            if (getQualifiedName(bound).equals("java.lang.Object")) {
              continue;
            }
            builder.addBound(fromType(bound, false));

          }
        } else {
          if (!getQualifiedName(upperBound).equals("java.lang.Object")) {
            builder.addBound(fromType(upperBound, false));
          }
        }
      }
      return builder.build();
    }

    if (type instanceof DeclaredType) {
      for (TypeMirror param : ((DeclaredType) type).getTypeArguments()) {
        builder.addParam(fromType(param, withBounds));
      }
      Element enclosingElement = ((DeclaredType) type).asElement().getEnclosingElement();
      while (enclosingElement.getKind() != ElementKind.PACKAGE) {
        builder.addOuterClass(enclosingElement.getSimpleName().toString());
        enclosingElement = enclosingElement.getEnclosingElement();
      }

      builder.setPackageName(((QualifiedNameable) enclosingElement).getQualifiedName().toString());
    }
    return builder.build();
  }

  public static TypeMetadata fromType(TypeMirror type) {
    return fromType(type, true);
  }

  public static TypeMetadata fromElement(Element element) {
    return fromType(element.asType(), true);
  }

  public static TypeMetadata fromObject(Object object) {
    return from(object.getClass(), true);
  }

  public static TypeMetadata from(Type type) {
    return from(type, true);
  }

  private static TypeMetadata from(Type type, boolean includeParams) {
    if (type instanceof java.lang.reflect.TypeVariable) {
      java.lang.reflect.TypeVariable typeVar = (java.lang.reflect.TypeVariable) type;
      Builder metadata = builder()
          .setIsTypeParameter(true)
          .setName(typeVar.getName());
      for (Type bound : typeVar.getBounds()) {
        if (bound.equals(Object.class)) {
          continue;
        }
        metadata.addBound(from(bound, includeParams));
      }
      return metadata.build();
    }

    if (type instanceof ParameterizedType) {
      ParameterizedType paramType = (ParameterizedType) type;
      Builder metadata = from(paramType.getRawType(), false).toBuilder();
      if (includeParams) {
        for (Type param : paramType.getActualTypeArguments()) {
          metadata.addParam(from(param, true));
        }
      }
      return metadata.build();
    }
    Preconditions.checkArgument(type instanceof Class);
    Class<?> clazz = (Class<?>) type;
    Builder metadata = TypeMetadata.builder()
        .setPackageName(clazz.getPackage().getName())
        .setName(clazz.getSimpleName());
    Class<?> enclosing = clazz.getEnclosingClass();
    while (enclosing != null) {
      metadata.addOuterClass(enclosing.getSimpleName());
    }
    if (includeParams) {
      for (java.lang.reflect.TypeVariable<? extends Class<?>> variable : clazz.getTypeParameters()) {
        metadata.addParam(from(variable, true));
      }
    }
    return metadata.build();
  }

  public static TypeMetadata simpleTypeParam(String paramName) {
    return builder().setIsTypeParameter(true).setName(paramName).build();
  }

  static Builder builder() {
    return new AutoValue_TypeMetadata.Builder()
        .setPackageName("")
        .setIsTypeParameter(false);
  }

  abstract Builder toBuilder();

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setPackageName(String packageName);

    abstract Builder setIsTypeParameter(boolean isTypeParameter);

    abstract ImmutableList.Builder<String> outerClassNamesBuilder();

    abstract Builder setName(String name);

    abstract ImmutableList.Builder<TypeMetadata> paramsBuilder();

    abstract ImmutableList.Builder<TypeMetadata> boundsBuilder();

    Builder addOuterClass(String className) {
      setIsTypeParameter(false);
      outerClassNamesBuilder().add(className);
      return this;
    }

    Builder addParam(TypeMetadata metadata) {
      paramsBuilder().add(metadata);
      return this;
    }

    Builder addBound(TypeMetadata metadata) {
      setIsTypeParameter(true);
      boundsBuilder().add(metadata);
      return this;
    }

    abstract TypeMetadata autoBuild();

    TypeMetadata build() {
      TypeMetadata metadata = autoBuild();
      if (metadata.isTypeParameter()) {
        Preconditions.checkState(metadata.params().isEmpty(),
            "Type parameters given for type-parameter: %s",
            metadata.nameBuilder().addSimpleName().addBounds().toString());
        Preconditions.checkState(metadata.outerClassNames().isEmpty(),
            "Nesting classes given type-parameter: %s", metadata.nameBuilder().addSimpleName().addBounds().toString());
        Preconditions.checkState(metadata.packageName().isEmpty(),
            "Nonempty package given for type-parameter: %s",
            metadata.nameBuilder().addSimpleName().addBounds().toString());
      } else {
        Preconditions.checkState(metadata.bounds().isEmpty(),
            "Bounds given for non-type-parameter: %s", metadata.fullDescription());
      }
      return metadata;
    }
  }

  // TODO: Incorporate shorter names if we have imports available.
  public class TypeNameBuilder {
    private final StringBuilder nameBuilder;

    TypeNameBuilder() {
      nameBuilder = new StringBuilder();
    }

    public TypeNameBuilder addOutermostClassName() {
      if (!outerClassNames().isEmpty()) {
        nameBuilder.append(outerClassNames().get(outerClassNames().size() - 1));
      }
      return this;
    }

    public TypeNameBuilder addNestingPrefix(String delimiter) {
      if (!outerClassNames().isEmpty()) {
        nameBuilder.append(Joiner.on(delimiter).join(Lists.reverse(outerClassNames()))).append(delimiter);
      }
      return this;
    }

    public TypeNameBuilder addPackagePrefix() {
      if (!packageName().isEmpty()) {
        nameBuilder.append(packageName()).append(".");
      }
      return this;
    }

    public TypeNameBuilder addNestingPrefix() {
      return addNestingPrefix(".");
    }

    public TypeNameBuilder addSimpleName() {
      nameBuilder.append(name());
      return this;
    }

    private TypeNameBuilder addParams(java.util.function.Function<TypeMetadata, String> paramsToStrings) {
      if (!params().isEmpty()) {
        nameBuilder
            .append("<")
            .append(params().stream().map(paramsToStrings).collect(Collectors.joining(", ")))
            .append(">");
      }
      return this;
    }

    public TypeNameBuilder addSimpleParams() {
      return addParams(param -> param.nameBuilder()
          .addPackagePrefix()
          .addNestingPrefix()
          .addSimpleName()
          .addSimpleParams()
          .toString());
    }

    public TypeNameBuilder addFullParams() {
      return addParams(param -> param.nameBuilder()
          .addPackagePrefix()
          .addNestingPrefix()
          .addSimpleName()
          .addSimpleParams() // Note, there cannot be both simple params and bounds.
          .addBounds() // as one only applies to type params and one to non-type-params.
          .toString());
    }

    public TypeNameBuilder addBounds() {
      if (!bounds().isEmpty()) {
        nameBuilder
            .append(" extends ")
            .append(
                bounds().stream()
                    .map((bound) -> bound.nameBuilder()
                        .addPackagePrefix()
                        .addNestingPrefix()
                        .addSimpleName()
                        .addSimpleParams() // Note, there cannot be both simple params and bounds.
                        .addBounds() // as one only applies to type params and one to non-type-params.
                        .toString())
                    .collect(Collectors.joining(", ")));
      }
      return this;
    }

    public TypeNameBuilder append(String s) {
      nameBuilder.append(s);
      return this;
    }

    @Override
    public String toString() {
      return nameBuilder.toString();
    }
  }
}
