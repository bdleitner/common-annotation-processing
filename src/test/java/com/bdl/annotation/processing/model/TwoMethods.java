package com.bdl.annotation.processing.model;

@SuppressWarnings("unused")
public interface TwoMethods {

  void one();

  void two();

  abstract class TwoMethodsOneImplemented implements TwoMethods {

    @Override
    public void one() {
    }
  }
}
