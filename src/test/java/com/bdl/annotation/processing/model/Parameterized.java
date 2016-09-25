package com.bdl.annotation.processing.model;

/** Simple interface for testing the annotation processor. */
public interface Parameterized<T> {

  T frozzle(T input);
}
