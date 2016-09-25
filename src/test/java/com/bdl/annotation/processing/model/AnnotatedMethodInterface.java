package com.bdl.annotation.processing.model;

/**
 * An interface with an annotation method, used in testing.
 *
 * @author Ben Leitner
 */
@SuppressWarnings("unused") // Used via compiler element search in MethodMetadataTest.
  @SomeAnnotation(value = "class", anInt = 5, option = AnnotationOption.THIRD)
interface AnnotatedMethodInterface {

  @SomeAnnotation(value = "foo", option = AnnotationOption.SECOND)
  void annotatedSomething();
}
