package com.bdl.annotation.processing.model;

import java.util.List;

/**
 * A testing class that utilizes multiple forms of inheritance for testing.
 *
 * @author Ben Leitner
 */
@SomeAnnotation
abstract class AbstractClass<A, B extends Comparable<B>, C extends List<B>>
    extends AbstractSuperclass<B>
    implements ExtendedExtendedParameterized<A>, ComplexParameterized<A, B, C>, OtherSimple {

  private AbstractClass(String blargh) {
    super(blargh);
  }

  AbstractClass(int foo) {
    super(String.valueOf(foo));
  }

  public AbstractClass(boolean foo) {
    this(foo ? 1 : 0);
  }

  @Override
  public A frozzle(A input) {
    return input;
  }

  @Override
  protected int fromSuper(int foo) {
    return foo;
  }
}
