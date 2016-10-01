package com.bdl.annotation.processing.model;

import java.util.Set;

/**
 * Interface for classes that use types.
 *
 * @author Ben Leitner
 */
public interface UsesTypes {

  Set<TypeMetadata> getAllTypes();
}
