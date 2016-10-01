package com.bdl.annotation.processing.model;

/**
 * Contains fields used for FieldMetadata testing.
 *
 * @author Ben Leitner
 */
@SuppressWarnings("unused") // Used in FieldMetadataTest.
public class HasFields {

  private String foo;
  protected Object bar;

  @SomeAnnotation
  public int baz;

  long blargh = 5;

  private String[] array;
  private int[][][] threeDArray;
}
