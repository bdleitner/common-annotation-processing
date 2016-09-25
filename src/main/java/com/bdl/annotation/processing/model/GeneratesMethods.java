package com.bdl.annotation.processing.model;

import com.google.common.collect.ImmutableList;

/**
 * Interface for classes that generate methods that need implementing.
 *
 * @author Ben Leitner
 */
interface GeneratesMethods {

  ImmutableList<MethodMetadata> getAllMethods();

}
