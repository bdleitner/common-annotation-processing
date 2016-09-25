package com.bdl.annotation.processing.model;

/**
 * Interface with multiple parameters, one of which is multibound.
 *
 * @author Ben Leitner
 */
@SuppressWarnings("unused") // Used via compiler element search in TypeMetadataTest.
public interface ParameterizedMultibound<S, T extends Simple & Parameterized<S>> {
}
