package com.bdl.annotation.processing.model;

import com.google.testing.compile.CompilationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for the FieldMetadata class.
 *
 * @author Ben Leitner
 */
@RunWith(JUnit4.class)
public class FieldMetadataTest {

  @Rule public final CompilationRule compilation = new CompilationRule();

  private Elements elements;

  @Before
  public void before() {
    elements = compilation.getElements();
  }

  private Element getElement(TypeElement container, String name) {
    for (Element element : container.getEnclosedElements()) {
      if (element.getSimpleName().toString().equals(name)) {
        return element;
      }
    }
    throw new IllegalArgumentException(String.format("No element named %s found", name));
  }

  @Test
  public void testFromElement() {
    TypeElement typeElement =
        elements.getTypeElement("com.bdl.annotation.processing.model.HasFields");

    Element foo = getElement(typeElement, "foo");
    FieldMetadata field = FieldMetadata.from(foo);
    TypeMetadata hasFieldsType =
        TypeMetadata.builder()
            .setPackageName("com.bdl.annotation.processing.model")
            .setName("HasFields")
            .build();
    assertThat(field)
        .isEqualTo(
            FieldMetadata.builder()
                .containingClass(hasFieldsType)
                .modifiers(Modifiers.visibility(Visibility.PRIVATE))
                .name("foo")
                .type(TypeMetadata.STRING)
                .build());

    Element bar = getElement(typeElement, "bar");
    field = FieldMetadata.from(bar);
    assertThat(field)
        .isEqualTo(
            FieldMetadata.builder()
                .containingClass(hasFieldsType)
                .modifiers(Modifiers.visibility(Visibility.PROTECTED))
                .name("bar")
                .type(TypeMetadata.OBJECT)
                .build());

    Element baz = getElement(typeElement, "baz");
    field = FieldMetadata.from(baz);
    assertThat(field)
        .isEqualTo(
            FieldMetadata.builder()
                .containingClass(hasFieldsType)
                .addAnnotation(
                    AnnotationMetadata.builder().setType(TestingTypes.SOME_ANNOTATION).build())
                .modifiers(Modifiers.visibility(Visibility.PUBLIC).makeStatic())
                .name("baz")
                .type(TypeMetadata.INT)
                .build());

    Element blargh = getElement(typeElement, "blargh");
    field = FieldMetadata.from(blargh);
    assertThat(field)
        .isEqualTo(
            FieldMetadata.builder()
                .containingClass(hasFieldsType)
                .modifiers(Modifiers.visibility(Visibility.PACKAGE_LOCAL).makeFinal())
                .name("blargh")
                .type(TypeMetadata.LONG)
                .build());

    Element threeDArray = getElement(typeElement, "threeDArray");
    field = FieldMetadata.from(threeDArray);
    assertThat(field)
        .isEqualTo(
            FieldMetadata.builder()
                .containingClass(hasFieldsType)
                .modifiers(Modifiers.visibility(Visibility.PRIVATE).makeStatic().makeFinal())
                .name("threeDArray")
                .type(TypeMetadata.INT.arrayOf().arrayOf().arrayOf())
                .build());
  }
}
