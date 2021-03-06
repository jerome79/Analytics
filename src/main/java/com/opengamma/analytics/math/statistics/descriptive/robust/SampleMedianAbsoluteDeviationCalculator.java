/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive.robust;

import java.util.function.Function;

import com.opengamma.analytics.math.statistics.descriptive.MedianCalculator;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class SampleMedianAbsoluteDeviationCalculator implements Function<double[], Double> {
  private static final Function<double[], Double> MEDIAN = new MedianCalculator();

  @Override
  public Double apply(final double[] x) {
    ArgChecker.notNull(x, "x");
    final int n = x.length;
    ArgChecker.isTrue(n > 1, "Need at least two data points to calculate MAD");
    final double median = MEDIAN.apply(x);
    final double[] diff = new double[n];
    for (int i = 0; i < n; i++) {
      diff[i] = Math.abs(x[i] - median);
    }
    return MEDIAN.apply(diff);
  }

}
