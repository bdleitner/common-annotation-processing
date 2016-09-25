package com.bdl.annotation.processing.model;

/** Simple interface for testing the annotation processor. */
public interface Simple extends SuperSimple {

  int add(int first, int second);

  String repeat(String template, int times);
}
