package com.bdl.annotation.processing.model;

/** @author Ben Leitner */
interface OtherSimple {

  String blorp(String input);

  @Override
  String toString();

  void doNothing(String input);
}
