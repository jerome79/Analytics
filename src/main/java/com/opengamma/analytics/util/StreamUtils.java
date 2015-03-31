/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p>
 * Please see distribution for license.
 */
package com.opengamma.analytics.util;

import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.opengamma.strata.collect.tuple.Pair;

public class StreamUtils {

  private StreamUtils() {
  }

  public static <T, U> Stream<Pair<T, U>> zip(Stream<T> s1, Stream<U> s2) {

    Iterator<T> s1Iter = s1.iterator();
    Iterator<U> s2Iter = s2.iterator();

    return StreamSupport.stream(spliteratorUnknownSize(new Iterator<Pair<T, U>>() {
      @Override
      public boolean hasNext() {
        return s1Iter.hasNext() && s2Iter.hasNext();
      }

      @Override
      public Pair<T, U> next() {
        return Pair.of(s1Iter.next(), s2Iter.next());
      }
    }, Spliterator.ORDERED), false);
  }

  public static <T> Stream<Pair<T, T>> slidingPairs(Stream<T> s) {

    Iterator<T> it = s.iterator();
    if (!it.hasNext()) {
      return Stream.empty();
    }

    return StreamSupport.stream(spliteratorUnknownSize(new Iterator<Pair<T, T>>() {

      private T current = it.next();

      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public Pair<T, T> next() {

        T next = it.next();
        Pair<T, T> pair = Pair.of(current, next);
        current = next;
        return pair;
      }
    }, Spliterator.ORDERED), false);
  }
}
