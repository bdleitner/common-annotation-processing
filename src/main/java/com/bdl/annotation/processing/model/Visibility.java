package com.bdl.annotation.processing.model;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * Enumeration of possible visibilities.
 *
 * @author Ben Leitner
 */
enum Visibility {
  PUBLIC,
  PROTECTED,
  PACKAGE_LOCAL,
  PRIVATE;

  static Visibility forElement(Element element) {
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(Modifier.PUBLIC)) {
      return PUBLIC;
    }
    if (modifiers.contains(Modifier.PRIVATE)) {
      return PRIVATE;
    }
    if (modifiers.contains(Modifier.PROTECTED)) {
      return PROTECTED;
    }
    return PACKAGE_LOCAL;
  }

  String prefix() {
    if (this == PACKAGE_LOCAL) {
      return "";
    }
    return String.format("%s ", name().toLowerCase());
  }
}
