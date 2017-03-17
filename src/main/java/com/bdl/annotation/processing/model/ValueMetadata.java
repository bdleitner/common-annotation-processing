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

  public static ValueMetadata create(TypeMetadata type, String value) {
    return new AutoValue_ValueMetadata(type, value);
  }

  public static ValueMetadata create(Object object) {
    return new AutoValue_ValueMetadata(TypeMetadata.fromObject(object), object.toString());
  }
}
