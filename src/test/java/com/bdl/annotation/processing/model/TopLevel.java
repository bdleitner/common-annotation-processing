package com.bdl.annotation.processing.model;

/**
 * Nested classes for testing.
 *
 * @author Ben Leitner
 */
@SuppressWarnings("unused") // Used via compiler element search in TypeMetadataTest.
class TopLevel {

  static class Outer {

    static class Inner {}
  }
}
