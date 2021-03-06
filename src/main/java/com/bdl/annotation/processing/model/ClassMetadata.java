package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * Metadata class for a relevant parts of a class to write.
 *
 * @author Ben Leitner
 */
@AutoValue
public abstract class ClassMetadata implements UsesTypes, Annotatable {

  /** Enumeration of the possible types to AutoAdapt: Class and Interface. */
  public enum Category {
    CLASS,
    INTERFACE;

    static Category forKind(ElementKind kind) {
      switch (kind) {
        case CLASS:
        case ENUM:
          return Category.CLASS;
        case INTERFACE:
        case ANNOTATION_TYPE:
          return Category.INTERFACE;
        default:
          throw new IllegalArgumentException("Bad Kind: " + kind);
      }
    }
  }

  private ImmutableList<FieldMetadata> allFields;
  private ImmutableList<MethodMetadata> allMethods;

  @Override
  public abstract ImmutableList<AnnotationMetadata> annotations();

  public abstract Modifiers modifiers();

  /** The AutoAdaptee's {@link Category}. */
  public abstract Category category();

  /** Contains the complete type metadata for the class. */
  public abstract TypeMetadata type();

  /** The inheritance metadatas for the types that this one inherits from. */
  public abstract ImmutableList<InheritanceMetadata> inheritances();

  public abstract ImmutableSet<ConstructorMetadata> constructors();

  /** Fields that are declared in this class. */
  public abstract ImmutableList<FieldMetadata> fields();

  /** Methods that are declared in this class. */
  public abstract ImmutableList<MethodMetadata> methods();

  @Override
  public Set<TypeMetadata> getAllTypes() {
    ImmutableSet.Builder<TypeMetadata> imports = ImmutableSet.builder();
    imports.addAll(type().getAllTypes());
    imports.addAll(
        inheritances()
            .stream()
            .map((inheritance) -> inheritance.classMetadata().type())
            .collect(toSet()));
    for (AnnotationMetadata annotation : annotations()) {
      imports.addAll(annotation.getAllTypes());
    }
    for (ConstructorMetadata constructor : constructors()) {
      imports.addAll(constructor.getAllTypes());
    }
    for (MethodMetadata method : methods()) {
      imports.addAll(method.getAllTypes());
    }
    return imports.build();
  }

  public ImmutableList<FieldMetadata> getAllFields() {
    if (allFields == null) {
      Stream<FieldMetadata> fieldStream = Stream.empty();
      for (InheritanceMetadata inheritance : inheritances()) {
        fieldStream =
            Stream.concat(
                fieldStream,
                inheritance
                    .getAllFields()
                    .stream()
                    .filter((field) -> field.modifiers().visibility() != Visibility.PRIVATE));
      }

      fieldStream = Stream.concat(fieldStream, fields().stream());

      allFields = ImmutableList.copyOf(fieldStream.sorted().collect(Collectors.toList()));
    }
    return allFields;
  }

  /** Methods declared in this type or in any supertype / interface. */
  public ImmutableList<MethodMetadata> getAllMethods() {
    if (allMethods == null) {
      Stream<MethodMetadata> methodStream = Stream.empty();
      for (InheritanceMetadata inheritance : inheritances()) {
        methodStream =
            Stream.concat(
                methodStream,
                inheritance
                    .getAllMethods()
                    .stream()
                    .filter((method) -> method.modifiers().visibility() != Visibility.PRIVATE));
      }

      methodStream = Stream.concat(methodStream, methods().stream());

      Set<MethodMetadata> methods = methodStream.collect(toSet());

      Set<MethodMetadata> concreteMethods =
          methods.stream().filter((method) -> !method.modifiers().isAbstract()).collect(toSet());

      Set<MethodMetadata> strippedConcreteMethods =
          concreteMethods.stream().map(MethodMetadata::withoutAnnotations).collect(toSet());

      Set<MethodMetadata> abstractMethods =
          methods
              .stream()
              .filter(method -> method.modifiers().isAbstract())
              .filter(
                  method ->
                      !strippedConcreteMethods.contains(method.asConcrete().withoutAnnotations()))
              .collect(toSet());

      allMethods =
          ImmutableList.copyOf(
              Stream.concat(concreteMethods.stream(), abstractMethods.stream())
                  .sorted()
                  .collect(Collectors.toList()));
    }
    return allMethods;
  }

  public String fullyQualifiedPathName() {
    return type().packagePrefix() + type().nestingPrefix() + type().name();
  }

  @Override
  public String toString() {
    return fullyQualifiedPathName();
  }

  public static ClassMetadata fromElement(Element element) {
    TypeMetadata type = TypeMetadata.fromElement(element);

    Modifiers modifiers = Modifiers.visibility(Visibility.forElement(element));
    if (element.getModifiers().contains(Modifier.ABSTRACT)) {
      modifiers = modifiers.makeAbstract();
    }
    if (element.getModifiers().contains(Modifier.STATIC)) {
      modifiers = modifiers.makeStatic();
    }
    if (element.getModifiers().contains(Modifier.FINAL)) {
      modifiers = modifiers.makeFinal();
    }
    Builder metadata =
        builder()
            .setModifiers(modifiers)
            .setCategory(Category.forKind(element.getKind()))
            .setType(type);

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
      if (enclosed.getKind() == ElementKind.FIELD) {
        metadata.addField(FieldMetadata.from(type, enclosed));
      }
    }
    return metadata.build();
  }

  public static Builder builder() {
    return new AutoValue_ClassMetadata.Builder()
        .setModifiers(Modifiers.visibility(Visibility.PUBLIC));
  }

  @AutoValue.Builder
  public abstract static class Builder {
    abstract ImmutableList.Builder<AnnotationMetadata> annotationsBuilder();

    public abstract Builder setModifiers(Modifiers modifiers);

    public abstract Builder setCategory(Category category);

    public abstract Builder setType(TypeMetadata type);

    abstract ImmutableList.Builder<InheritanceMetadata> inheritancesBuilder();

    abstract ImmutableSet.Builder<ConstructorMetadata> constructorsBuilder();

    abstract ImmutableList.Builder<FieldMetadata> fieldsBuilder();

    abstract ImmutableList.Builder<MethodMetadata> methodsBuilder();

    public Builder addInheritance(InheritanceMetadata inheritance) {
      inheritancesBuilder().add(inheritance);
      return this;
    }

    public Builder addAnnotation(AnnotationMetadata annotation) {
      annotationsBuilder().add(annotation);
      return this;
    }

    public Builder addConstructor(ConstructorMetadata constructor) {
      constructorsBuilder().add(constructor);
      return this;
    }

    public Builder addField(FieldMetadata field) {
      fieldsBuilder().add(field);
      return this;
    }

    public Builder addMethod(MethodMetadata method) {
      methodsBuilder().add(method);
      return this;
    }

    public abstract ClassMetadata build();
  }
}
