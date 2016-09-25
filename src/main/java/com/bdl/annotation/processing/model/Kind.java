package com.bdl.annotation.processing.model;

/**
 * Enumeration of the broad kinds of types.
 *
 * @author Ben Leitner
 */
public enum Kind {
  /** primitive or boxed numeric types. */
  NUMERIC,
  /** primitive or boxed booleans. */
  BOOLEAN,
  /** voids. */
  VOID,
  /** Strings. */
  STRING,
  /** All other objects. */
  OBJECT
}
