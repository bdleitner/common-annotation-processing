package com.bdl.annotation.processing.model;

import static com.google.common.truth.Truth.assertThat;

import com.google.testing.compile.CompilationRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/** Tests for the {@link AnnotationMetadata} class. */
@RunWith(JUnit4.class)
public class AnnotationMetadataTest {

  @Rule public final CompilationRule compilation = new CompilationRule();

  private Elements elements;

  @Before
  public void before() {
    elements = compilation.getElements();
  }

  @Test
  public void testFromElement() {
    TypeElement typeElement = elements.getTypeElement("com.bdl.annotation.processing.model.AnnotatedMethodInterface");
    AnnotationMirror annotationMirror = typeElement.getAnnotationMirrors().get(0);
    AnnotationMetadata actual = AnnotationMetadata.fromType(annotationMirror);
    AnnotationMetadata expected = AnnotationMetadata.builder()
        .setType(TypeMetadata.builder()
            .setPackageName("com.bdl.annotation.processing.model")
            .setName("SomeAnnotation")
            .build())
        .putValue(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setIsAbstract(true)
                .setType(TypeMetadata.STRING)
                .setName("value")
                .build(),
            ValueMetadata.create(TypeMetadata.STRING, "class"))
        .putValue(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setIsAbstract(true)
                .setType(TypeMetadata.INT)
                .setName("anInt")
                .build(),
            ValueMetadata.create(TypeMetadata.INT, "5"))
        .putValue(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setIsAbstract(true)
                .setType(TestingTypes.ANNOTATION_OPTION)
                .setName("option")
                .build(),
            ValueMetadata.create(TestingTypes.ANNOTATION_OPTION,
                "THIRD"))
        .build();
    assertThat(actual).isEqualTo(expected);
  }
}
