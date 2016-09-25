package com.bdl.annotation.processing.model;

import java.util.Set;

/**
 * Interface for classes that use types.
 *
 * @author Ben Leitner
 */
interface UsesTypes {

  Set<TypeMetadata> getAllTypes();
}
