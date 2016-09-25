package com.bdl.annotation.processing.model;

/**
 * @author Ben Leitner
 */
@SuppressWarnings("unused") // Used via compiler element search in TypeMetadataTest.
public interface Field<F extends Field<F>> {

  F add(F that);
  F subtract(F that);
  F multiply(F that);
  F divide(F that);
}
