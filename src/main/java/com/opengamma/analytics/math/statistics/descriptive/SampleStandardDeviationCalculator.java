/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculates the sample standard deviation of a series of data. The sample standard deviation of a series of data is defined as the square root of 
 * the sample variance (see {@link SampleVarianceCalculator}).
 */
public class SampleStandardDeviationCalculator extends Function1D<double[], Double> {
  private static final Function1D<double[], Double> VARIANCE = new SampleVarianceCalculator();

  /**
   * @param x The array of data, not null, must contain at least two data points
   * @return The sample standard deviation
   */
  @Override
  public Double evaluate(final double[] x) {
    ArgChecker.notNull(x, "x");
    ArgChecker.isTrue(x.length >= 2, "Need at least two points to calculate standard deviation");
    return Math.sqrt(VARIANCE.evaluate(x));
  }

}
