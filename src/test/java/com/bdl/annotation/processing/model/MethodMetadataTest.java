package com.bdl.annotation.processing.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.testing.compile.CompilationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for {@link MethodMetadata}.
 *
 * @author Ben Leitner
 */
@RunWith(JUnit4.class)
public class MethodMetadataTest {

  @Rule public final CompilationRule compilation = new CompilationRule();

  private Elements elements;

  @Before
  public void before() {
    elements = compilation.getElements();
  }

  private ExecutableElement getMethodElement(String fullyQualifiedClassName, String methodName) {
    for (Element element :
        elements.getAllMembers(elements.getTypeElement(fullyQualifiedClassName))) {
      if (element.getKind() == ElementKind.METHOD
          && element.getSimpleName().toString().equals(methodName)) {
        return (ExecutableElement) element;
      }
    }
    throw new IllegalArgumentException(
        String.format(
            "No method \"%s\" found in class \"%s.\"", methodName, fullyQualifiedClassName));
  }

  @Test
  public void testEqualityIgnoresParameterNames() {
    MethodMetadata method1 =
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .setType(TypeMetadata.INT)
            .setName("add")
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "first"))
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "second"))
            .build();
    MethodMetadata method2 =
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .setType(TypeMetadata.INT)
            .setName("add")
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "anInt"))
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "anotherInt"))
            .build();

    assertThat(method1).isEqualTo(method2);
  }

  @Test
  public void testSimpleMethod() {
    MethodMetadata method =
        MethodMetadata.fromMethod(
            getMethodElement("com.bdl.annotation.processing.model.Simple", "add"));
    assertThat(method)
        .isEqualTo(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setIsAbstract(true)
                .setType(TypeMetadata.INT)
                .setName("add")
                .addParameter(ParameterMetadata.of(TypeMetadata.INT, "first"))
                .addParameter(ParameterMetadata.of(TypeMetadata.INT, "second"))
                .build());

    method =
        MethodMetadata.fromMethod(
            getMethodElement("com.bdl.annotation.processing.model.Simple", "repeat"));
    assertThat(method)
        .isEqualTo(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setIsAbstract(true)
                .setType(TypeMetadata.STRING)
                .setName("repeat")
                .addParameter(ParameterMetadata.of(TypeMetadata.STRING, "template"))
                .addParameter(ParameterMetadata.of(TypeMetadata.INT, "times"))
                .build());
  }

  @Test
  public void testAnnotations() {
    ExecutableElement method =
        getMethodElement(
            "com.bdl.annotation.processing.model.AnnotatedMethodInterface", "annotatedSomething");
    Preconditions.checkNotNull(method, "No matching method found");
    MethodMetadata actual = MethodMetadata.fromMethod(method);
    assertThat(actual)
        .isEqualTo(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setIsAbstract(true)
                .setName("annotatedSomething")
                .setType(TypeMetadata.VOID)
                .addAnnotation(
                    AnnotationMetadata.builder()
                        .setType(TestingTypes.SOME_ANNOTATION)
                        .putValue("value", ValueMetadata.create(TypeMetadata.STRING, "foo"))
                        .putValue(
                            "option",
                            ValueMetadata.create(TestingTypes.ANNOTATION_OPTION, "SECOND"))
                        .build())
                .build());
  }

  @Test
  public void testArrays() {
    ExecutableElement method =
        getMethodElement("com.bdl.annotation.processing.model.MethodContainer", "arraysMethod");
    Preconditions.checkNotNull(method, "No matching method found");
    MethodMetadata actual = MethodMetadata.fromMethod(method);
    assertThat(actual)
        .isEqualTo(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setIsAbstract(true)
                .setName("arraysMethod")
                .setType(TypeMetadata.STRING.arrayOf())
                .addParameter(ParameterMetadata.of(TypeMetadata.INT.arrayOf().arrayOf(), "inputs"))
                .build());
  }

  @Test
  public void testParameterizedMethod() {
    MethodMetadata method =
        MethodMetadata.fromMethod(
            getMethodElement("com.bdl.annotation.processing.model.ComplexParameterized", "filter"));
    TypeMetadata bType =
        TypeMetadata.builder()
            .setName("B")
            .addBound(
                TypeMetadata.builder()
                    .setPackageName("java.util")
                    .setName("List")
                    .addParam(TypeMetadata.simpleTypeParam("A"))
                    .build())
            .build();
    MethodMetadata expected =
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .addTypeParameter(TypeMetadata.simpleTypeParam("A"))
            .addTypeParameter(bType)
            .setType(
                TypeMetadata.builder()
                    .setPackageName("com.google.common.collect")
                    .setName("ImmutableList")
                    .addParam(TypeMetadata.simpleTypeParam("A"))
                    .build())
            .setName("filter")
            .addParameter(ParameterMetadata.of(bType, "source"))
            .addParameter(
                ParameterMetadata.of(
                    TypeMetadata.builder()
                        .setPackageName("com.google.common.base")
                        .setName("Predicate")
                        .addParam(TypeMetadata.simpleTypeParam("A"))
                        .build(),
                    "predicate"))
            .build();
    assertThat(method).isEqualTo(expected);

    assertThat(method.toString(Imports.empty()))
        .isEqualTo(
            "public abstract <A, B extends java.util.List<A>>"
                + " com.google.common.collect.ImmutableList<A> filter(B arg0, com.google.common.base.Predicate<A> arg1)");
  }

  @Test
  public void testTypeConversion() {
    MethodMetadata method =
        MethodMetadata.fromMethod(
            getMethodElement("com.bdl.annotation.processing.model.ComplexParameterized", "extend"));
    MethodMetadata expected =
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .addTypeParameter(TypeMetadata.simpleTypeParam("T"))
            .setType(TypeMetadata.simpleTypeParam("T"))
            .setName("extend")
            .addParameter(
                ParameterMetadata.of(
                    TypeMetadata.builder()
                        .setIsTypeParameter(true)
                        .setName("B")
                        .addBound(
                            TypeMetadata.builder()
                                .setPackageName("java.util")
                                .setName("List")
                                .addParam(TypeMetadata.simpleTypeParam("A"))
                                .build())
                        .build(),
                    "list"))
            .addParameter(ParameterMetadata.of(TypeMetadata.simpleTypeParam("T"), "template"))
            .build();

    assertThat(method.convertTypeParameters(ImmutableMap.of("Y", "A", "Z", "B")))
        .isEqualTo(expected);

    expected =
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .addTypeParameter(TypeMetadata.simpleTypeParam("B"))
            .setType(TypeMetadata.simpleTypeParam("B"))
            .setName("extend")
            .addParameter(
                ParameterMetadata.of(
                    TypeMetadata.builder()
                        .setIsTypeParameter(true)
                        .setName("T")
                        .addBound(
                            TypeMetadata.builder()
                                .setPackageName("java.util")
                                .setName("List")
                                .addParam(TypeMetadata.simpleTypeParam("A"))
                                .build())
                        .build(),
                    "list"))
            .addParameter(ParameterMetadata.of(TypeMetadata.simpleTypeParam("B"), "template"))
            .build();
    assertThat(method.convertTypeParameters(ImmutableMap.of("Y", "A", "Z", "T")))
        .isEqualTo(expected);
  }
}
