package com.bdl.annotation.processing.model;

import com.google.auto.value.AutoValue;

/**
 * Encapsulation of Metadata for a simple value.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class ValueMetadata {

  abstract TypeMetadata type();
  abstract String value();

  static ValueMetadata create(TypeMetadata type, String value) {
    return new AutoValue_ValueMetadata(type, value);
  }
}
