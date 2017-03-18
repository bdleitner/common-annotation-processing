package com.bdl.annotation.processing.model;

import com.bdl.annotation.processing.model.ClassMetadata.Category;
import com.google.testing.compile.CompilationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import static com.bdl.annotation.processing.model.TypeMetadata.simpleTypeParam;
import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for {@link InheritanceMetadata}
 *
 * @author Ben Leitner
 */
@RunWith(JUnit4.class)
public class InheritanceMetadataTest {

  @Rule public final CompilationRule compilation = new CompilationRule();

  private Elements elements;

  @Before
  public void before() {
    elements = compilation.getElements();
  }

  @Test
  public void testSimpleInheritance() {
    TypeElement simpleElement =
        elements.getTypeElement("com.bdl.annotation.processing.model.Simple");
    TypeMirror inherited = simpleElement.getInterfaces().get(0);
    InheritanceMetadata actual = InheritanceMetadata.fromType((DeclaredType) inherited);
    assertThat(actual)
        .isEqualTo(
            InheritanceMetadata.builder()
                .setClassMetadata(
                    ClassMetadata.builder()
                        .setModifiers(Modifiers.visibility(Visibility.PUBLIC).makeAbstract())
                        .setCategory(Category.INTERFACE)
                        .setType(
                            TypeMetadata.builder()
                                .setPackageName("com.bdl.annotation.processing.model")
                                .setName("SuperSimple")
                                .build())
                        .addMethod(
                            MethodMetadata.builder()
                                .setModifiers(
                                    Modifiers.visibility(Visibility.PUBLIC).makeAbstract())
                                .setType(TypeMetadata.STRING)
                                .setName("thingToString")
                                .addParameter(ParameterMetadata.of(TestingTypes.THING, "thing"))
                                .build())
                        .build())
                .build());
  }

  @Test
  public void testParameterizedInheritance() {
    TypeElement simpleElement =
        elements.getTypeElement("com.bdl.annotation.processing.model.ExtendedParameterized");
    TypeMirror inherited = simpleElement.getInterfaces().get(0);
    InheritanceMetadata actual = InheritanceMetadata.fromType((DeclaredType) inherited);
    assertThat(actual)
        .isEqualTo(
            InheritanceMetadata.builder()
                .addInheritanceParam(simpleTypeParam("S"))
                .setClassMetadata(
                    ClassMetadata.builder()
                        .setModifiers(Modifiers.visibility(Visibility.PUBLIC).makeAbstract())
                        .setCategory(Category.INTERFACE)
                        .setType(
                            TypeMetadata.builder()
                                .setPackageName("com.bdl.annotation.processing.model")
                                .setName("Parameterized")
                                .addParam(TestingTypes.PARAM_T)
                                .build())
                        .addMethod(
                            MethodMetadata.builder()
                                .setModifiers(
                                    Modifiers.visibility(Visibility.PUBLIC).makeAbstract())
                                .setType(TestingTypes.PARAM_T)
                                .setName("frozzle")
                                .addParameter(ParameterMetadata.of(TestingTypes.PARAM_T, "input"))
                                .build())
                        .build())
                .build());

    assertThat(actual.getAllMethods())
        .containsExactly(
            MethodMetadata.builder()
                .setModifiers(Modifiers.visibility(Visibility.PUBLIC).makeAbstract())
                .setType(TestingTypes.PARAM_S)
                .setName("frozzle")
                .addParameter(ParameterMetadata.of(TestingTypes.PARAM_S, "input"))
                .build());
  }

  @Test
  public void testExtendedParameterizedInheritance() {
    TypeElement simpleElement =
        elements.getTypeElement(
            "com.bdl.annotation.processing.model.ExtendedExtendedParameterized");
    TypeMirror inherited = simpleElement.getInterfaces().get(0);
    InheritanceMetadata actual = InheritanceMetadata.fromType((DeclaredType) inherited);
    assertThat(actual)
        .isEqualTo(
            InheritanceMetadata.builder()
                .addInheritanceParam(simpleTypeParam("C"))
                .setClassMetadata(
                    ClassMetadata.builder()
                        .setModifiers(Modifiers.visibility(Visibility.PUBLIC).makeAbstract())
                        .setCategory(Category.INTERFACE)
                        .setType(
                            TypeMetadata.builder()
                                .setPackageName("com.bdl.annotation.processing.model")
                                .setName("ExtendedParameterized")
                                .addParam(simpleTypeParam("S"))
                                .build())
                        .addAnnotation(
                            AnnotationMetadata.builder()
                                .setType(TestingTypes.SOME_ANNOTATION)
                                .build())
                        .addInheritance(
                            InheritanceMetadata.builder()
                                .addInheritanceParam(simpleTypeParam("S"))
                                .setClassMetadata(
                                    ClassMetadata.builder()
                                        .setModifiers(Modifiers.visibility(Visibility.PUBLIC).makeAbstract())
                                        .setCategory(Category.INTERFACE)
                                        .setType(
                                            TypeMetadata.builder()
                                                .setPackageName(
                                                    "com.bdl.annotation.processing.model")
                                                .setName("Parameterized")
                                                .addParam(simpleTypeParam("T"))
                                                .build())
                                        .addMethod(
                                            MethodMetadata.builder()
                                                .setModifiers(
                                                    Modifiers.visibility(Visibility.PUBLIC)
                                                        .makeAbstract())
                                                .setType(TestingTypes.PARAM_T)
                                                .setName("frozzle")
                                                .addParameter(
                                                    ParameterMetadata.of(
                                                        TestingTypes.PARAM_T, "input"))
                                                .build())
                                        .build())
                                .build())
                        .addMethod(
                            MethodMetadata.builder()
                                .setModifiers(
                                    Modifiers.visibility(Visibility.PUBLIC).makeAbstract())
                                .setType(TestingTypes.PARAM_S)
                                .setName("extendedFrozzle")
                                .addParameter(ParameterMetadata.of(TestingTypes.PARAM_S, "input"))
                                .build())
                        .build())
                .build());

    assertThat(actual.getAllMethods())
        .containsExactly(
            MethodMetadata.builder()
                .setModifiers(Modifiers.visibility(Visibility.PUBLIC).makeAbstract())
                .setType(simpleTypeParam("C"))
                .setName("frozzle")
                .addParameter(ParameterMetadata.of(simpleTypeParam("C"), "input"))
                .build(),
            MethodMetadata.builder()
                .setModifiers(Modifiers.visibility(Visibility.PUBLIC).makeAbstract())
                .setType(simpleTypeParam("C"))
                .setName("extendedFrozzle")
                .addParameter(ParameterMetadata.of(simpleTypeParam("C"), "input"))
                .build());
  }
}
