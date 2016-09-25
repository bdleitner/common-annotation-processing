package com.bdl.annotation.processing.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation used for testing.
 *
 * @author Ben Leitner
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SomeAnnotation {
  String value() default "";

  int anInt() default 2;

  AnnotationOption option() default AnnotationOption.FIRST;
}
