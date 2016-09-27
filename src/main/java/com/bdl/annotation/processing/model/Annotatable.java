package com.bdl.annotation.processing.model;

import com.google.common.collect.ImmutableList;

/**
 * Interface for metadata classes that represent something that can be annotated.
 *
 * @author Ben Leitner
 */
public interface Annotatable {

  /** Annotations defined on the item. */
  ImmutableList<AnnotationMetadata> annotations();
}
