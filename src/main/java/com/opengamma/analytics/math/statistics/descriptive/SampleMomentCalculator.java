/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import java.util.function.Function;

import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculates the $n^th$ sample raw moment of a series of data.
 * <p>
 * The sample raw moment $m_n$ of a series of data $x_1, x_2, \dots, x_s$ is given by:
 * $$
 * \begin{align*}
 * m_n = \frac{1}{s}\sum_{i=1}^s x_i^n
 * \end{align*}
 * $$
 */
public class SampleMomentCalculator implements Function<double[], Double> {
  private final int _n;

  /**
   * @param n The degree of the moment to calculate, cannot be negative
   */
  public SampleMomentCalculator(final int n) {
    _n = ArgChecker.notNegativeOrZero(n, "n");
  }

  /**
   * @param x The array of data, not null or empty
   * @return The sample raw moment
   */
  @Override
  public Double apply(final double[] x) {
    ArgChecker.notNull(x, "x was null");
    ArgChecker.isTrue(x.length > 0, "x was empty");
    if (_n == 0) {
      return 1.;
    }
    double sum = 0;
    for (final Double d : x) {
      sum += Math.pow(d, _n);
    }
    return sum / (x.length - 1);
  }

}
