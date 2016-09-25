package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Metadata class for a relevant parts of a class to write.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class ClassMetadata implements GeneratesImports, GeneratesMethods {

  /** Enumeration of the possible types to AutoAdapt: Class and Interface. */
  enum Category {
    CLASS,
    INTERFACE;

    static Category forKind(ElementKind kind) {
      switch (kind) {
        case CLASS:
          return Category.CLASS;
        case INTERFACE:
          return Category.INTERFACE;
        default:
          throw new IllegalArgumentException("Bad Kind: " + kind);
      }
    }
  }

  private ImmutableList<MethodMetadata> allMethods;

  /** Annotations defined on the class. */
  abstract ImmutableList<AnnotationMetadata> annotations();

  /** The AutoAdaptee's {@link Category}. */
  abstract Category category();

  /** Contains the complete type metadata for the class. */
  abstract TypeMetadata type();

  /** The inheritance metadatas for the types that this one inherits from. */
  abstract ImmutableList<InheritanceMetadata> inheritances();

  abstract ImmutableSet<ConstructorMetadata> constructors();

  /** Methods that are declared in this type. */
  abstract ImmutableList<MethodMetadata> methods();

  @Override
  public Set<TypeMetadata> getImports() {
    ImmutableSet.Builder<TypeMetadata> imports = ImmutableSet.builder();
    imports.addAll(type().getImports());
    for (InheritanceMetadata inheritance : inheritances()) {
      imports.addAll(inheritance.getImports());
    }
    for (ConstructorMetadata constructor : constructors()) {
      imports.addAll(constructor.getImports());
    }
    for (MethodMetadata method : methods()) {
      imports.addAll(method.getImports());
    }
    return imports.build();
  }

  /** Methods declared in this type or in any supertype / interface. */
  @Override
  public ImmutableList<MethodMetadata> getAllMethods() {
    if (allMethods == null) {
      Stream<MethodMetadata> methodStream = Stream.empty();
      for (InheritanceMetadata inheritance : inheritances()) {
        methodStream = Stream.concat(
            methodStream,
            inheritance.getAllMethods().stream()
                .filter((method) -> method.visibility() != Visibility.PRIVATE));
      }

      methodStream = Stream.concat(methodStream, methods().stream());

      Set<MethodMetadata> methods = methodStream.collect(Collectors.toSet());

      Set<MethodMetadata> concreteMethods = methods.stream()
          .filter((method) -> !method.isAbstract())
          .collect(Collectors.toSet());

      Set<MethodMetadata> abstractMethods = methods.stream()
          .filter(MethodMetadata::isAbstract)
          .filter((method) -> !concreteMethods.contains(method.toBuilder().setIsAbstract(false).build()))
          .collect(Collectors.toSet());

      allMethods = ImmutableList.copyOf(
          Stream.concat(
              concreteMethods.stream(),
              abstractMethods.stream())
              .sorted()
              .collect(Collectors.toList()));
    }
    return allMethods;
  }

  String fullyQualifiedPathName() {
    return type().nameBuilder()
        .addPackagePrefix()
        .addNestingPrefix()
        .addSimpleName()
        .toString();
  }

  @Override
  public String toString() {
    return fullyQualifiedPathName();
  }

  static ClassMetadata fromElement(Element element) {
    Builder metadata = builder()
        .setCategory(Category.forKind(element.getKind()))
        .setType(TypeMetadata.fromElement(element));

    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      metadata.addAnnotation(AnnotationMetadata.fromType(annotationMirror));
    }

    TypeElement typeElement = (TypeElement) element;
    TypeMirror superClass = typeElement.getSuperclass();
    if (superClass instanceof DeclaredType) {
      metadata.addInheritance(InheritanceMetadata.fromType((DeclaredType) superClass));
    }

    for (TypeMirror inherited : typeElement.getInterfaces()) {
      metadata.addInheritance(InheritanceMetadata.fromType((DeclaredType) inherited));
    }

    for (Element enclosed : element.getEnclosedElements()) {
      if (enclosed.getKind() == ElementKind.METHOD) {
        metadata.addMethod(MethodMetadata.fromMethod((ExecutableElement) enclosed));
      }
      if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
        metadata.addConstructor(ConstructorMetadata.fromConstructor(enclosed));
      }
    }
    return metadata.build();
  }

  static Builder builder() {
    return new AutoValue_ClassMetadata.Builder();
  }

  @AutoValue.Builder
  static abstract class Builder {
    abstract ImmutableList.Builder<AnnotationMetadata> annotationsBuilder();

    abstract Builder setCategory(Category category);

    abstract Builder setType(TypeMetadata type);

    abstract ImmutableList.Builder<InheritanceMetadata> inheritancesBuilder();

    abstract ImmutableSet.Builder<ConstructorMetadata> constructorsBuilder();

    abstract ImmutableList.Builder<MethodMetadata> methodsBuilder();

    Builder addInheritance(InheritanceMetadata inheritance) {
      inheritancesBuilder().add(inheritance);
      return this;
    }

    Builder addAnnotation(AnnotationMetadata annotation) {
      annotationsBuilder().add(annotation);
      return this;
    }

    Builder addConstructor(ConstructorMetadata constructor) {
      constructorsBuilder().add(constructor);
      return this;
    }

    Builder addMethod(MethodMetadata method) {
      methodsBuilder().add(method);
      return this;
    }

    abstract ClassMetadata build();
  }
}
