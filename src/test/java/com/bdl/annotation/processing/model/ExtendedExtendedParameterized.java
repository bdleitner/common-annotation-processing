package com.bdl.annotation.processing.model;

/**
 * A parameterized interface that extends another parameterized interface that extends another parameterized interface,
 * al with different type params.
 *
 * @author Ben Leitner
 */
@SuppressWarnings("unused") // Used via compiler element search in InheritanceMetadataTest.
public interface ExtendedExtendedParameterized<C> extends ExtendedParameterized<C> {

  C superExtendedFrozzle(C input);
}
