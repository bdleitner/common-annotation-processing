package com.bdl.annotation.processing.model;

/**
 * A separate parameterized interface.
 *
 * @author Ben Leitner
 */
interface OtherParameterized<T> {

  T blargh(T input);

  T frumple(T input);
}
