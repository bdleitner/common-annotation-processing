package com.bdl.annotation.processing.model;

import java.util.Set;

/**
 * Interface for classes that report needed imports.
 *
 * @author Ben Leitner
 */
interface GeneratesImports {

  Set<TypeMetadata> getImports();
}
