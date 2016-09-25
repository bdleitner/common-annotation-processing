package com.bdl.annotation.processing.model;

import static com.bdl.annotation.processing.model.TypeMetadata.simpleTypeParam;
import static com.google.common.truth.Truth.assertThat;

import com.google.testing.compile.CompilationRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Tests for the ClassMetadata class.
 *
 * @author Ben Leitner
 */
@RunWith(JUnit4.class)
public class ClassMetadataTest {

  @Rule public final CompilationRule compilation = new CompilationRule();

  private ClassMetadata metadata;

  @Before
  public void before() {
    Elements elements = compilation.getElements();
    TypeElement element = elements.getTypeElement("com.bdl.annotation.processing.model.AbstractClass");
    metadata = ClassMetadata.fromElement(element);
  }

  @Test
  public void testRequiredMethods() {
    TypeMetadata typeEExtendsListOfD = TypeMetadata.builder()
        .setName("E")
        .addBound(TypeMetadata.builder()
            .setPackageName("java.util")
            .setName("List")
            .addParam(simpleTypeParam("D"))
            .build())
        .build();
    assertThat(metadata.getAllMethods()).containsExactly(
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .setType(TypeMetadata.STRING)
            .setName("thingToString")
            .addParameter(ParameterMetadata.of(TestingTypes.THING, "input"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .setType(TypeMetadata.INT)
            .setName("add")
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "first"))
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "second"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .setType(simpleTypeParam("A"))
            .setName("extendedFrozzle")
            .addParameter(ParameterMetadata.of(simpleTypeParam("A"), "input"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .setType(simpleTypeParam("A"))
            .setName("superExtendedFrozzle")
            .addParameter(ParameterMetadata.of(simpleTypeParam("A"), "input"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .addTypeParameter(simpleTypeParam("D"))
            .addTypeParameter(typeEExtendsListOfD)
            .setType(TypeMetadata.builder()
                .setPackageName("com.google.common.collect")
                .setName("ImmutableList")
                .addParam(simpleTypeParam("D"))
                .build())
            .setName("filter")
            .addParameter(ParameterMetadata.of(typeEExtendsListOfD, "source"))
            .addParameter(ParameterMetadata.of(
                TypeMetadata.builder()
                    .setPackageName("com.google.common.base")
                    .setName("Predicate")
                    .addParam(simpleTypeParam("D"))
                    .build(),
                "predicate"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .addTypeParameter(simpleTypeParam("T"))
            .setType(simpleTypeParam("T"))
            .setName("extend")
            .addParameter(ParameterMetadata.of(
                TypeMetadata.builder()
                    .setIsTypeParameter(true)
                    .setName("C")
                    .addBound(TypeMetadata.builder()
                        .setPackageName("java.util")
                        .setName("List")
                        .addParam(simpleTypeParam("B"))
                        .build())
                    .build(),
                "list"))
            .addParameter(ParameterMetadata.of(simpleTypeParam("T"), "template"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .setType(simpleTypeParam("B"))
            .setName("blargh")
            .addParameter(ParameterMetadata.of(simpleTypeParam("B"), "input"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .setType(TypeMetadata.VOID)
            .setName("doNothing")
            .addParameter(ParameterMetadata.of(TypeMetadata.STRING, "input"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PROTECTED)
            .setIsAbstract(true)
            .setType(TypeMetadata.VOID)
            .setName("voidFromSuper")
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "foo"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(TypeMetadata.STRING)
            .setName("blorp")
            .addParameter(ParameterMetadata.of(TypeMetadata.STRING, "input"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(TypeMetadata.BOOLEAN)
            .setName("equals")
            .addParameter(ParameterMetadata.of(TypeMetadata.OBJECT, "that"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(simpleTypeParam("A"))
            .setName("frozzle")
            .addParameter(ParameterMetadata.of(simpleTypeParam("A"), "input"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(simpleTypeParam("B"))
            .setName("frumple")
            .addParameter(ParameterMetadata.of(simpleTypeParam("B"), "input"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(TypeMetadata.CLASS)
            .setName("getClass")
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(TypeMetadata.INT)
            .setName("hashCode")
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(TypeMetadata.VOID)
            .setName("notify")
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(TypeMetadata.VOID)
            .setName("notifyAll")
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(TypeMetadata.VOID)
            .setName("wait")
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(TypeMetadata.VOID)
            .setName("wait")
            .addParameter(ParameterMetadata.of(TypeMetadata.LONG, "input"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(TypeMetadata.VOID)
            .setName("wait")
            .addParameter(ParameterMetadata.of(TypeMetadata.LONG, "arg0"))
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "arg1"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PROTECTED)
            .setType(TypeMetadata.VOID)
            .setName("finalize")
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PROTECTED)
            .setType(TypeMetadata.OBJECT)
            .setName("clone")
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(TypeMetadata.STRING)
            .setName("toString")
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PROTECTED)
            .setType(TypeMetadata.INT)
            .setName("fromSuper")
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "foo"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(TypeMetadata.STRING)
            .setName("repeat")
            .addParameter(ParameterMetadata.of(TypeMetadata.STRING, "template"))
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "times"))
            .build());
  }

  @Test
  public void testConstructors() {
    assertThat(metadata.constructors()).containsExactly(
        ConstructorMetadata.builder()
            .visibility(Visibility.PRIVATE)
            .addParameter(ParameterMetadata.of(TypeMetadata.STRING, "blargh"))
            .build(),
        ConstructorMetadata.builder()
            .visibility(Visibility.PACKAGE_LOCAL)
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "foo"))
            .build(),
        ConstructorMetadata.builder()
            .visibility(Visibility.PUBLIC)
            .addParameter(ParameterMetadata.of(TypeMetadata.BOOLEAN, "foo"))
            .build());
  }

  @Test
  public void testAnnotations() {
    assertThat(metadata.annotations()).containsExactly(AnnotationMetadata.builder()
        .setType(TestingTypes.SOME_ANNOTATION)
        .build());
  }
}
