package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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

  public String packagePrefix() {
    return packageName().isEmpty() ? "" : packageName() + ".";
  }

  public String nestingPrefix() {
    return nestingPrefix(".");
  }

  public String nestingPrefix(String delimiter) {
    String prefix = outerClassNames().reverse().stream().collect(Collectors.joining(delimiter));
    return prefix.isEmpty() ? "" : prefix + delimiter;
  }

  public String reference(Imports imports) {
    return reference(imports, false);
  }

  public String reference(Imports imports, boolean withBounds) {
    StringBuilder s = new StringBuilder();
    Imports.ReferenceType referenceType = imports.reference(this);
    switch (referenceType) {
      case FULLY_QUALIFIED_PATH_NAME:
        s.append(packagePrefix());
        // fallthrough
      case NESTED_NAME:
        s.append(nestingPrefix());
        // fallthrough
      case NAME_ONLY:
        s.append(name());
    }
    if (!params().isEmpty()) {
      s.append("<");
      s.append(params().stream()
          .map((type) -> type.reference(imports, withBounds))
          .collect(Collectors.joining(", ")));
      s.append(">");
    }
    if (withBounds && !bounds().isEmpty()) {
      s.append(" extends ");
      s.append(bounds().stream()
          .map((type) -> type.reference(imports, false)) // do not recurse on bounds
          .collect(Collectors.joining(" & ")));
    }
    return s.toString();
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

  public Kind kind() {
    switch (packagePrefix() + nestingPrefix() + name()) {
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
          reference(Imports.empty()),
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
          reference(Imports.empty()),
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
    if (params().isEmpty()) {
      return this;
    }
    Builder builder = builder()
        .setPackageName(packageName())
        .setName(name());
    builder.outerClassNamesBuilder().addAll(outerClassNames());
    return builder.build();
  }

  @Override
  public String toString() {
    return reference(Imports.empty());
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

  public static Builder builder() {
    return new AutoValue_TypeMetadata.Builder()
        .setPackageName("")
        .setIsTypeParameter(false);
  }

  abstract Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setPackageName(String packageName);

    public abstract Builder setIsTypeParameter(boolean isTypeParameter);

    abstract ImmutableList.Builder<String> outerClassNamesBuilder();

    public abstract Builder setName(String name);

    abstract ImmutableList.Builder<TypeMetadata> paramsBuilder();

    abstract ImmutableList.Builder<TypeMetadata> boundsBuilder();

    public Builder addOuterClass(String className) {
      setIsTypeParameter(false);
      outerClassNamesBuilder().add(className);
      return this;
    }

    public Builder addParam(TypeMetadata metadata) {
      paramsBuilder().add(metadata);
      return this;
    }

    public Builder addBound(TypeMetadata metadata) {
      setIsTypeParameter(true);
      boundsBuilder().add(metadata);
      return this;
    }

    abstract TypeMetadata autoBuild();

    public TypeMetadata build() {
      TypeMetadata metadata = autoBuild();
      if (metadata.isTypeParameter()) {
        Preconditions.checkState(metadata.params().isEmpty(),
            "Type parameters given for type-parameter: %s",
            metadata.reference(Imports.empty()));
        Preconditions.checkState(metadata.outerClassNames().isEmpty(),
            "Nesting classes given type-parameter: %s",
            metadata.reference(Imports.empty()));
        Preconditions.checkState(metadata.packageName().isEmpty(),
            "Nonempty package given for type-parameter: %s",
            metadata.reference(Imports.empty()));
      } else {
        Preconditions.checkState(metadata.bounds().isEmpty(),
            "Bounds given for non-type-parameter: %s", metadata.reference(Imports.empty()));
      }
      return metadata;
    }
  }
}
