package com.bdl.annotation.processing.model;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.testing.compile.CompilationRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Tests for the TypeMetadata class.
 *
 * @author Ben Leitner
 */
@RunWith(JUnit4.class)
public class TypeMetadataTest {

  @Rule public final CompilationRule compilation = new CompilationRule();

  private Elements elements;

  @Before
  public void before() {
    elements = compilation.getElements();
  }

  @Test
  public void testSimpleInterface() {
    TypeElement element = elements.getTypeElement("com.bdl.annotation.processing.model.Simple");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.annotation.processing.model")
            .setName("Simple")
            .build());

    assertThat(type.toString(Imports.empty()))
        .isEqualTo("com.bdl.annotation.processing.model.Simple");

    assertThat(type.toString(Imports.empty(), true))
        .isEqualTo("com.bdl.annotation.processing.model.Simple");
  }

  @Test
  public void testParameterizedInterface() {
    TypeElement element = elements.getTypeElement("com.bdl.annotation.processing.model.Parameterized");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.annotation.processing.model")
            .setName("Parameterized")
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("T")
                .build())
            .build());

    assertThat(type.toString(Imports.empty()))
        .isEqualTo("com.bdl.annotation.processing.model.Parameterized<T>");

    assertThat(type.toString(Imports.empty(), true))
        .isEqualTo("com.bdl.annotation.processing.model.Parameterized<T>");
  }

  @Test
  public void testParameterizedWithBound() {
    TypeElement element = elements.getTypeElement("com.bdl.annotation.processing.model.Field");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.annotation.processing.model")
            .setName("Field")
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("F")
                .addBound(TypeMetadata.builder()
                    .setPackageName("com.bdl.annotation.processing.model")
                    .setName("Field")
                    .addParam(TypeMetadata.builder()
                        .setIsTypeParameter(true)
                        .setName("F")
                        .build())
                    .build())
                .build())
            .build());

    assertThat(type.toString(Imports.empty()))
        .isEqualTo("com.bdl.annotation.processing.model.Field<F>");

    assertThat(type.toString(Imports.empty(), true))
        .isEqualTo("com.bdl.annotation.processing.model.Field<F extends com.bdl.annotation.processing.model.Field<F>>");
  }

  @Test
  public void testMultipleParamsMultipleBounds() {
    TypeElement element = elements.getTypeElement("com.bdl.annotation.processing.model.ParameterizedMultibound");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.annotation.processing.model")
            .setName("ParameterizedMultibound")
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("S")
                .build())
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("T")
                .addBound(TypeMetadata.builder()
                    .setPackageName("com.bdl.annotation.processing.model")
                    .setName("Simple")
                    .build())
                .addBound(TypeMetadata.builder()
                    .setPackageName("com.bdl.annotation.processing.model")
                    .setName("Parameterized")
                    .addParam(TypeMetadata.builder()
                        .setIsTypeParameter(true)
                        .setName("S")
                        .build())
                    .build())
                .build())
            .build());

    assertThat(type.toString(Imports.empty()))
        .isEqualTo("com.bdl.annotation.processing.model.ParameterizedMultibound<S, T>");

    assertThat(type.toString(Imports.empty(), true))
        .isEqualTo("com.bdl.annotation.processing.model.ParameterizedMultibound"
            + "<S, T extends com.bdl.annotation.processing.model.Simple & com.bdl.annotation.processing.model.Parameterized<S>>");
  }

  @Test
  public void testMultipleParamsMultipleBounds_fromClass() {
    TypeMetadata type = TypeMetadata.from(ParameterizedMultibound.class);
    TypeMetadata expected = TypeMetadata.builder()
        .setPackageName("com.bdl.annotation.processing.model")
        .setName("ParameterizedMultibound")
        .addParam(TypeMetadata.builder()
            .setIsTypeParameter(true)
            .setName("S")
            .build())
        .addParam(TypeMetadata.builder()
            .setIsTypeParameter(true)
            .setName("T")
            .addBound(TypeMetadata.builder()
                .setPackageName("com.bdl.annotation.processing.model")
                .setName("Simple")
                .build())
            .addBound(TypeMetadata.builder()
                .setPackageName("com.bdl.annotation.processing.model")
                .setName("Parameterized")
                .addParam(TypeMetadata.builder()
                    .setIsTypeParameter(true)
                    .setName("S")
                    .build())
                .build())
            .build())
        .build();
    assertThat(type).isEqualTo(expected);

    assertThat(type.toString(Imports.empty()))
        .isEqualTo("com.bdl.annotation.processing.model.ParameterizedMultibound<S, T>");

    assertThat(type.toString(Imports.empty(), true))
        .isEqualTo("com.bdl.annotation.processing.model.ParameterizedMultibound"
            + "<S, T extends com.bdl.annotation.processing.model.Simple & com.bdl.annotation.processing.model.Parameterized<S>>");
  }

  @Test
  public void testNestedClasses() {
    TypeElement element = elements.getTypeElement("com.bdl.annotation.processing.model.TopLevel.Outer.Inner");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.annotation.processing.model")
            .setName("Inner")
            .addOuterClass("Outer")
            .addOuterClass("TopLevel")
            .build());

    assertThat(type.toString(Imports.empty()))
        .isEqualTo("com.bdl.annotation.processing.model.TopLevel.Outer.Inner");

    assertThat(type.toString(Imports.empty(), true))
        .isEqualTo("com.bdl.annotation.processing.model.TopLevel.Outer.Inner");
  }

  @Test
  public void testNestedClasses_fromType() {
    TypeMetadata type = TypeMetadata.from(TopLevel.Outer.Inner.class);
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.annotation.processing.model")
            .setName("Inner")
            .addOuterClass("Outer")
            .addOuterClass("TopLevel")
            .build());

    assertThat(type.toString(Imports.empty()))
        .isEqualTo("com.bdl.annotation.processing.model.TopLevel.Outer.Inner");

    assertThat(type.toString(Imports.empty(), true))
        .isEqualTo("com.bdl.annotation.processing.model.TopLevel.Outer.Inner");
  }

  @Test
  public void testAllTypes() {
    TypeElement element = elements.getTypeElement("com.bdl.annotation.processing.model.ParameterizedMultibound");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type.getAllTypes())
        .containsExactly(
            TypeMetadata.builder()
                .setPackageName("com.bdl.annotation.processing.model")
                .setName("Parameterized")
                .build(),
            TypeMetadata.builder()
                .setPackageName("com.bdl.annotation.processing.model")
                .setName("ParameterizedMultibound")
                .build(),
            TypeMetadata.builder()
                .setPackageName("com.bdl.annotation.processing.model")
                .setName("Simple")
                .build());
  }

  @Test
  public void testComplexParameterization() {
    TypeElement element = elements.getTypeElement("com.bdl.annotation.processing.model.ComplexParameterized");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.annotation.processing.model")
            .setName("ComplexParameterized")
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("X")
                .build())
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("Y")
                .addBound(TypeMetadata.builder()
                    .setPackageName("java.lang")
                    .setName("Comparable")
                    .addParam(TypeMetadata.builder()
                        .setIsTypeParameter(true)
                        .setName("Y")
                        .build())
                    .build())
                .build())
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("Z")
                .addBound(TypeMetadata.builder()
                    .setPackageName("java.util")
                    .setName("List")
                    .addParam(TypeMetadata.builder()
                        .setIsTypeParameter(true)
                        .setName("Y")
                        .build())
                    .build())
                .build())
            .build());

    assertThat(type.toString(Imports.empty()))
        .isEqualTo("com.bdl.annotation.processing.model.ComplexParameterized<X, Y, Z>");

    assertThat(type.toString(Imports.empty(), true))
        .isEqualTo("com.bdl.annotation.processing.model.ComplexParameterized<"
            + "X, Y extends Comparable<Y>, Z extends java.util.List<Y>>");
  }

  @Test
  public void testTypeConversion() {
    TypeElement element = elements.getTypeElement("com.bdl.annotation.processing.model.ComplexParameterized");
    TypeMetadata type = TypeMetadata.fromElement(element);
    type = type.convertTypeParams(ImmutableList.of(
        TypeMetadata.simpleTypeParam("A"),
        TypeMetadata.simpleTypeParam("B"),
        TypeMetadata.simpleTypeParam("C")));
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.annotation.processing.model")
            .setName("ComplexParameterized")
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("A")
                .build())
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("B")
                .addBound(TypeMetadata.builder()
                    .setPackageName("java.lang")
                    .setName("Comparable")
                    .addParam(TypeMetadata.builder()
                        .setIsTypeParameter(true)
                        .setName("B")
                        .build())
                    .build())
                .build())
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("C")
                .addBound(TypeMetadata.builder()
                    .setPackageName("java.util")
                    .setName("List")
                    .addParam(TypeMetadata.builder()
                        .setIsTypeParameter(true)
                        .setName("B")
                        .build())
                    .build())
                .build())
            .build());

    assertThat(type.toString(Imports.empty()))
        .isEqualTo("com.bdl.annotation.processing.model.ComplexParameterized<A, B, C>");

    assertThat(type.toString(Imports.empty(), true))
        .isEqualTo("com.bdl.annotation.processing.model.ComplexParameterized<"
            + "A, B extends Comparable<B>, C extends java.util.List<B>>");
  }

  @Test
  public void testArrayType() {
    TypeElement hasFields = elements.getTypeElement("com.bdl.annotation.processing.model.HasFields");
    Element field = null;
    for (Element element : hasFields.getEnclosedElements()) {
      if (element.getSimpleName().toString().equals("array")) {
        field = element;
        break;
      }
    }

    Preconditions.checkState(field != null, "Unable to find field HasFields.array");
    TypeMetadata type = TypeMetadata.fromType(field.asType());
    assertThat(type).isEqualTo(TypeMetadata.STRING.arrayOf());

    assertThat(type.toString(Imports.empty()))
        .isEqualTo("String[]");
  }

  @Test
  public void testMultipleArrayType() {
    TypeElement hasFields = elements.getTypeElement("com.bdl.annotation.processing.model.HasFields");
    Element field = null;
    for (Element element : hasFields.getEnclosedElements()) {
      if (element.getSimpleName().toString().equals("threeDArray")) {
        field = element;
        break;
      }
    }

    Preconditions.checkState(field != null, "Unable to find field HasFields.threeDArray");
    TypeMetadata type = TypeMetadata.fromType(field.asType());
    assertThat(type).isEqualTo(
        TypeMetadata.INT.arrayOf().arrayOf().arrayOf());
    assertThat(type.toString(Imports.empty()))
        .isEqualTo("int[][][]");
  }
}