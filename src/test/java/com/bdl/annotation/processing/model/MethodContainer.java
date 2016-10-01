package com.bdl.annotation.processing.model;

/**
 * Container of some methods for MethodMetadata testing.
 *
 * @author Ben Leitner
 */
interface MethodContainer {

  void noParams();

  String[] arraysMethod(int[][] inputs);

  int someParams(int arg1, int arg2);

  @SomeAnnotation
  Object annotated();
}
