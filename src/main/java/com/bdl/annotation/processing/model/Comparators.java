package com.bdl.annotation.processing.model;

import com.google.common.collect.ComparisonChain;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Container class for some useful Comparators.
 *
 * @author Ben Leitner
 */
class Comparators {

  private Comparators() {
    // Container class, no instantiation.
  }

  /** Comparator for lists of comparable things. */
  public static <S extends Comparable<S>, T extends List<S>> Comparator<T> forLists() {
    return (first, second) -> {
      ComparisonChain chain = ComparisonChain.start();
      chain = chain.compare(first.size(), second.size());
      for (int i = 0; i < Math.min(first.size(), second.size()); i++) {
        chain = chain.compare(first.get(i), second.get(i));
      }
      return chain.result();
    };
  }

  /** Comparator for lists of comparable things. */
  public static <I, O extends Comparable<O>, T extends List<I>> Comparator<T> forLists(
      Function<I, O> function) {
    return (first, second) -> {
      ComparisonChain chain = ComparisonChain.start();
      chain = chain.compare(first.size(), second.size());
      for (int i = 0; i < Math.min(first.size(), second.size()); i++) {
        chain = chain.compare(function.apply(first.get(i)), function.apply(second.get(i)));
      }
      return chain.result();
    };
  }
}
