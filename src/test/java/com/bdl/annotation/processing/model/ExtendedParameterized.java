package com.bdl.annotation.processing.model;

/**
 * A parameterized interface that extends another parameterized interface with a different type param.
 *
 * @author Ben Leitner
 */
@SomeAnnotation
public interface ExtendedParameterized<S> extends Parameterized<S> {

  S extendedFrozzle(S input);
}
