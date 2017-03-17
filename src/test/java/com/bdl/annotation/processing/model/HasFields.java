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

  @SomeAnnotation public static int baz;

  final long blargh = 5;

  private String[] array;
  private static final int[][][] threeDArray = new int[1][2][3];
}
