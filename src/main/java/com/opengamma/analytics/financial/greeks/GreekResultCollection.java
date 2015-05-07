/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.greeks;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Joiner;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * 
 */
public class GreekResultCollection implements Iterable<Pair<Greek, Double>> {

  /** The backing map */
  private final Map<Greek, Double> _backingMap = new TreeMap<>();

  /**
   * Gets the value of a greek.
   * @param greek The greek
   * @return the value of the greek
   */
  public Double get(final Greek greek) {
    if (greek == null) {
      return null;
    }
    return _backingMap.get(greek);
  }

  /**
   * Adds a greek to the map
   * @param greek The greek, not null
   * @param result The result
   */
  public void put(final Greek greek, final Double result) {
    ArgChecker.notNull(greek, "Greek");
    // NOTE null result IS a legitimate result.
    // We still put it in the backing map, so that we can tell that a particular
    // greek WAS computed, but the result was also NULL.
    _backingMap.put(greek, result);
  }

  /**
   * @return true if this collection is empty
   */
  public boolean isEmpty() {
    return _backingMap.isEmpty();
  }

  /**
   * @param greek The greek
   * @return true if this collection contains a value for this greek
   */
  public boolean contains(final Greek greek) {
    return _backingMap.containsKey(greek);
  }

  /**
   * @return The number of greeks in this collection
   */
  public int size() {
    return _backingMap.size();
  }

  /**
   * @return All greeks in this collection
   */
  public Set<Greek> keySet() {
    return _backingMap.keySet();
  }

  /**
   * @return All values of the greeks in this collection
   */
  public Collection<Double> values() {
    return Collections.unmodifiableCollection(_backingMap.values());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("GreekResultCollection[");
    final List<String> elements = new LinkedList<>();
    for (final Map.Entry<Greek, Double> entry : _backingMap.entrySet()) {
      final StringBuilder elementSb = new StringBuilder();
      elementSb.append(entry.getKey()).append("=").append(entry.getValue());
      elements.add(elementSb.toString());
    }
    sb.append(Joiner.on(", ").join(elements));
    sb.append("]");
    return sb.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    for (final Map.Entry<Greek, Double> entry : _backingMap.entrySet()) {
      result = prime * result + entry.getKey().hashCode();
      result = prime * result + entry.getValue().hashCode();
    }
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof GreekResultCollection)) {
      return false;
    }
    final GreekResultCollection other = (GreekResultCollection) obj;
    // This is really bad and we'll want to change it when we're less reliant on just the backing map.
    return Objects.equals(_backingMap, other._backingMap);
  }

  @Override
  public Iterator<Pair<Greek, Double>> iterator() {
    // This can be dramatically improved if we change the backing map
    // to not be a backing map at all.
    return new BackingMapGreekIterator(_backingMap.entrySet().iterator());
  }

  /**
   * Iterates over the backing map
   */
  protected static class BackingMapGreekIterator implements Iterator<Pair<Greek, Double>> {
    /**  The backing map iterator */
    private final Iterator<Map.Entry<Greek, Double>> _backingIterator;

    /**
     * @param backingIterator The iterator of the backing map
     */
    public BackingMapGreekIterator(final Iterator<Map.Entry<Greek, Double>> backingIterator) {
      _backingIterator = backingIterator;
    }

    @Override
    public boolean hasNext() {
      return _backingIterator.hasNext();
    }

    @Override
    public Pair<Greek, Double> next() {
      final Map.Entry<Greek, Double> nextEntry = _backingIterator.next();
      return Pair.of(nextEntry.getKey(), nextEntry.getValue());
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Cannot remove from this iterator.");
    }

  }

}
