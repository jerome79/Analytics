/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

/**
 * 
 * @param <T> The strike type (e.g. delta)
 */
public class StrikeAlgebra<T extends StrikeType> {

  public T add(final T a, final T b) {
    final double sum = a.getValue() + b.getValue();
    return (T) a.with(sum);
  }

  public T subtract(final T a, final T b) {
    final double diff = a.getValue() - b.getValue();
    return (T) a.with(diff);
  }

}
