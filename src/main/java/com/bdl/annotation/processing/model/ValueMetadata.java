package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;

/**
 * Encapsulation of Metadata for a simple value.
 *
 * @author Ben Leitner
 */
@AutoValue
public abstract class ValueMetadata {

  public abstract TypeMetadata type();
  public abstract String value();

  static ValueMetadata create(TypeMetadata type, String value) {
    return new AutoValue_ValueMetadata(type, value);
  }
}
